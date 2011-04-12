package ch.cern.opc.scriptRunner.results.async

import static org.junit.Assert.*
import org.apache.commons.lang.NotImplementedException;
import org.junit.Test
import org.junit.Before
import groovy.mock.interceptor.*

import static ch.cern.opc.scriptRunner.results.async.AssertAsyncRunResult.ASYNC_STATE.CREATED as CREATED
import static ch.cern.opc.scriptRunner.results.async.AssertAsyncRunResult.ASYNC_STATE.WAITING as WAITING
import static ch.cern.opc.scriptRunner.results.async.AssertAsyncRunResult.ASYNC_STATE.TIMED_OUT as TIMED_OUT
import static ch.cern.opc.scriptRunner.results.async.AssertAsyncRunResult.ASYNC_STATE.MATCHED as MATCHED

class AssertAsyncRunResultTest 
{
	def testee
	
	@Before
	void setup()
	{
		testee = new NonAbstractAssertAsyncRunResult(10, null)
	}
	
	@Test
	void testCtorCreatesObjectInCreatedState()
	{
		assertEquals(CREATED, testee.state)
	}

	@Test
	void testSetStateGetState()
	{
		testee.state = WAITING
		assertEquals(WAITING, testee.state)
		
		testee.state = TIMED_OUT
		assertEquals(TIMED_OUT, testee.state)
		
		testee.state = MATCHED
		assertEquals(MATCHED, testee.state)
	}

	@Test
	void testElapsedWaitInitialisedToZero()
	{
		assertEquals(0, testee.elapsedWait)
	}
	
	@Test
	void testElapsedWaitIncremenetedOnEachTick()
	{
		assertEquals(0, testee.elapsedWait)
		
		for(i in 1..10)
		{
			testee.onTick()
			assertEquals(i, testee.elapsedWait)
		}
	}
	
	@Test
	void testOnTickDoesNotSetStatusToTimedOutIfElapsedTimeIsLessThanTimeout()
	{
		for(i in 1..testee.timeout - 1)
		{
			testee.onTick()
			assertFalse('timeout should not have been reached', TIMED_OUT == testee.state)
		}
		
		testee.onTick()
		assertEquals('timeout reached', TIMED_OUT, testee.state)
	}

	@Test
	void testRegisterAsyncConditionSetsStatusToWaiting()
	{
		def actualRegisteredWithManager = null
		def mockManager = new MockFor(AsyncConditionManager)
		mockManager.demand.registerAsyncCondition{actualRegisteredWithManager = it}
		
		mockManager.use
		{
			testee.registerWithManager(new AsyncConditionManager())
		}

		assertEquals(testee, actualRegisteredWithManager)
		assertEquals(WAITING, testee.state)
	}

	private class NonAbstractAssertAsyncRunResult extends AssertAsyncRunResult
	{
		NonAbstractAssertAsyncRunResult(timeout, itemPath)
		{
			super(timeout, itemPath)
		}
		
		@Override
		def toXml(xmlBuilder)
		{
			throw NotImplementedException('toXml called for test stub - no implementation')
		}
		
		@Override
		def checkUpdate(itemPath, actualValue)
		{
			throw NotImplementedException('checkUpdate called for test stub - no implementation')
		}
	}
}
