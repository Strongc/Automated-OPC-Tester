package ch.cern.opc.ua.clientlib.subscription;

import static ch.cern.opc.common.Log.logDebug;
import static ch.cern.opc.common.Log.logError;
import static ch.cern.opc.common.Log.logTrace;
import static ch.cern.opc.common.Log.logWarning;
import static org.apache.commons.lang3.ArrayUtils.isEmpty;
import static org.apache.commons.lang3.ArrayUtils.isNotEmpty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.opcfoundation.ua.application.SessionChannel;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.UnsignedByte;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.common.ServiceFaultException;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.Attributes;
import org.opcfoundation.ua.core.CreateMonitoredItemsRequest;
import org.opcfoundation.ua.core.CreateMonitoredItemsResponse;
import org.opcfoundation.ua.core.CreateSubscriptionRequest;
import org.opcfoundation.ua.core.CreateSubscriptionResponse;
import org.opcfoundation.ua.core.DeleteSubscriptionsRequest;
import org.opcfoundation.ua.core.DeleteSubscriptionsResponse;
import org.opcfoundation.ua.core.MonitoredItemCreateRequest;
import org.opcfoundation.ua.core.MonitoringMode;
import org.opcfoundation.ua.core.MonitoringParameters;
import org.opcfoundation.ua.core.ReadValueId;
import org.opcfoundation.ua.core.TimestampsToReturn;

import ch.cern.opc.ua.clientlib.addressspace.NodeDescription;
import ch.cern.opc.ua.clientlib.notification.OPCUAAsyncUpdateCallback;
import ch.cern.opc.ua.clientlib.notification.SubscriptionNotification;
import ch.cern.opc.ua.clientlib.notification.SubscriptionNotificationHandler;

public class Subscription implements SubscriptionNotificationHandler
{
	private static final Double SAMPLING_INTERVAL = Double.valueOf(-1);
	private static final UnsignedInteger QUEUE_SIZE = UnsignedInteger.valueOf(1);
	
	private static final UnsignedByte PRIORITY = UnsignedByte.valueOf(0);
	private static final UnsignedInteger REQUESTED_LIFETIME_COUNT = UnsignedInteger.valueOf(60);
	private static final UnsignedInteger MAX_KEEP_ALIVE_COUNT = UnsignedInteger.valueOf(5);
	private static final Double PUBLISHING_INTERVAL = Double.valueOf(500);

	private final SessionChannel channel;
	private final OPCUAAsyncUpdateCallback dslCallback;

	private boolean active = false;
	private UnsignedInteger subscriptionId;
	
	private UnsignedInteger clientHandle = UnsignedInteger.ONE;
	private Map<UnsignedInteger, NodeDescription> monitoredItems = new HashMap<UnsignedInteger, NodeDescription>();
	
	protected UnsignedInteger getNextClientHandle()
	{
		if(clientHandle.equals(UnsignedInteger.MAX_VALUE))
		{
			clientHandle = UnsignedInteger.ONE;
		}
		
		clientHandle = clientHandle.inc();
		
		return clientHandle;
	}

	public Subscription(final SessionChannel channel, final OPCUAAsyncUpdateCallback dslCallback)
	{
		this.channel = channel;
		this.dslCallback = dslCallback;
		createSubscription();
	}
	
	public boolean isActive()
	{
		return active;
	}
	
	public UnsignedInteger getSubscriptionId()
	{
		return subscriptionId;
	}

	private void createSubscription()
	{
		CreateSubscriptionRequest request = new CreateSubscriptionRequest();
		request.setPublishingEnabled(Boolean.TRUE);
		request.setRequestedPublishingInterval(PUBLISHING_INTERVAL);
		request.setRequestedMaxKeepAliveCount(MAX_KEEP_ALIVE_COUNT);
		request.setRequestedLifetimeCount(REQUESTED_LIFETIME_COUNT);
		request.setPriority(PRIORITY);

		try 
		{
			logTrace("sending createsubscriptionrequest to server");
			CreateSubscriptionResponse response = channel.CreateSubscription(request);
			if(response.getResponseHeader().getServiceResult().isGood())
			{
				logTrace("received positive createsubscriptionresponse from server");
				active = true;
				subscriptionId = response.getSubscriptionId();
			}
			else
			{
				logError("received negative createsubscriptionresponse from server");
			}
		} 
		catch (ServiceFaultException e) 
		{
			logError("createsubscriptionrequest threw ["+e.getClass().getSimpleName()+"] message ["+e.getMessage()+"]");
			e.printStackTrace();
		} 
		catch (ServiceResultException e) 
		{
			logError("createsubscriptionrequest threw ["+e.getClass().getSimpleName()+"] message ["+e.getMessage()+"]");
			e.printStackTrace();
		}
	}

