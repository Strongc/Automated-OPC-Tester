package ch.cern.opc.dsl.common.async

import static org.junit.Assert.*
import org.apache.commons.lang.NotImplementedException;
import org.junit.Test
import org.junit.Before

import ch.cern.opc.dsl.common.async.AssertAsyncRunResult;
import ch.cern.opc.dsl.common.async.AsyncConditionManager;
import groovy.mock.interceptor.*
import static ch.cern.opc.dsl.common.async.AsyncState.*

class AssertAsyncRunResultTest 
{
	def testee
	
	@Before
	void setup()
	{
		testee = new NonAbstractAssertAsyncRunResult(10, "this.is.the.item.path")
	}
	
	@Test
	void testIsItemPathMatch()
	{
		assertTrue(new NonAbstractAssertAsyncRunResult(10, "this.is.the.item.path").
			isItemPathMatch("this.is.the.item.path") )
		
		assertFalse(new NonAbstractAssertAsyncRunResult(10, "this.is.the.item.path").
			isItemPathMatch("this.is.NOT.the.item.path") )
		
		assertFalse(new NonAbstractAssertAsyncRunResult(10, "this.is.the.item.path").
			isItemPathMatch("") )
		
		assertFalse(new NonAbstractAssertAsyncRunResult(10, "this.is.the.item.path").
			isItemPathMatch(null) )
		
		assertFalse(new NonAbstractAssertAsyncRunResult(10, "").
			isItemPathMatch("some.item.path") )
		
		assertFalse(new NonAbstractAssertAsyncRunResult(10, null).
			isItemPathMatch("some.item.path") )
		
		assertTrue(new NonAbstractAssertAsyncRunResult(10, "").
			isItemPathMatch("") )
		
		assertTrue(new NonAbstractAssertAsyncRunResult(10, null).
			isItemPathMatch(null) )
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
		
		testee.state = PASSED
		assertEquals(PASSED, testee.state)
		
		testee.state = FAILED
		assertEquals(FAILED, testee.state)
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
	void testOnTickDoesNotCallTimedOutIfElapsedTimeIsLessThanTimeout()
	{
		for(i in 1..testee.timeout - 1)
		{
			testee.onTick()
			assertFalse('timeout should not have been reached', testee.isTimedOut)
		}
		
		testee.onTick()
		assertTrue('timeout reached', testee.isTimedOut)
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
		public def isTimedOut = false
		 
		NonAbstractAssertAsyncRunResult(timeout, itemPath)
		{
			super(timeout, itemPath)
		}
		
		@Override
		def toXml(xmlBuilder)
		{
			return null
		}
		
		@Override
		def timedOut()
		{
			isTimedOut = true
		}
		
		@Override
		def checkUpdate(itemPath, actualValue)
		{
			throw NotImplementedException('checkUpdate called for test stub - no implementation')
		}
	}
}
