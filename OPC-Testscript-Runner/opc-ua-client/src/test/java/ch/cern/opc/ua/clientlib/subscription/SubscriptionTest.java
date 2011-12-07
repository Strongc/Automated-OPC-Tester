package ch.cern.opc.ua.clientlib.subscription;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.opcfoundation.ua.application.SessionChannel;
import org.opcfoundation.ua.builtintypes.StatusCode;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.common.ServiceFaultException;
import org.opcfoundation.ua.common.ServiceResultException;

import ch.cern.opc.ua.clientlib.session.MockChannelBuilder;

public class SubscriptionTest 
{
	private final static UnsignedInteger SUBSCRIPTION_ID = UnsignedInteger.valueOf(123);

	private Subscription testee;

	@Before
	public void setup() throws ServiceFaultException, ServiceResultException
	{
		SessionChannel channel = new MockChannelBuilder().createSubscriptionResponse(SUBSCRIPTION_ID, StatusCode.GOOD).build();
		testee = new Subscription(channel);
	}

	@Test
	public void testIsCreatedForCreationScenarios() throws ServiceFaultException, ServiceResultException
	{
		SessionChannel goodChannel = new MockChannelBuilder().createSubscriptionResponse(SUBSCRIPTION_ID, StatusCode.GOOD).build();
		assertTrue(new Subscription(goodChannel).isCreated());

		SessionChannel badChannel = new MockChannelBuilder().createSubscriptionResponse(SUBSCRIPTION_ID, StatusCode.BAD).build();
		assertFalse(new Subscription(badChannel).isCreated());
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
		SessionChannel channel = new MockChannelBuilder().
		createSubscriptionResponse(SUBSCRIPTION_ID, StatusCode.GOOD).
		deleteSubscriptionResponse(StatusCode.GOOD).
		build();
		
		testee = new Subscription(channel);

		assertTrue(testee.delete());
		assertFalse(testee.isCreated());
	}

}
