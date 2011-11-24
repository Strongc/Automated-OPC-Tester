package ch.cern.opc.ua.clientlib;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;

import org.apache.commons.lang3.ArrayUtils;
import org.opcfoundation.ua.application.Client;
import org.opcfoundation.ua.common.ServiceFaultException;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.EndpointDescription;
import org.opcfoundation.ua.transport.security.Cert;
import org.opcfoundation.ua.transport.security.KeyPair;
import org.opcfoundation.ua.transport.security.PrivKey;

import ch.cern.opc.ua.clientlib.addressspace.AddressSpace;
import ch.cern.opc.ua.clientlib.addressspace.NodeDescription;
import ch.cern.opc.ua.clientlib.session.Session;
import ch.cern.opc.ua.clientlib.subscription.Subscription;
import static ch.cern.opc.ua.clientlib.EndpointSummary.toEndpointSummaries;
import static ch.cern.opc.ua.clientlib.EndpointSummary.matchEndpoint;

public class UaClient 
{
	private final static Class<?>[] INVALID_DATA_TYPE = new Class<?>[0];
	
	private static UaClient instance = new UaClient();
	public static UaClient instance() 
	{
		return instance;
	}

	private Client client = null;
	private Session session = null;
	private EndpointDescription[] endpoints;

	private UaClient()
	{
	}

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

	public void startSession(final EndpointSummary endpoint)
	{
		System.out.println("starting session with endpoint:\n"+endpoint);
		
		if(session == null)
		{
			EndpointDescription endpointDsc = matchEndpoint(endpoint, endpoints);
			if(endpointDsc != null)
			{
				session = new Session(client, endpointDsc);
			}
			else
			{
				System.err.println("Failed to locate target endpoint:\n"+endpoint+"\nfrom known server endpoints:\n"+ArrayUtils.toString(endpoints));
			}
		}
		
		if(!session.setup())
		{
			System.err.println("Failed to start session with endpoint ["+endpoint+"]");
		}
	}

	public void stopSession()
	{
		if(session != null)
		{
			if(!session.close())
			{
				System.err.println("Failed to close session with endpoint ["+session.getEndpoint()+"]");
			}
		}
		
		session = null;
	}

	public String[] browseNamespace()
	{
		if(session != null)
		{
			return session.getNamespace();
		}

		return new String[] {"Cannot browse namespace until a client/server session has been established"};
	}

	public AddressSpace browseAddressspace()
	{
		if(session != null)
		{
			return session.getAddressspace();
		}

		return null;
	}

	public String[] readNodeValue(final String nodeId)
	{
		if(session != null)
		{
			NodeDescription node = session.getAddressspace().findNodeById(nodeId);
			return session.getReader().readNodeValue(node);
		}
		
		return new String[]{"Failed to read node ["+nodeId+"] - session was not created"};
	}

	public Class<?>[] readNodeDataTypes(final String nodeId)
	{
		if(session == null)
		{
			System.err.println("null session");
			return INVALID_DATA_TYPE;
		}
		
		NodeDescription node = session.getAddressspace().findNodeById(nodeId);
			
		if(node == null)
		{
			System.err.println("Failed to find node ["+nodeId+"] in address space");
			return INVALID_DATA_TYPE; 
		}
			
		if(!session.getBrowser().getNodeDataTypes(node))
		{
			System.err.println("Failed to read node data type ["+nodeId+"]");
		}
		
		return node.getDataTypes();
	}

	public boolean writeNodeValue(final String nodeId, String... values)
	{
		System.out.println("Writing to node ["+nodeId+"], values: "+ArrayUtils.toString(values));
		
		if(nodeId == null) return false;
		
		if(session != null)
		{
			NodeDescription node = session.getAddressspace().findNodeById(nodeId);
			return session.getWriter().writeNodeValue(node, values);
		}

		return false;
	}
	
	public boolean startSubscription(final String subscriptionName)
	{
		Subscription subscription = session.createSubscription(subscriptionName);
		if(subscription != null)
		{
			return subscription.isCreated();
		}
		
		return false;
	}
	
	public boolean monitorNodeValues(final String subscriptionName, final String... nodeIds)
	{
		final Subscription subscription = session.createSubscription(subscriptionName);
		
		if(subscription == null)
		{
			System.err.println("No subscription found with name ["+subscriptionName+"], unable to monitor nodes ["+ArrayUtils.toString(nodeIds)+"]");
			return false;
		}
		
		if(!subscription.isCreated())
		{
			System.err.println("Subscription name ["+subscriptionName+"] id ["+subscription.getSubscriptionId()+"] was not correctly established with the server, unable to monitor nodes ["+ArrayUtils.toString(nodeIds)+"]");
			return false;
		}
		
		System.out.println("Adding nodes ["+ArrayUtils.toString(nodeIds)+"] to subscription name ["+subscriptionName+"] id ["+subscription.getSubscriptionId()+"]");
		boolean result = subscription.addMonitoredItems(session.getAddressspace().findNodesById(nodeIds));
		System.out.println("Added ["+nodeIds.length+"] items to subscription name ["+subscriptionName+"] id ["+subscription.getSubscriptionId()+"], result ["+result+"]");
		
		return result;
	}
}
