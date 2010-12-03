package ch.cern.opc.scriptRunner.results.async;

import static ch.cern.opc.scriptRunner.results.async.AssertAsyncEqualsRunResult.ASYNC_STATE.*

import static org.junit.Assert.*;
import org.junit.Test
import org.junit.Before
import groovy.mock.interceptor.*

class AssertAsyncManagerTest 
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
		testee = new AssertAsynchManager()
		asyncAssert = createMockAsyncAssert({return WAITING}, {println 'onTick called'}, {a,b->println 'checkUpdate called'})
	}
	
	@Test
	void testRegisterAsyncAssertSetsRegisteredStateOnAsyncAssertObject()
	{
		assertEquals(0, testee.registeredAsyncAssertsCount)
		testee.registerAsyncAssert(asyncAssert)
		assertEquals(1, testee.registeredAsyncAssertsCount)
	}
	
	@Test
	void testAsyncUpdateCallsAsyncAssertCheckUpdate()
	{
		def actualItem
		def actualValue
		
		def mockAsyncAssert = createMockAsyncAssert({return PASSED}, null, {item, value-> actualItem = item; actualValue=value})
		testee.registerAsyncAssert(mockAsyncAssert)
		
		testee.asyncUpdate(ITEM_PATH, EXPECTED_VALUE)
		assertEquals(ITEM_PATH, actualItem)
		assertEquals(EXPECTED_VALUE, actualValue)
	}
	
	@Test
	void testAsyncUpdateClearsPassedAsyncAsserts()
	{
		testee.with 
		{
			registerAsyncAssert(createMockAsyncAssert({return PASSED}, null, {item, value->}))
			registerAsyncAssert(createMockAsyncAssert({return PASSED}, null, {item, value->}))
			registerAsyncAssert(createMockAsyncAssert({return WAITING}, null, {item, value->}))
			registerAsyncAssert(createMockAsyncAssert({return WAITING}, null, {item, value->}))
		}
		assertEquals(4, testee.registeredAsyncAssertsCount)
		
		testee.asyncUpdate('some.item', 'some value')
		
		assertEquals(2, testee.registeredAsyncAssertsCount)
	}
	
	@Test
	void testOnTickCallsOnTickOfRegisteredAsyncAsserts()
	{
		def tickCalled = false
		
		testee.registerAsyncAssert(createMockAsyncAssert({return PASSED}, {tickCalled=true}, null))
		
		testee.onTick()
		
		assertTrue(tickCalled)

	}	
	
	@Test
	void testOnTickClearsTimedOutAsyncAsserts()
	{
		testee.with 
		{
			registerAsyncAssert(createMockAsyncAssert({return TIMED_OUT}, {}, null))
			registerAsyncAssert(createMockAsyncAssert({return WAITING}, {}, null))
			registerAsyncAssert(createMockAsyncAssert({return TIMED_OUT}, {}, null))
			registerAsyncAssert(createMockAsyncAssert({return WAITING}, {}, null))
		}
		assertEquals(4, testee.registeredAsyncAssertsCount)
		
		testee.onTick()
		
		assertEquals(2, testee.registeredAsyncAssertsCount)
	}
	
	@Test
	void testTimeoutAllRemainingAsyncAsserts()
	{
		def allAsyncAsserts = []
		
		for(i in 1..3)
		{
			allAsyncAsserts << new AssertAsyncEqualsRunResult(null, null, null, null)
		}
		allAsyncAsserts.each{testee.registerAsyncAssert(it)}

		assertEquals(3, testee.registeredAsyncAssertsCount)
		
		testee.timeoutAllRemainingAsyncAsserts()
		
		assertEquals(0, testee.registeredAsyncAssertsCount)
		allAsyncAsserts.each{assertEquals(TIMED_OUT, it.state)}
	}
	
	@Test
	void testStartTickingAndStopTicking()
	{
		def tickCalledCounter = 0
		
		def asyncAssert = createMockAsyncAssert({return WAITING}, {println 'tick';tickCalledCounter++}, null)
		testee.registerAsyncAssert(asyncAssert)
		
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
