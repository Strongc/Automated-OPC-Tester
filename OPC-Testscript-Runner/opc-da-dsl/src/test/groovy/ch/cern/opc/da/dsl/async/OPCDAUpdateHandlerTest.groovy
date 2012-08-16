package ch.cern.opc.da.dsl.async

import static org.junit.Assert.*


import ch.cern.opc.client.UpdateValue
import ch.cern.opc.dsl.common.client.UpdateHandler
import org.junit.*
import static ch.cern.opc.common.Log.*

class OPCDAUpdateHandlerTest 
{
	class RcvdUpdate
	{
		String itemId
		String attrId
		String value
		int quality
		int type
		String timestamp
	}

	def rcvdUpdates = []
	
	private OPCDAUpdateHandler testee
	
	@Before
	void setup()
	{
		rcvdUpdates = []
		
		def updateHandler  = 
		[
			onUpdate:{itemId, attributeId, value, quality, type, timestamp->
				System.out.println("item [${itemId}] att [${attributeId}] val [${value}] quality [${quality}] type [${type}] ts [${timestamp}]")
				
				def update = new RcvdUpdate()
				update.itemId = itemId
				update.attrId = attributeId
				update.value = value
				update.quality = quality
				update.type = type
				update.timestamp = timestamp
				
				rcvdUpdates.add(update)
		}
		] as UpdateHandler

		testee = new OPCDAUpdateHandler(updateHandler)
		testee.startUpdaterThread()
	}
	
	@After
	void teardown()
	{
		testee.stopUpdaterThread()
	}
	
	@Test
	void testMultipleUpdates()
	{
		pushUpdate("item1", null, 0, 0, null)
		pushUpdate("item2", null, 0, 0, null)
		pushUpdate("item3", null, 0, 0, null)
		
		waitForUpdates(3)
		assertEquals(3, rcvdUpdates.size())
		
		assertEquals("item1", rcvdUpdates[0].itemId)
		assertEquals("item2", rcvdUpdates[1].itemId)
		assertEquals("item3", rcvdUpdates[2].itemId)
	}
	
	@Test
	void testOnUpdateWithNullPath()
	{
		pushUpdate(null, "123", 192, 8, "timestamp")
		waitForUpdates()

		assertEquals(1, rcvdUpdates.size())
		def update = rcvdUpdates[0]
		
		assertNull(update.itemId)
		assertEquals("123", update.value)
	}
	
	@Test
	void testOnUpdateWithNullValue()
	{
		pushUpdate("item.path", null, 192, 8, "timestamp")
		waitForUpdates()
		
		assertEquals(1, rcvdUpdates.size())
		def update = rcvdUpdates[0]

		assertEquals("item.path", update.itemId)
		assertNull(update.value)
	}
	
	@Test
	void testOnUpdateWithNonNullParameters()
	{
		pushUpdate("item.path", "123", 192, 8, "timestamp")
		waitForUpdates()
		
		assertEquals(1, rcvdUpdates.size())
		def update = rcvdUpdates[0]

		assertEquals("item.path", update.itemId)
		assertEquals("123", update.value)
	}
	
	private def waitForUpdates(def numberOfUpdates = 1)
	{
		final def MAX_WAIT_MS = 100
		final def SNOOZE_MS = 10
		 
		for(def elapsedWait = 0; elapsedWait < MAX_WAIT_MS; elapsedWait += SNOOZE_MS)
		{
			if(numberOfUpdates == rcvdUpdates.size()) return
			Thread.sleep(SNOOZE_MS);
		}
		
		fail("failed to receive the required number of updates [${numberOfUpdates}] within timeout [${MAX_WAIT_MS}]")
	}
	
	//"item.path", "123", 192, 8, "timestamp"
	private def pushUpdate(path, value, quality, type, timestamp)
	{
		testee.updatesQueue.offerLast(new UpdateValue(path, '', value, quality, type, timestamp))
	}

}
