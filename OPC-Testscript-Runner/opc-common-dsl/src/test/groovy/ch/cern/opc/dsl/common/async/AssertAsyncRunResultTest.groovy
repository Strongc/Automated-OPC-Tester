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
	AssertAsyncRunResult testee
	
	private final static String ITEM_PATH = 'this.is.the.item.path'
	private final static String ITEM_VALUE = '42'
	
	@Before
	void setup()
	{
		testee = new NonAbstractAssertAsyncRunResult(10, ITEM_PATH, ITEM_VALUE)
	}
	
	@Test
	void testIsItemPathMatch()
	{
		assertTrue(testee.isItemPathMatch(ITEM_PATH))
		
		assertFalse(testee.isItemPathMatch(ITEM_PATH+'NOT!') )
		
		assertFalse(testee.isItemPathMatch("") )
		
		assertFalse(testee.isItemPathMatch(null) )
		
		assertFalse(new NonAbstractAssertAsyncRunResult(10, "", ITEM_VALUE).
			isItemPathMatch("some.item.path") )
		
		assertFalse(new NonAbstractAssertAsyncRunResult(10, null, ITEM_VALUE).
			isItemPathMatch("some.item.path") )
		
		assertTrue(new NonAbstractAssertAsyncRunResult(10, "", ITEM_VALUE).
			isItemPathMatch("") )
		
		assertTrue(new NonAbstractAssertAsyncRunResult(10, null, ITEM_VALUE).
			isItemPathMatch(null) )
	}
	
	@Test
	void testIsItemValueMatch()
	{
		assertTrue(testee.isItemValueMatch(ITEM_VALUE))
		
		assertFalse(testee.isItemValueMatch(ITEM_VALUE+'NOT!') )
		
		assertFalse(testee.isItemValueMatch("") )
		
		assertFalse(testee.isItemValueMatch(null) )
		
		assertFalse(new NonAbstractAssertAsyncRunResult(10, ITEM_PATH, "").
			isItemValueMatch("some.item.path") )
		
		assertFalse(new NonAbstractAssertAsyncRunResult(10, ITEM_PATH, null).
			isItemValueMatch("some.item.path") )
		
		assertTrue(new NonAbstractAssertAsyncRunResult(10, ITEM_PATH, "").
			isItemValueMatch("") )
		
		assertTrue(new NonAbstractAssertAsyncRunResult(10, ITEM_PATH, null).
			isItemValueMatch(null) )
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
		 
		NonAbstractAssertAsyncRunResult(timeout, itemPath, itemValue)
		{
			super(timeout, itemPath, itemValue)
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
