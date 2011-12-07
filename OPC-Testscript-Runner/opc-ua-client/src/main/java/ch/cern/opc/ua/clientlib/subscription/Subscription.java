package ch.cern.opc.ua.clientlib.subscription;

import static org.apache.commons.lang3.ArrayUtils.isEmpty;
import static org.apache.commons.lang3.ArrayUtils.isNotEmpty;

import java.lang.management.MemoryNotificationInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opcfoundation.ua.application.SessionChannel;
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
import ch.cern.opc.ua.clientlib.notification.SubscriptionNotification;

public class Subscription 
{
	private static final Double SAMPLING_INTERVAL = Double.valueOf(-1);
	private static final UnsignedInteger QUEUE_SIZE = UnsignedInteger.valueOf(1);
	
	private static final UnsignedByte PRIORITY = UnsignedByte.valueOf(0);
	private static final UnsignedInteger REQUESTED_LIFETIME_COUNT = UnsignedInteger.valueOf(60);
	private static final UnsignedInteger MAX_KEEP_ALIVE_COUNT = UnsignedInteger.valueOf(5);
	private static final Double PUBLISHING_INTERVAL = Double.valueOf(500);

	private final SessionChannel channel;

	private boolean isCreated = false;
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

	public Subscription(final SessionChannel channel)
	{
		this.channel = channel;
		createSubscription();
	}
	
	public boolean isCreated()
	{
		return isCreated;
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
			CreateSubscriptionResponse response = channel.CreateSubscription(request);
			if(response.getResponseHeader().getServiceResult().isGood())
			{
				isCreated = true;
				subscriptionId = response.getSubscriptionId();
			}
			System.out.println(response.toString());
		} 
		catch (ServiceFaultException e) 
		{
			e.printStackTrace();
		} 
		catch (ServiceResultException e) 
		{
			e.printStackTrace();
		}
	}

	public boolean addMonitoredItems(final NodeDescription... nodes)
	{
		if(isEmpty(nodes))
		{
			System.err.println("Cannot monitor nodes via subscription - empty node list provided");
			return false;
		}
		
		if(!isCreated) 
		{
			System.err.println("Failed to monitor nodes, the parent subscription has not been created on the server");
			return false;
		}
		
		System.out.println("Adding monitor for values of ["+nodes.length+"] nodes on subscription [id: "+subscriptionId.intValue()+"]");

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
	public void onNotification(final SubscriptionNotification message)
	{
		System.out.println("Thread id ["+Thread.currentThread().getId()+"] updating subscription ["+getSubscriptionId()+"] with notification: "+message);
	}

	public boolean delete() 
	{
		if(!isCreated) return true;
		
		DeleteSubscriptionsRequest request = new DeleteSubscriptionsRequest();
		request.setSubscriptionIds(new UnsignedInteger[]{subscriptionId});
		
		try 
		{
			DeleteSubscriptionsResponse response = channel.DeleteSubscriptions(request);
			System.out.println(response);

			if(response.getResponseHeader().getServiceResult().isGood())
			{
				isCreated = false;
				return true;
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
}
