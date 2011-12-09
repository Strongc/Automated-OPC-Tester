package ch.cern.opc.ua.dsl.async

import org.opcfoundation.ua.builtintypes.DateTime
import org.opcfoundation.ua.builtintypes.StatusCode
import org.opcfoundation.ua.builtintypes.UnsignedShort
import org.opcfoundation.ua.builtintypes.Variant
import org.opcfoundation.ua.builtintypes.UnsignedInteger
import org.opcfoundation.ua.builtintypes.DataValue

import static org.junit.Assert.*
import org.junit.Test
import org.junit.Before

import ch.cern.opc.dsl.common.client.UpdateHandler
import ch.cern.opc.ua.clientlib.notification.SubscriptionNotification
import ch.cern.opc.ua.dsl.NodeTest;

class OPCUAUpdateHandlerTest 
{

	static final SUBSCRIPTION_ID = new UnsignedInteger(100)
	static final CLIENT_HANDLE = new UnsignedInteger(200)
	static final NODE_ID = "ns:namespace;s=nodeId"
	static final ATTR_ID = "attribute"
	static final VALUE = "a string value"
	static final NOTIFICATION = createSubscriptionNotification(VALUE)
		
	private def rcvdItemId
	private def rcvdAttributeId
	private def rcvdValue
	private def wasUpdateInvoked

	private def testee
	
	private def createMockedDslHandler()
	{
		rcvdAttributeId = null
		rcvdItemId = null
		rcvdValue = null
		
		wasUpdateInvoked = false
		
		def result = [
			onUpdate:{itemId, attributeId, value->
				wasUpdateInvoked = true
				rcvdItemId = itemId
				rcvdAttributeId = attributeId
				rcvdValue = value}
		] as UpdateHandler;
	
		return result
	}
	
	@Before
	void setup()
	{
		def dslHandler = createMockedDslHandler()
		testee = new OPCUAUpdateHandler(dslHandler)
	} 
	
	@Test
	void testOnUpdateWithNullNodeIdDoesNotUpdateDsl()
	{
		testee.onUpdate(null, ATTR_ID, NOTIFICATION)
		assertFalse(wasUpdateInvoked)
	}
	
	@Test
	void testOnUpdateWithNullAttributeIdDoesNotUpdateDsl()
	{
		testee.onUpdate(NODE_ID, null, NOTIFICATION)
		assertFalse(wasUpdateInvoked)
	}

	@Test
	void testOnUpdateWithNullValueDoesNotUpdateDsl()
	{
		testee.onUpdate(NODE_ID, ATTR_ID, null)
		assertFalse(wasUpdateInvoked)
	}

	@Test
	void testOnUpdateEmptyValueDoesNotUpdateDsl()
	{
		testee.onUpdate(NODE_ID, ATTR_ID, new SubscriptionNotification(null, null, null))
		assertFalse(wasUpdateInvoked)
	}
	
	@Test
	void testOnUpdateEmptyDataValueUpdatesWithNull()
	{
		def emptyDataValue = new DataValue((Variant)null)
		def notification = new SubscriptionNotification(SUBSCRIPTION_ID, CLIENT_HANDLE, emptyDataValue)

		println(notification)
		testee.onUpdate(NODE_ID, ATTR_ID, notification)
		assertRcvdUpdate(NODE_ID, ATTR_ID, "null")
	}
	
	@Test
	void testOnUpdatePassesValuesToDsl()
	{
		testee.onUpdate(NODE_ID, ATTR_ID, NOTIFICATION)
		assertRcvdUpdate(NODE_ID, ATTR_ID, VALUE)
	}
	
	private def assertRcvdUpdate(expectedItemId, expectedAttributeId, expectedValue)
	{
		assertEquals(expectedItemId, rcvdItemId)
		assertEquals(expectedAttributeId, rcvdAttributeId)
		assertEquals(expectedValue, rcvdValue)
	}
	
	private static def createSubscriptionNotification(value)
	{
		def dataValue = new DataValue(
			new Variant(value),
			StatusCode.GOOD,
			new DateTime(),
			UnsignedShort.ZERO,
			new DateTime(),
			UnsignedShort.ZERO)
		
		def notification = new SubscriptionNotification(
			SUBSCRIPTION_ID,
			CLIENT_HANDLE,
			dataValue)

		return notification;
	}
}
