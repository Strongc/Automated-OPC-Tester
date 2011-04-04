package ch.cern.opc.scriptRunner.results.async;

import static org.junit.Assert.*;
import org.junit.Before
import org.junit.Test
import groovy.mock.interceptor.*

class AsyncTickerTest 
{
	def testee
	
	@Before
	void setup()
	{
		testee = new AsyncTicker() 
	}
	
	@Test
	void testStartStartsTimer()
	{
		def tickCalled = 0
		
		def mockManager = new MockFor(AsyncConditionManager)
		mockManager.demand.onTick(0..4){println 'onTick called'; tickCalled++}
		
		mockManager.use
		{
			testee.start(new AsyncConditionManager())
			sleep(3500) // use a 0.5s margin of error for timer.
			testee.stop()
		}
		
		assertEquals('expected tick to be called 4 times - immediately then once per second subsequently', 
			4, tickCalled)
	}	
}
