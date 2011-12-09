package ch.cern.opc.ua.clientlib.notification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.opcfoundation.ua.builtintypes.DataValue;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.builtintypes.Variant;

public class SubscriptionNotificationTest 
{
	private final static UnsignedInteger SUBSRIPTION_ID = UnsignedInteger.valueOf(123);
	private final static UnsignedInteger CLIENT_HANDLE = UnsignedInteger.valueOf(345);
	private final static DataValue VALUE = new DataValue(new Variant(789));
	
	private SubscriptionNotification testee;
	
	@Before
	public void setup()
	{
		testee = new SubscriptionNotification(SUBSRIPTION_ID, CLIENT_HANDLE, VALUE);
	}
	
	@Test
	public void testFieldsFilledIn()
	{
		assertEquals(SUBSRIPTION_ID, testee.getSubscriptionId());
		assertEquals(CLIENT_HANDLE, testee.getClientHandle());
		assertEquals(VALUE, testee.getValue());
	}
	
	@Test
	public void testToStringHandlesNullContents()
	{
		testee = new SubscriptionNotification(null, null, null);
		try
		{
			testee.toString();
		}
		catch(NullPointerException e)
		{
			e.printStackTrace();
			fail();
		}
	}
}
