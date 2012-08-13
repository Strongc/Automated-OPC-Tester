package ch.cern.opc.da.dsl.async

import static org.junit.Assert.*
import ch.cern.opc.client.OPCDAClientApi
import ch.cern.opc.client.OPCDAClientInstance
import ch.cern.opc.client.Update
import ch.cern.opc.dsl.common.client.UpdateHandler
import org.junit.Test
import org.junit.Before

class OPCDAUpdateHandlerTest 
{
	private def rcvdItemId
	private def rcvdAttributeId
	private def rcvdValue
	private def rcvdQuality
	private def rcvdType
	private def rcvdTimestamp
	
	private OPCDAUpdateHandler testee
	
	@Before
	void setup()
	{
		rcvdAttributeId = null
		rcvdItemId = null
		rcvdValue = null
		rcvdQuality = null
		rcvdType = null
		rcvdTimestamp = null
		
		def updateHandler  = 
		[
			onUpdate:{itemPath, attributeId, value, quality, type, timestamp->
				rcvdItemId = itemPath
				rcvdAttributeId = attributeId
				rcvdValue = value
				rcvdQuality = quality
				rcvdType = type
				rcvdTimestamp = timestamp}
		] as UpdateHandler;
			
		testee = new OPCDAUpdateHandler(updateHandler)
	}
	
	@Test
	void testOnUpdateWithNullPath()
	{
		def result = testee.onUpdate(createUpdate(null, "123", 192, 8, "timestamp"))
		assertEquals(0, result)
		assertNull(rcvdItemId)
		assertNull(rcvdValue)
	}
	
	@Test
	void testOnUpdateWithNullValue()
	{
		def result = testee.onUpdate(createUpdate("item.path", null, 192, 8, "timestamp"))
		assertEquals(0, result)
		assertNull(rcvdItemId)
		assertNull(rcvdValue)
	}
	
	@Test
	void testOnUpdateWithNonNullParameters()
	{
		assertNull(rcvdItemId)
		assertNull(rcvdValue)
		
		def result = testee.onUpdate(createUpdate("item.path", "123", 192, 8, "timestamp"))
		assertEquals(1, result)
		assertEquals("item.path", rcvdItemId)
		assertNull(rcvdAttributeId)
		assertEquals("123", rcvdValue)
	}
	
	Update createUpdate(itemPath, value, quality, type, timestamp)
	{
		def update = new Update()
		
		update.itemPath = itemPath
		update.attributeId = 'INVALID_ATTRIBUTE_ID'
		update.value = value
		update.quality = quality
		update.type = type
		update.timestamp = timestamp
		
		return update
	}
}
