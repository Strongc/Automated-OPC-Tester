package ch.cern.opc.ua.clientlib.subscription;

import static ch.cern.opc.ua.clientlib.session.SessionChannelTestUtils.createMockChannel;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.opcfoundation.ua.builtintypes.StatusCode;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.common.ServiceFaultException;
import org.opcfoundation.ua.common.ServiceResultException;

public class SubscriptionTest 
{
	private final static UnsignedInteger SUBSCRIPTION_ID = UnsignedInteger.valueOf(123);
	
	private Subscription testee;
	
	@Before
	public void setup() throws ServiceFaultException, ServiceResultException
	{
		testee = new Subscription(createMockChannel(SUBSCRIPTION_ID, StatusCode.GOOD));
	}
	
	@Test
	public void testIsCreatedForCreationScenarios() throws ServiceFaultException, ServiceResultException
	{
		assertTrue(new Subscription(createMockChannel(SUBSCRIPTION_ID, StatusCode.GOOD)).isCreated());
		assertFalse(new Subscription(createMockChannel(SUBSCRIPTION_ID, StatusCode.BAD)).isCreated());
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

}