	public boolean addMonitoredItems(final NodeDescription... nodes)
	{
		if(isEmpty(nodes))
		{
			logError("Cannot monitor nodes via subscription - empty node list provided");
			return false;
		}
		
		if(!active) 
		{
			logError("Failed to monitor nodes, the parent subscription is not active on the server");
			return false;
		}
		
		logDebug("Adding monitor for values of ["+nodes.length+"] nodes on subscription [id: "+subscriptionId.intValue()+"]");

		CreateMonitoredItemsRequest request = new CreateMonitoredItemsRequest();
		request.setItemsToCreate(createMonitorRequests(nodes));
		request.setTimestampsToReturn(TimestampsToReturn.Source);
		request.setSubscriptionId(subscriptionId);
		
		try 
		{
			CreateMonitoredItemsResponse response = channel.CreateMonitoredItems(request);
			System.out.println(response);
			return response.getResponseHeader().getServiceResult().isGood();
		} 
		catch (ServiceFaultException e) 
		{
			e.printStackTrace();
		} 
		catch (ServiceResultException e) 
		{
			e.printStackTrace();
		}
		
		return false;
	}
	
	private MonitoredItemCreateRequest[] createMonitorRequests(final NodeDescription... nodes) 
	{
		List<MonitoredItemCreateRequest> result = new ArrayList<MonitoredItemCreateRequest>();

		if(isNotEmpty(nodes))
		{
			for(NodeDescription node : nodes)
			{
				if(node == null) continue;
				MonitoredItemCreateRequest request = new MonitoredItemCreateRequest();

				request.setItemToMonitor(createReadSpecifier(node));
				request.setMonitoringMode(MonitoringMode.Reporting);
				request.setRequestedParameters(createNodeMonitoringParameters(node));

				result.add(request);
			}
		}

		return result.toArray(new MonitoredItemCreateRequest[0]);
	}

	private MonitoringParameters createNodeMonitoringParameters(final NodeDescription node) 
	{
		final MonitoringParameters result = new MonitoringParameters();
		
		UnsignedInteger clientHandle = getNextClientHandle();
		monitoredItems.put(clientHandle, node);
		result.setClientHandle(clientHandle);
		
		result.setDiscardOldest(Boolean.TRUE);
		result.setQueueSize(QUEUE_SIZE);
		result.setSamplingInterval(SAMPLING_INTERVAL);
		
		return result;
	}

	private ReadValueId createReadSpecifier(NodeDescription node) 
	{
		System.out.println("Creating read specifier for node browse name ["+node.getBrowseName()+"] id ["+node.getNodeId()+"]");
		
		ReadValueId readSpecifier = new ReadValueId();
		readSpecifier.setNodeId(node.getNodeId());
		readSpecifier.setAttributeId(Attributes.Value);
		return readSpecifier;
	}

	/**
	 * WARNING
	 *  
	 * This method is most likely to be invoked from some 
	 * thread dedicated to handling publication responses 
	 * arriving from the server asynchronously.
	 *  
	 * @param message
	 */
	@Override
	public void handle(SubscriptionNotification notification) 
	{
		System.out.println("Thread id ["+Thread.currentThread().getId()+"] updating subscription ["+getSubscriptionId()+"] callback? ["+(dslCallback != null?"Y":"N")+"] with notification: "+notification);
		
		if(dslCallback != null)
		{
			NodeDescription dsc = monitoredItems.get(notification.getClientHandle());
			if(dsc != null)
			{
				dslCallback.onUpdate(dsc.getNodeId().toString(), "", notification);
			}
			else
			{
				logWarning("Subscription ["+getSubscriptionId()+"] received update for item with unknown client handle ["+notification.getClientHandle()+"], ignoring...");
			}
		}
		else
		{
			logWarning("No DSL handler to update with subscription notifications");
		}
	}

	public boolean delete() 
	{
		if(!active) return true;
		active = false;
		
		DeleteSubscriptionsRequest request = new DeleteSubscriptionsRequest();
		request.setSubscriptionIds(new UnsignedInteger[]{subscriptionId});
		
		try 
		{
			DeleteSubscriptionsResponse response = channel.DeleteSubscriptions(request);
			System.out.println(response);

			if(response.getResponseHeader().getServiceResult().isGood())
			{
				return true;
			}
			else
			{
				logError("received negative deletesubscriptionresponse from server");
			}
		} 
		catch (ServiceFaultException e) 
		{
			e.printStackTrace();
		} 
		catch (ServiceResultException e) 
		{
			e.printStackTrace();
		}
		
		return false;
	}

	public int getMonitoredItemCount() 
	{
		return monitoredItems.size();
	}
	
	public UnsignedInteger getClientHandle(final NodeId targetNodeId)
	{
		if(targetNodeId == null) return null;
		
		for(Entry<UnsignedInteger, NodeDescription > entry : monitoredItems.entrySet())
		{
			if(targetNodeId.equals(entry.getValue().getNodeId()))
			{
				return entry.getKey();
			}
		}
		
		return null;
	}
	
	public boolean hasClientHandle(final UnsignedInteger handle)
	{
		return monitoredItems.containsKey(handle);
	}
}
