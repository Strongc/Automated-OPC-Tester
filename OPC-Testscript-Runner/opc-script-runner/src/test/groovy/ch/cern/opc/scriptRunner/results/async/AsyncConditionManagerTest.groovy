package ch.cern.opc.scriptRunner.results.async;

import static ch.cern.opc.scriptRunner.results.async.AssertAsyncRunResult.ASYNC_STATE.WAITING
import static ch.cern.opc.scriptRunner.results.async.AssertAsyncRunResult.ASYNC_STATE.MATCHED
import static ch.cern.opc.scriptRunner.results.async.AssertAsyncRunResult.ASYNC_STATE.TIMED_OUT
import static ch.cern.opc.scriptRunner.results.async.AssertAsyncRunResult.ASYNC_STATE.CREATED

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
		
		def mockAsyncAssert = createMockAsyncAssert({return MATCHED}, null, {item, value-> actualItem = item; actualValue=value})
		testee.registerAsyncCondition(mockAsyncAssert)
		
		testee.asyncUpdate(ITEM_PATH, EXPECTED_VALUE)
		assertEquals(ITEM_PATH, actualItem)
		assertEquals(EXPECTED_VALUE, actualValue)
	}
	
	@Test
	void testAsyncUpdateClearsMatchedAsyncConditions()
	{
		testee.with 
		{
			registerAsyncCondition(createMockAsyncAssert({return MATCHED}, null, {item, value->}))
			registerAsyncCondition(createMockAsyncAssert({return MATCHED}, null, {item, value->}))
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
		
		testee.registerAsyncCondition(createMockAsyncAssert({return MATCHED}, {tickCalled=true}, null))
		
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
	void testStartTickingAndStopTicking()
	{
		def tickCalledCounter = 0
		
		def asyncAssert = createMockAsyncAssert({return WAITING}, {println 'tick';tickCalledCounter++}, null)
		testee.registerAsyncCondition(asyncAssert)
		
		testee.startTicking()
		sleep(3500) //3s should be sufficient - .5 extra margin for error
		testee.stopTicking()
		
		assertEquals('4 - immediately, then on each subsequent second', 4, tickCalledCounter)
	}
	
	@Test
	void testMaxConditionTimeout_isZeroForNoRegisteredAsyncConditions()
	{
		assertEquals(0, testee.registeredAsyncConditionsCount)
		assertEquals(0, testee.maxConditionTimeout)
	}
	
	@Test
	void testMaxTimeout_returnsMaxTimeoutOfAllConditions()
	{
		addAsyncCondition(2, WAITING)
		addAsyncCondition(3, WAITING)
		addAsyncCondition(1, WAITING)
		
		assertEquals(3, testee.registeredAsyncConditionsCount)
		assertEquals(3, testee.maxConditionTimeout)
	}
	
	@Test
	void testMaxTimeout_returnsMaxTimeoutOfOnlyWaitingConditions()
	{
		addAsyncCondition(2, CREATED)
		addAsyncCondition(3, TIMED_OUT)
		addAsyncCondition(1, WAITING)
		addAsyncCondition(4, MATCHED)
		
		assertEquals(4, testee.registeredAsyncConditionsCount)
		assertEquals(1, testee.maxConditionTimeout)
	}
	
	private def addAsyncCondition(timeout, state)
	{
		def condition =  new AssertAsyncEqualsRunResult(null, timeout, null, null)
		condition.registerWithManager(testee)
		condition.state = state
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
