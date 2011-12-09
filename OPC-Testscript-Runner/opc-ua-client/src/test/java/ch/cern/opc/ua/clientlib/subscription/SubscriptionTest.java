package ch.cern.opc.ua.clientlib.subscription;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.opcfoundation.ua.application.SessionChannel;
import org.opcfoundation.ua.builtintypes.DataValue;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.StatusCode;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.common.ServiceFaultException;
import org.opcfoundation.ua.common.ServiceResultException;

import ch.cern.opc.ua.clientlib.addressspace.NodeDescription;
import ch.cern.opc.ua.clientlib.notification.MockUaDslCallback;
import ch.cern.opc.ua.clientlib.notification.SubscriptionNotification;
import ch.cern.opc.ua.clientlib.session.MockChannelBuilder;

public class SubscriptionTest 
{
	private final static UnsignedInteger SUBSCRIPTION_ID = UnsignedInteger.valueOf(123);

	MockUaDslCallback mockCallback;
	private Subscription testee;

	@Before
	public void setup() throws ServiceFaultException, ServiceResultException
	{
		SessionChannel channel = new MockChannelBuilder().
		createSubscriptionResponse(SUBSCRIPTION_ID, StatusCode.GOOD).
		deleteSubscriptionResponse(StatusCode.GOOD).
		createMonitoredItemsResponse(StatusCode.GOOD).
		build();

		mockCallback = new MockUaDslCallback();
		testee = new Subscription(channel, mockCallback);
	}

	@Test
	public void testIsActiveForCreationScenarios() throws ServiceFaultException, ServiceResultException
	{
		SessionChannel goodChannel = new MockChannelBuilder().createSubscriptionResponse(SUBSCRIPTION_ID, StatusCode.GOOD).build();
		assertTrue(new Subscription(goodChannel, null).isActive());

		SessionChannel badChannel = new MockChannelBuilder().createSubscriptionResponse(SUBSCRIPTION_ID, StatusCode.BAD).build();
		assertFalse(new Subscription(badChannel, null).isActive());
	}

	@Test
	public void testGetSubscriptionId()
	{
		assertEquals(SUBSCRIPTION_ID, testee.getSubscriptionId());
	}

	@Test
	public void testGetNextClientHandleIncrements()
	{
		UnsignedInteger previousClientHandle = testee.getNextClientHandle();

		for(int i=0; i<10; i++)
		{
			UnsignedInteger nextClientHandle = testee.getNextClientHandle();
			assertEquals(UnsignedInteger.ONE, nextClientHandle.subtract(previousClientHandle));
			previousClientHandle = nextClientHandle;
		}
	}

	@Test
	public void testDeleteSubscription() throws ServiceFaultException, ServiceResultException
	{
		assertTrue(testee.isActive());

		testee.delete();

		assertFalse(testee.isActive());
	}

	@Test
	public void testAddMonitoredItemIncreasesMonitoredItemCount()
	{
		assertEquals(0, testee.getMonitoredItemCount());

		testee.addMonitoredItems(new NodeDescription("node1", new NodeId(1, "1")));

		assertEquals(1, testee.getMonitoredItemCount());

		testee.addMonitoredItems(new NodeDescription("node2", new NodeId(2, "2")));

		assertEquals(2, testee.getMonitoredItemCount());

		testee.addMonitoredItems(
				new NodeDescription("node3", new NodeId(3, "3")),
				new NodeDescription("node4", new NodeId(4, "4")),
				new NodeDescription("node5", new NodeId(5, "5")));

		assertEquals(5, testee.getMonitoredItemCount());
	}

	@Test
	public void testAddMultipleNodes()
	{
		assertEquals(0, testee.getMonitoredItemCount());

		testee.addMonitoredItems(
				new NodeDescription("node1", new NodeId(1, "1")),
				new NodeDescription("node2", new NodeId(4, "2")),
				new NodeDescription("node3", new NodeId(5, "3")));

		assertEquals(3, testee.getMonitoredItemCount());

	}

	@Test
	public void testGetClientHandle()
	{
		NodeId nodeId = new NodeId(1, "1");
		assertNull(testee.getClientHandle(nodeId));

		testee.addMonitoredItems(new NodeDescription("node1", nodeId));

		assertNotNull(testee.getClientHandle(nodeId));
	}
	
	@Test
	public void testGetClientHandleForInvalidId()
	{
		NodeId nodeId = new NodeId(666, "invalid");
		assertNull(testee.getClientHandle(nodeId));		
	}

	@Test
	public void testHandleUpdatesTheDslCallback()
	{
		NodeId nodeId = new NodeId(1, "1");
		testee.addMonitoredItems(new NodeDescription("node1", nodeId));

		UnsignedInteger clientHandle = testee.getClientHandle(nodeId);

		SubscriptionNotification notification = new SubscriptionNotification(SUBSCRIPTION_ID, clientHandle, new DataValue());

		testee.handle(notification);

		assertEquals(nodeId.toString(), mockCallback.rcvdNodeId);
		assertEquals(notification, mockCallback.rcvdNotification);
	}

	@Test
	public void testHandleCopesWithNullClientHandles()
	{
		SubscriptionNotification notification = new SubscriptionNotification(SUBSCRIPTION_ID, null, new DataValue());
		try
		{
			testee.handle(notification);
		}
		catch(NullPointerException e)
		{
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void testHandleCopesWithUnknownClientHandles()
	{
		UnsignedInteger invalidClientHandle = new UnsignedInteger(666);
		assertFalse(testee.hasClientHandle(invalidClientHandle));
		
		SubscriptionNotification notification = new SubscriptionNotification(SUBSCRIPTION_ID, invalidClientHandle, new DataValue());

		testee.handle(notification);

		assertFalse(mockCallback.wasInvoked);
	}
	
}
