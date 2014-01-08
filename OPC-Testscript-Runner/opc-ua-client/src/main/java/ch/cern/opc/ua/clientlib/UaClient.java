package ch.cern.opc.ua.clientlib;

import static ch.cern.opc.common.Log.logDebug;
import static ch.cern.opc.common.Log.logError;
import static ch.cern.opc.common.Log.logInfo;
import static ch.cern.opc.ua.clientlib.EndpointSummary.matchEndpoint;
import static ch.cern.opc.ua.clientlib.EndpointSummary.toEndpointSummaries;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;

import org.apache.commons.lang3.ArrayUtils;
import org.opcfoundation.ua.application.Client;
import org.opcfoundation.ua.builtintypes.DataValue;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.common.ServiceFaultException;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.EndpointDescription;
import org.opcfoundation.ua.transport.security.Cert;
import org.opcfoundation.ua.transport.security.KeyPair;
import org.opcfoundation.ua.transport.security.PrivKey;

import ch.cern.opc.ua.clientlib.addressspace.AddressSpace;
import ch.cern.opc.ua.clientlib.addressspace.NodeDescription;
import ch.cern.opc.ua.clientlib.notification.OPCUAAsyncUpdateCallback;
import ch.cern.opc.ua.clientlib.session.Session;
import ch.cern.opc.ua.clientlib.subscription.Subscription;

public class UaClient implements UaClientInterface 
{
	private final static Class<?>[] INVALID_DATA_TYPE = new Class<?>[0];
	
	private static UaClient instance = new UaClient();
	
	public static UaClientInterface instance() 
	{
		return instance;
	}

	private Client client = null;
	private Session session = null;
	private EndpointDescription[] endpoints;
	private OPCUAAsyncUpdateCallback dslCallback;

	private UaClient()
	{
	}

