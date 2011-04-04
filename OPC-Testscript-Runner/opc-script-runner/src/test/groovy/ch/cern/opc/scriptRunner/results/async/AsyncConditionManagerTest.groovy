package ch.cern.opc.scriptRunner.results.async;

import static ch.cern.opc.scriptRunner.results.async.AssertAsyncEqualsRunResult.ASYNC_STATE.*

import static org.junit.Assert.*;
import org.junit.Test
import org.junit.Before
import groovy.mock.interceptor.*

class AsyncConditionManagerTest 
{
	private final static DEFAULT_TIMEOUT = 10
	
	private final static ITEM = 'itemPathKey'
	private final static EXPECTED = 'expectedValueKey'
	
	private final static def ITEM_PATH = 'path.to.test.item'
	private final static def EXPECTED_VALUE = 'expected'
	
	private def testee
	private def asyncAssert
	
	@Before
	void setup()
	{
		testee = new AsyncConditionManager()
		asyncAssert = createMockAsyncAssert({return WAITING}, {println 'onTick called'}, {a,b->println 'checkUpdate called'})
	}
	
	@Test
	void testRegisterAsyncConditionIncreasesRegisterAsyncConditionCount()
	{
		assertEquals(0, testee.registeredAsyncConditionsCount)
		
		testee.registerAsyncCondition(asyncAssert)

		assertEquals(1, testee.registeredAsyncConditionsCount)
	}
	
	@Test
	void testAsyncUpdateCallsAsyncAssertCheckUpdate()
	{
		def actualItem
		def actualValue
		
		def mockAsyncAssert = createMockAsyncAssert({return PASSED}, null, {item, value-> actualItem = item; actualValue=value})
		testee.registerAsyncCondition(mockAsyncAssert)
		
		testee.asyncUpdate(ITEM_PATH, EXPECTED_VALUE)
		assertEquals(ITEM_PATH, actualItem)
		assertEquals(EXPECTED_VALUE, actualValue)
	}
	
	@Test
	void testAsyncUpdateClearsPassedAsyncConditions()
	{
		testee.with 
		{
			registerAsyncCondition(createMockAsyncAssert({return PASSED}, null, {item, value->}))
			registerAsyncCondition(createMockAsyncAssert({return PASSED}, null, {item, value->}))
			registerAsyncCondition(createMockAsyncAssert({return WAITING}, null, {item, value->}))
			registerAsyncCondition(createMockAsyncAssert({return WAITING}, null, {item, value->}))
		}
		assertEquals(4, testee.registeredAsyncConditionsCount)
		
		testee.asyncUpdate('some.item', 'some value')
		
		assertEquals(2, testee.registeredAsyncConditionsCount)
	}
	
	@Test
	void testOnTickCallsOnTickOfRegisteredAsyncConditions()
	{
		def tickCalled = false
		
		testee.registerAsyncCondition(createMockAsyncAssert({return PASSED}, {tickCalled=true}, null))
		
		testee.onTick()
		
		assertTrue(tickCalled)

	}	
	
	@Test
	void testOnTickClearsTimedOutAsyncConditions()
	{
		testee.with 
		{
			registerAsyncCondition(createMockAsyncAssert({return TIMED_OUT}, {}, null))
			registerAsyncCondition(createMockAsyncAssert({return WAITING}, {}, null))
			registerAsyncCondition(createMockAsyncAssert({return TIMED_OUT}, {}, null))
			registerAsyncCondition(createMockAsyncAssert({return WAITING}, {}, null))
		}
		assertEquals(4, testee.registeredAsyncConditionsCount)
		
		testee.onTick()
		
		assertEquals(2, testee.registeredAsyncConditionsCount)
	}
	
	@Test
	void testTimeoutAllRemainingAsyncConditions()
	{
		def allAsyncConditions = []
		
		for(i in 1..3)
		{
			allAsyncConditions << new AssertAsyncEqualsRunResult(null, null, null, null)
		}
		allAsyncConditions.each{testee.registerAsyncCondition(it)}

		assertEquals(3, testee.registeredAsyncConditionsCount)
		
		testee.timeoutAllRemainingAsyncConditions()
		
		assertEquals(0, testee.registeredAsyncConditionsCount)
		allAsyncConditions.each{assertEquals(TIMED_OUT, it.state)}
	}
	
	@Test
	void testStartTickingAndStopTicking()
	{
		def tickCalledCounter = 0
		
		def asyncAssert = createMockAsyncAssert({return WAITING}, {println 'tick';tickCalledCounter++}, null)
		testee.registerAsyncCondition(asyncAssert)
		
		testee.startTicking()
		sleep(3500) //3 should be sufficient - .5 extra margin for error
		testee.stopTicking()
		
		assertEquals('4 - immediately, then on each subsequent second', 4, tickCalledCounter)
	}
	
	private def createMockAsyncAssert(getStateClosure, onTickClosure, checkUpdateClosure)
	{
		def mockAsyncAssert = new StubFor(AssertAsyncEqualsRunResult)
		mockAsyncAssert.with 
		{
			demand.getState(0..5, getStateClosure)
			demand.onTick(0..5, onTickClosure)
			demand.checkUpdate(0..5, checkUpdateClosure) 
		}

		def result
		mockAsyncAssert.use
		{
			result = new AssertAsyncEqualsRunResult(null, null, null, null)
		}
		return result
	}
}
