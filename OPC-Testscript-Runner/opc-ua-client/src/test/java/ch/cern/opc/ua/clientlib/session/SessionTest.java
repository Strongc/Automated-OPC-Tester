package ch.cern.opc.ua.clientlib.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

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
		testee.createSubscription(SUBSCRIPTION_NAME);
		
		assertEquals(SUBSCRIPTION_ID, testee.getSubscription(SUBSCRIPTION_NAME).getSubscriptionId());
	}
	
	@Test
	public void testCreateSubscriptionDoesNotAddSubscriptionsWhichFail() throws ServiceFaultException, ServiceResultException
	{
		SessionChannel channel = new MockChannelBuilder().createSubscriptionResponse(SUBSCRIPTION_ID, StatusCode.BAD).build();
		
		testee.setChannel(channel);
		testee.createSubscription(SUBSCRIPTION_NAME);
		
		assertNull(testee.getSubscription(SUBSCRIPTION_NAME));
	}
	
	@Test
	public void testCreateSubscriptionDoesNotOverwriteExistingSubscriptionWithSameName() throws ServiceFaultException, ServiceResultException
	{
		/**
		 * mock channel needs to return everything_went_fine response
		 */
		SessionChannel channel = new MockChannelBuilder().createSubscriptionResponse(SUBSCRIPTION_ID, StatusCode.GOOD).build();
		testee.setChannel(channel);

		Subscription theSubscription = testee.createSubscription(SUBSCRIPTION_NAME);
		Subscription anotherSubscriptionWithSameName = testee.createSubscription(SUBSCRIPTION_NAME);

		assertNotNull(theSubscription);
		assertSame(theSubscription, anotherSubscriptionWithSameName);
	}
}