	/* (non-Javadoc)
	 * @see ch.cern.opc.ua.clientlib.UaClientInterface#setCertificate(java.io.File, java.io.File, java.lang.String)
	 */
	@Override
	public void setCertificate(final File publicKeyFile, final File privateKeyFile, final String password)
	{
		if (publicKeyFile == null) throw new IllegalArgumentException("certificate must be non-null");
		if (privateKeyFile == null) throw new IllegalArgumentException("private key must be non-null");

		try {
			Cert publicKey = Cert.load(publicKeyFile.toURI().toURL());
			if(publicKey == null) throw new IllegalArgumentException("failed to load public certificate");

			PrivKey privateKey = PrivKey.loadFromKeyStore(privateKeyFile.toURI().toURL(), password);
			if(privateKey == null) throw new IllegalArgumentException("faield to load private key");

			client = new Client(new KeyPair(publicKey, privateKey));
		} 
		catch (MalformedURLException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see ch.cern.opc.ua.clientlib.UaClientInterface#getEndpoints(java.net.URI)
	 */
	@Override
	public EndpointSummary[] getEndpoints(URI serverURI) throws IllegalStateException
	{
		endpoints = new EndpointDescription[]{};
		try 
		{
			endpoints = client.discoverEndpoints(serverURI);
			return toEndpointSummaries(endpoints);
		} 
		catch (ServiceFaultException e) 
		{
			e.printStackTrace();
		} 
		catch (ServiceResultException e) 
		{
			e.printStackTrace();
		}

		throw new IllegalStateException("Failed to connect to server at ["+serverURI+"]");
	}

	/* (non-Javadoc)
	 * @see ch.cern.opc.ua.clientlib.UaClientInterface#startSession(ch.cern.opc.ua.clientlib.EndpointSummary)
	 */
	@Override
	public void startSession(final EndpointSummary endpoint)
	{
		logInfo("starting session with endpoint:\n"+endpoint);
		
		if(session == null)
		{
			EndpointDescription endpointDsc = matchEndpoint(endpoint, endpoints);
			if(endpointDsc != null)
			{
				session = new Session(client, endpointDsc);
			}
			else
			{
				logError("Failed to locate target endpoint:\n"+endpoint+"\nfrom known server endpoints:\n"+ArrayUtils.toString(endpoints));
			}
		}
		
		if(!session.setup())
		{
			logError("Failed to start session with endpoint ["+endpoint+"]");
		}
	}

	/* (non-Javadoc)
	 * @see ch.cern.opc.ua.clientlib.UaClientInterface#stopSession()
	 */
	@Override
	public void stopSession()
	{
		if(session != null)
		{
			if(session.close())
			{
				logInfo("Closed session with endpoint ["+session.getEndpoint()+"]");
			}
			else
			{
				logError("Failed to close session with endpoint ["+session.getEndpoint()+"]");
			}
		}
		
		session = null;
	}
	
	/* (non-Javadoc)
	 * @see ch.cern.opc.ua.clientlib.UaClientInterface#browseNamespace()
	 */
	@Override
	public String[] browseNamespace()
	{
		if(session != null)
		{
			return session.getNamespace();
		}

		logError("Cannot browse namespace until a client/server session has been established");
		return new String[] {"Cannot browse namespace until a client/server session has been established"};
	}

	/* (non-Javadoc)
	 * @see ch.cern.opc.ua.clientlib.UaClientInterface#browseAddressspace()
	 */
	@Override
	public AddressSpace browseAddressspace()
	{
		if(session != null)
		{
			return session.getAddressspace();
		}
		
		logError("Failed to browse addressspace - no session has been created");
		return null;
	}

	/* (non-Javadoc)
	 * @see ch.cern.opc.ua.clientlib.UaClientInterface#readNodeValue(java.lang.String)
	 */
	@Override
	public DataValue[] readNodeValue(final NodeId nodeId)
	{
		if(session != null)
		{
			NodeDescription node = session.getAddressspace().findNodeById(nodeId);
			return session.getReader().readNodeValue(node);
		}

		logError("Failed to read node ["+nodeId+"] - session was not created");
		return new DataValue[]{};
	}

	/* (non-Javadoc)
	 * @see ch.cern.opc.ua.clientlib.UaClientInterface#readNodeDataTypes(java.lang.String)
	 */
	@Override
	public Class<?>[] readNodeDataTypes(final NodeId nodeId)
	{
		if(session == null)
		{
			logError("failed to read node ["+nodeId+"] data type - no session");
			return INVALID_DATA_TYPE;
		}
		
		NodeDescription node = session.getAddressspace().findNodeById(nodeId);
			
		if(node == null)
		{
			logError("failed to read node ["+nodeId+"] data type - node not found in address space");
			return INVALID_DATA_TYPE; 
		}
			
		if(!session.getBrowser().getNodeDataTypes(node))
		{
			logError("failed to read node ["+nodeId+"] data type - found node ["+nodeId+"] but data type read failed");
		}
		
		return node.getDataTypes();
	}

	/* (non-Javadoc)
	 * @see ch.cern.opc.ua.clientlib.UaClientInterface#writeNodeValue(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean writeNodeValueSync(final NodeId nodeId, String... values)
	{
		logDebug("Writing synchronously  to node ["+nodeId+"], values: "+ArrayUtils.toString(values));
		
		if(nodeId == null) return false;
		
		if(session != null)
		{
			NodeDescription node = session.getAddressspace().findNodeById(nodeId);
			return session.getWriter().writeNodeValueSync(node, values);
		}

		return false;
	}
	
	@Override
	public boolean writeNodeValueAsync(NodeId nodeId, String... values) 
	{
		logDebug("Writing asynchronously  to node ["+nodeId+"], values: "+ArrayUtils.toString(values));
		
		if(nodeId == null) return false;
		
		if(session != null)
		{
			NodeDescription node = session.getAddressspace().findNodeById(nodeId);
			return session.getWriter().writeNodeValueAsync(node, values);
		}

		return false;
	}

	
	/* (non-Javadoc)
	 * @see ch.cern.opc.ua.clientlib.UaClientInterface#startSubscription(java.lang.String)
	 */
	@Override
	public boolean startSubscription(final String subscriptionName)
	{
		Subscription subscription = session.createSubscription(subscriptionName, dslCallback);
		if(subscription != null)
		{
			return subscription.isActive();
		}
		
		return false;
	}
	
	@Override
	public boolean deleteSubscription(final String subscriptionName)
	{
		return session.deleteSubscription(subscriptionName);
	}

	@Override
	public boolean hasSubscription(String subscriptionName) 
	{
		return (session.getSubscription(subscriptionName) != null);
	}
	
	
	/* (non-Javadoc)
	 * @see ch.cern.opc.ua.clientlib.UaClientInterface#monitorNodeValues(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean monitorNodeValues(final String subscriptionName, final NodeId... nodeIds)
	{
		final Subscription subscription = session.getSubscription(subscriptionName);
		
		if(subscription == null)
		{
			logError("No subscription found with name ["+subscriptionName+"], unable to monitor ["+nodeIds.length+"] nodes");
			return false;
		}
		
		if(!subscription.isActive())
		{
			logError("Subscription name ["+subscriptionName+"] id ["+subscription.getSubscriptionId()+"] was not correctly established with the server, unable to monitor ["+nodeIds.length+"] nodes");
			return false;
		}
		
		logDebug("Adding ["+nodeIds.length+"] nodes to subscription name ["+subscriptionName+"] id ["+subscription.getSubscriptionId()+"]");
		boolean result = subscription.addMonitoredItems(session.getAddressspace().findNodesById(nodeIds));
		logDebug("Added ["+nodeIds.length+"] nodes to subscription name ["+subscriptionName+"] id ["+subscription.getSubscriptionId()+"], result ["+result+"]");
		
		return result;
	}

	/* (non-Javadoc)
	 * @see ch.cern.opc.ua.clientlib.UaClientInterface#getLastError()
	 */
	@Override
	public String getLastError() 
	{
		return "getLastError not implemented yet in opc-ua client";
	}
	
	protected void injectTestSession(Session testSession)
	{
		System.out.println("Warning: injecting test session to UaClient instance");
		session = testSession;
	}

	@Override
	public void registerAsyncUpdate(OPCUAAsyncUpdateCallback callback) 
	{
		if(callback == null) throw new IllegalArgumentException("Cannot pass null async update handler");
		
		logDebug("DSL async update handler registered");
		dslCallback = callback;
	}
}
