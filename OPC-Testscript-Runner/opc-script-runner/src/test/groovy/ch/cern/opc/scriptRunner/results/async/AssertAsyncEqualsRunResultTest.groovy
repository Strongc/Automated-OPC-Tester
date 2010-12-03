package ch.cern.opc.scriptRunner.results.async

import static org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import groovy.mock.interceptor.*

import static ch.cern.opc.scriptRunner.results.async.AssertAsyncEqualsRunResult.ASYNC_STATE.*
import groovy.xml.DOMBuilder
import groovy.xml.dom.DOMCategory

class AssertAsyncEqualsRunResultTest 
{
	private static final def TIMEOUT = 10
	private static final def ITEM_PATH = 'path.to.item'
	private static final def EXPECTED_VALUE = 'expected value'
	private static final def MESSAGE = 'assertion user message'
	
	def testee
	def xmlBuilder
	
	@Before
	void setup()
	{
		xmlBuilder = DOMBuilder.newInstance()
		
		com.sun.org.apache.xerces.internal.dom.ElementImpl.mixin(DOMCategory)
		groovy.xml.dom.DOMCategory.NodesHolder.mixin(DOMCategory)
		
		testee = new AssertAsyncEqualsRunResult(MESSAGE, TIMEOUT, ITEM_PATH, EXPECTED_VALUE)
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
		
		testee.state = PASSED
		assertEquals(PASSED, testee.state)	
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
	void testCheckUpdateSetsStatusToPassedIfUpdateMatchesExpected()
	{
		testee.checkUpdate(ITEM_PATH, EXPECTED_VALUE)
		assertEquals(PASSED, testee.state)
	}
	
	@Test
	void testCheckUpdateDoesNotSetStatusToPassedIfUpdateForDifferentItem()
	{
		testee.checkUpdate('different.item.path', EXPECTED_VALUE)
		assertFalse(PASSED == testee.state)
	}
	
	@Test
	void testCheckUpdateDoesNotSetStausToPassedIfUpdateHasNonExpectedValue()
	{
		testee.checkUpdate(ITEM_PATH, 'but not the expected value')
		assertFalse(PASSED == testee.state)
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
	void testRegisterAsyncAssertSetsStatusToWaiting()
	{
		def actualRegisteredWithManager = null
		def mockManager = new MockFor(AssertAsynchManager)
		mockManager.demand.registerAsyncAssert{actualRegisteredWithManager = it}
		
		mockManager.use 
		{
			testee.registerWithManager(new AssertAsynchManager())	
		}

		assertEquals(testee, actualRegisteredWithManager)		
		assertEquals(WAITING, testee.state)
	}
	
	@Test
	void testToXmlForPassed()
	{
		testee.elapsedWait = TIMEOUT - 1
		testee.state = PASSED

		def testcaseElement = testee.toXml(xmlBuilder)
		assertTestCaseElementPresentAndNameAttributeIsCorrect(testcaseElement)
		
		assertTrue(testcaseElement.'@name'.contains('passed'))
		assertTrue(testcaseElement.'@name'.contains(MESSAGE))
		
		assertEquals(1, testcaseElement.size())
		def successElement = testcaseElement.success[0]
		
		assertEquals("item [${ITEM_PATH}] obtained expected value [${EXPECTED_VALUE}] in [${testee.elapsedWait}] seconds".toString(), successElement.'@message')
	}
	
	@Test
	void testToXmlForTimedOut()
	{
		testee.elapsedWait = TIMEOUT
		testee.state = TIMED_OUT
		
		def testcaseElement = testee.toXml(xmlBuilder)
		assertTestCaseElementPresentAndNameAttributeIsCorrect(testcaseElement)
		
		assertTrue(testcaseElement.'@name'.contains('failed'))
		assertTrue(testcaseElement.'@name'.contains(MESSAGE))
		
		assertEquals(1, testcaseElement.size())
		def failureElement = testcaseElement.failure[0]
		
		assertEquals("item [${ITEM_PATH}] failed to obtain expected value [${EXPECTED_VALUE}] in [${testee.elapsedWait}] seconds".toString(), failureElement.'@message')
	}
	
	@Test
	void testToXmlForWaiting()
	{
		testee.elapsedWait = 0
		testee.state = WAITING
		
		def testcaseElement = testee.toXml(xmlBuilder)
		assertTestCaseElementPresentAndNameAttributeIsCorrect(testcaseElement)
		
		assertTrue(testcaseElement.'@name'.contains('incomplete'))
		assertTrue(testcaseElement.'@name'.contains(MESSAGE))

		assertEquals(1, testcaseElement.size())
		def incompleteElement = testcaseElement.incomplete[0]
		
		assertEquals("item [${ITEM_PATH}] waiting to obtain expected value [${EXPECTED_VALUE}], elapsed wait [${testee.elapsedWait}] seconds".toString(), incompleteElement.'@message')
	}
	
	private static def assertTestCaseElementPresentAndNameAttributeIsCorrect(testcaseElement)
	{
		assertEquals('testcase', testcaseElement.name)
		assertTrue(testcaseElement.'@name'.contains(AssertAsyncEqualsRunResult.TITLE))
	}
	
}