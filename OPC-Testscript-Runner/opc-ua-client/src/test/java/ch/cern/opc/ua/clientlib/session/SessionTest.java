package ch.cern.opc.ua.clientlib.session;

import static ch.cern.opc.ua.clientlib.session.SessionChannelTestUtils.createMockChannel;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.opcfoundation.ua.builtintypes.StatusCode;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.common.ServiceFaultException;
import org.opcfoundation.ua.common.ServiceResultException;

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
		testee.setChannel(createMockChannel(SUBSCRIPTION_ID, StatusCode.GOOD));
		testee.createSubscription(SUBSCRIPTION_NAME);
		
		assertEquals(SUBSCRIPTION_ID, testee.getSubscription(SUBSCRIPTION_NAME).getSubscriptionId());
	}
	
	@Test
	public void testCreateSubscriptionDoesNotAddSubscriptionsWhichFail() throws ServiceFaultException, ServiceResultException
	{
		testee.setChannel(createMockChannel(SUBSCRIPTION_ID, StatusCode.BAD));
		testee.createSubscription(SUBSCRIPTION_NAME);
		
		assertNull(testee.getSubscription(SUBSCRIPTION_NAME));
	}
}
