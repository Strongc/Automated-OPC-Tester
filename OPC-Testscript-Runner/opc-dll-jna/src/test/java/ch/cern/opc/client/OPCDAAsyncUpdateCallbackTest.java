package ch.cern.opc.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import org.junit.Before;
import org.junit.Test;

public class OPCDAAsyncUpdateCallbackTest 
{
	private boolean stopUpdateHandlerThread;
	private static final int QUEUE_CAPACITY = 5;
	private BlockingDeque<UpdateValue> updateQueue = new LinkedBlockingDeque<UpdateValue>(QUEUE_CAPACITY);
	
	OPCDAAsyncUpdateCallback testee;
	
	@Test
	public void testNothing()
	{
		assertTrue(true);
	}

	@Before
	public void setup()
	{
		updateQueue.clear();
		testee = new OPCDAAsyncUpdateCallback(updateQueue);
		stopUpdateHandlerThread = false;
	}
	
	@Test
	public void testUpdatesArePassedToQueue()
	{
		assertEquals(0, updateQueue.size());
		
		testee.onUpdate("some.test.item", "value", 2, 3, "timestamp");
		
		assertEquals(1, updateQueue.size());
	}
	
	@Test
	public void testQueuedUpdateHasCorrectContent() throws InterruptedException
	{
		testee.onUpdate("some.test.item", "value", 2, 3, "timestamp");

		UpdateValue update = updateQueue.takeFirst();
		assertEquals("some.test.item", update.itemPath);
		assertEquals("value", update.value);
		assertEquals(2, update.quality);
		assertEquals(3, update.type);
		assertEquals("timestamp", update.timestamp);
	}
	
	@Test
	public void testMultipleQueuedUpdates() throws InterruptedException
	{
		testee.onUpdate("item1", "value1", 1, 1, "timestamp1");
		testee.onUpdate("item2", "value2", 2, 2, "timestamp2");
		testee.onUpdate("item3", "value3", 3, 3, "timestamp3");
		
		assertEquals("item1", updateQueue.takeFirst().itemPath);
		assertEquals("item2", updateQueue.takeFirst().itemPath);
		assertEquals("item3", updateQueue.takeFirst().itemPath);
	}
	
	@Test
	public void testRemovalOfQueuedUpdates() throws InterruptedException
	{
		// fill queue
		for(int i=0; i<QUEUE_CAPACITY; i++)
		{
			testee.onUpdate("", "", 1, 1, "");
		}
		
		// empty queue
		for(int i=0; i<QUEUE_CAPACITY; i++)
		{
			updateQueue.takeFirst();
		}
		
		assertEquals(0, updateQueue.size());
	}
	
	@Test 
	public void testUpdateReturns_1_IfUpdateQueued()
	{
		for(int i=0; i<QUEUE_CAPACITY; i++)
		{
			int result = testee.onUpdate("item", "value", 1, 1, "timestamp");
			assertEquals(1, result);
		}
	}
	
	@Test 
	public void testUpdateReturns_0_IfUpdateCouldNotBeQueued()
	{
		// fill queue
		for(int i=0; i<QUEUE_CAPACITY; i++)
		{
			testee.onUpdate("item", "value", 1, 1, "timestamp");
		}
		
		// add one more
		int result = testee.onUpdate("item", "value", 1, 1, "timestamp");
		assertEquals(0, result);
	}
	
	/**
	 * In real-life updates added into the update queue will be handled by a seperate
	 * thread like this...
	 * @throws InterruptedException 
	 */
	@Test
	public void testUpdatesHandledBySeparateThread() throws InterruptedException
	{
		final int queueOverloadFactor = 100;
		final List<UpdateValue> handledUpdates = new ArrayList<UpdateValue>();
		
    	Runnable updatesHandler = new Runnable()
    	{
			@Override
			public void run() 
			{
				while(!stopUpdateHandlerThread)
				{
					try 
					{
						handledUpdates.add(updateQueue.takeFirst());
					} 
					catch (InterruptedException e) 
					{
						fail("Update handler thread unexpectedly interrupted");
					} 
				}
				System.out.println("UpdateHandler thread stopped");
			}
    	};
    	
    	Thread updatesHandlerThread = new Thread(updatesHandler);
		
    	updatesHandlerThread.start();
    	
    	// past queue capacity - queue should correctly marshal threads for putting/getting
		for(int i=0; i<QUEUE_CAPACITY * queueOverloadFactor; i++)
		{
			int result = testee.onUpdate("item", "value", 1, 1, "timestamp");
			assertEquals(1, result);
		}
		
		// expect all updates to arrive PDQ.
		for(int retry = 0; retry < 10; retry++)
		{
			if(handledUpdates.size() == QUEUE_CAPACITY * 10) break;
			Thread.sleep(50);
		}
		
		stopUpdateHandlerThread = true;

		assertEquals(QUEUE_CAPACITY * queueOverloadFactor, handledUpdates.size());
	}
}
