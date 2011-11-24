package ch.cern.opc.ua.clientlib.session;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;
import org.opcfoundation.ua.application.SessionChannel;
import org.opcfoundation.ua.builtintypes.StatusCode;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.common.ServiceFaultException;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.CreateSubscriptionRequest;
import org.opcfoundation.ua.core.CreateSubscriptionResponse;
import org.opcfoundation.ua.core.ResponseHeader;
import static ch.cern.opc.ua.clientlib.session.SessionChannelTestUtils.*;

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
	public void testNothingMuch()
	{
		assertNotNull(testee);
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
/*
	private static SessionChannel createMockChannel(final StatusCode result) throws ServiceFaultException, ServiceResultException 
	{
		SessionChannel channel = mock(SessionChannel.class);
		
		ResponseHeader header = new ResponseHeader();
		header.setServiceResult(result);
		
		CreateSubscriptionResponse response = new CreateSubscriptionResponse();
		response.setResponseHeader(header);
		response.setSubscriptionId(SUBSCRIPTION_ID);

		when(channel.CreateSubscription(any(CreateSubscriptionRequest.class))).thenReturn(response);
		return channel;
	}
*/	
}
