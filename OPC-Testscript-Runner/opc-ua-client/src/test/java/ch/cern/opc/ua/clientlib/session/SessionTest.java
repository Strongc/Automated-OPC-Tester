package ch.cern.opc.ua.clientlib.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.opcfoundation.ua.application.SessionChannel;
import org.opcfoundation.ua.builtintypes.StatusCode;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.common.ServiceFaultException;
import org.opcfoundation.ua.common.ServiceResultException;

import ch.cern.opc.ua.clientlib.subscription.Subscription;

public class SessionTest 
{
	private static final String SUBSCRIPTION_NAME = "testSubscription";
	private Session testee;
	private final static UnsignedInteger SUBSCRIPTION_ID = UnsignedInteger.valueOf(123);
	
	@Before
	public void setup()
	{
		testee = new Session(null, null);
	}
	
	@Test
	public void testCreatedSubscriptionIsRetrievableByName() throws ServiceFaultException, ServiceResultException
	{
		SessionChannel channel = new MockChannelBuilder().createSubscriptionResponse(SUBSCRIPTION_ID, StatusCode.GOOD).build();
		
		testee.setChannel(channel);
		testee.createSubscription(SUBSCRIPTION_NAME, null);
		
		assertEquals(SUBSCRIPTION_ID, testee.getSubscription(SUBSCRIPTION_NAME).getSubscriptionId());
	}
	
	@Test
	public void testCreateSubscriptionDoesNotAddSubscriptionsWhichFail() throws ServiceFaultException, ServiceResultException
	{
		SessionChannel channel = new MockChannelBuilder().createSubscriptionResponse(SUBSCRIPTION_ID, StatusCode.BAD).build();
		
		testee.setChannel(channel);
		testee.createSubscription(SUBSCRIPTION_NAME, null);
		
		assertNull(testee.getSubscription(SUBSCRIPTION_NAME));
	}
	
	@Test
	public void testCreateSubscriptionDoesNotOverwriteExistingSubscriptionWithSameName() throws ServiceFaultException, ServiceResultException
	{
		SessionChannel channel = new MockChannelBuilder().createSubscriptionResponse(SUBSCRIPTION_ID, StatusCode.GOOD).build();
		testee.setChannel(channel);

		Subscription theSubscription = testee.createSubscription(SUBSCRIPTION_NAME, null);
		Subscription anotherSubscriptionWithSameName = testee.createSubscription(SUBSCRIPTION_NAME, null);

		assertNotNull(theSubscription);
		assertSame(theSubscription, anotherSubscriptionWithSameName);
	}
	
	@Test
	public void testDeleteSubscriptionSuccessReturnsTrue() throws ServiceFaultException, ServiceResultException
	{
		SessionChannel channel = new MockChannelBuilder().
			createSubscriptionResponse(SUBSCRIPTION_ID, StatusCode.GOOD).
			deleteSubscriptionResponse(StatusCode.GOOD).
			build();
		testee.setChannel(channel);
		
		testee.createSubscription(SUBSCRIPTION_NAME, null);
		
		assertTrue(testee.deleteSubscription(SUBSCRIPTION_NAME));
	}
	
	@Test
	public void testDeleteSubscriptionFailureReturnsFalse() throws ServiceFaultException, ServiceResultException
	{
		SessionChannel channel = new MockChannelBuilder().
			createSubscriptionResponse(SUBSCRIPTION_ID, StatusCode.GOOD).
			deleteSubscriptionResponse(StatusCode.BAD).
			build();
		testee.setChannel(channel);
		
		testee.createSubscription(SUBSCRIPTION_NAME, null);
		
		assertFalse(testee.deleteSubscription(SUBSCRIPTION_NAME));
	}
	
	@Test
	public void testDeleteSubscriptionWhichDoesNotExistReturnsFalse() throws ServiceFaultException, ServiceResultException
	{
		assertNull(testee.getSubscription(SUBSCRIPTION_NAME));
		
		assertFalse(testee.deleteSubscription(SUBSCRIPTION_NAME));
	}
	
	@Test
	public void testDeleteSubscriptionRemovesItFromSession() throws ServiceFaultException, ServiceResultException
	{
		SessionChannel channel = new MockChannelBuilder().
			createSubscriptionResponse(SUBSCRIPTION_ID, StatusCode.GOOD).
			deleteSubscriptionResponse(StatusCode.GOOD).
			build();
		testee.setChannel(channel);

		// adds to map by subscription name and id
		testee.createSubscription(SUBSCRIPTION_NAME, null);
		assertNotNull(testee.getSubscription(SUBSCRIPTION_NAME));
		assertSame(testee.getSubscription(SUBSCRIPTION_NAME), testee.getSubscription(SUBSCRIPTION_ID));
		
		// removes from map any reference to the subscription object
		testee.deleteSubscription(SUBSCRIPTION_NAME);
		assertNull(testee.getSubscription(SUBSCRIPTION_NAME));
		assertNull(testee.getSubscription(SUBSCRIPTION_ID));
	}
	
	@Test
	public void testGetSubscriptionCount() throws ServiceFaultException, ServiceResultException
	{
		SessionChannel channel = new MockChannelBuilder().
			createSubscriptionResponse(SUBSCRIPTION_ID, StatusCode.GOOD).
			deleteSubscriptionResponse(StatusCode.GOOD).
			build();
	
		testee.setChannel(channel);
		
		assertEquals(0, testee.getSubscriptionCount());
		
		testee.createSubscription(SUBSCRIPTION_NAME, null);
		assertEquals(1, testee.getSubscriptionCount());
		
		testee.deleteSubscription(SUBSCRIPTION_NAME);
		assertEquals(0, testee.getSubscriptionCount());
	}
	
	@Test
	public void testDeleteLastSubscriptionStopsPublicationThread() throws ServiceFaultException, ServiceResultException
	{
		SessionChannel channel = new MockChannelBuilder().
			createSubscriptionResponse(SUBSCRIPTION_ID, StatusCode.GOOD).
			deleteSubscriptionResponse(StatusCode.GOOD).
			build();

		testee.setChannel(channel);
		
		PublicationThread mockPubThread = mock(PublicationThread.class);
		when(mockPubThread.start()).thenReturn(true);
		when(mockPubThread.stop()).thenReturn(true);
		
		testee.injectMockPublicationThread(mockPubThread);
		
		testee.createSubscription(SUBSCRIPTION_NAME, null);
		assertEquals(1, testee.getSubscriptionCount());
		verify(mockPubThread).start();

		testee.deleteSubscription(SUBSCRIPTION_NAME);
		assertEquals(0, testee.getSubscriptionCount());
		verify(mockPubThread).stop();
	}
}
