package ch.cern.opc.dsl.common.async

import static org.junit.Assert.*
import org.junit.Before
import org.junit.Test

import ch.cern.opc.common.ItemValue
import static ch.cern.opc.dsl.common.async.AsyncUpdateTestUtils.*
import ch.cern.opc.dsl.common.async.AssertAsyncEqualsRunResult
import static ch.cern.opc.dsl.common.async.AsyncState.*
import groovy.xml.DOMBuilder
import groovy.xml.dom.DOMCategory

class AssertAsyncEqualsRunResultTest
{
	private static final def TIMEOUT = 10
	private static final def ITEM_PATH = 'path.to.item'
	private static final def EXPECTED_VALUE = 'expected value'
	private static final def NOT_THE_EXPECTED_VALUE = 'not the expected value'
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
	void testCheckUpdateSetsStatusToMatchedIfUpdateMatchesExpected()
	{
		testee.checkUpdate(ITEM_PATH, createUpdate(EXPECTED_VALUE))
		assertEquals(PASSED, testee.state)
	}
	
	@Test
	void testCheckUpdateSetsStatusToPassedForDifferentStringTypes()
	{
		testee = new AssertAsyncEqualsRunResult(null, null, 'the.path', '23')
		testee.checkUpdate('the.path', createUpdate(new java.lang.String("23")))
		assertEquals(PASSED, testee.state)
		
		testee = new AssertAsyncEqualsRunResult(null, null, 'the.path', new java.lang.String("23"))
		testee.checkUpdate('the.path', createUpdate('23'))
		assertEquals(PASSED, testee.state)
		
		testee = new AssertAsyncEqualsRunResult(null, null, 'the.path', "${23}")
		testee.checkUpdate('the.path', createUpdate('23'))
		assertEquals(PASSED, testee.state)
	}
	
	@Test
	void testCheckUpdateHandlesNullActualValue()
	{
		testee = new AssertAsyncEqualsRunResult(null, null, 'the.path', 'expected_value')
		testee.checkUpdate('the.path', null)
		assertTrue(testee.state != PASSED)
	}
	
	@Test
	void testCheckUpdateHandlesNullExpectedValue()
	{
		testee = new AssertAsyncEqualsRunResult(null, null, 'the.path', null)
		testee.checkUpdate('the.path', createUpdate(EXPECTED_VALUE))
		assertTrue(testee.state != PASSED)
	}
	
	@Test
	void testCheckUpdateDoesNotSetStatusToMatchedIfUpdateForDifferentItem()
	{
		testee.checkUpdate('different.item.path', createUpdate(EXPECTED_VALUE))
		assertFalse(PASSED == testee.state)
	}
	
	@Test
	void testCheckUpdateDoesNotSetStausToMatchedIfUpdateHasNonExpectedValue()
	{
		testee.checkUpdate(ITEM_PATH, createUpdate(NOT_THE_EXPECTED_VALUE))
		assertFalse(PASSED == testee.state)
	}
	
	@Test
	void testTimedOutSetsStateToFailed()
	{
		testee.timedOut()
		assertEquals(FAILED, testee.state)
	}
	
	@Test
	void testToXmlForPassed()
	{
		testee.elapsedWait = TIMEOUT - 1
		testee.state = PASSED

		def testcaseElement = testee.toXml(xmlBuilder)
		assertTestCaseElementPresentAndNameAttributeIsCorrect(testcaseElement, AssertAsyncEqualsRunResult.TITLE, MESSAGE, 1)
		
		assertTrue(testcaseElement.'@name'.contains('passed'))
		
		def successElement = testcaseElement.success[0]
		assertEquals("item [${ITEM_PATH}] obtained expected value [${EXPECTED_VALUE}] in [${testee.elapsedWait}] seconds".toString(), successElement.'@message')
	}
	
	@Test
	void testToXmlForFailed()
	{
		testee.elapsedWait = TIMEOUT
		testee.state = FAILED
		
		def testcaseElement = testee.toXml(xmlBuilder)
		assertTestCaseElementPresentAndNameAttributeIsCorrect(testcaseElement, AssertAsyncEqualsRunResult.TITLE, MESSAGE, 1)
		
		assertTrue(testcaseElement.'@name'.contains('failed'))
		
		def failureElement = testcaseElement.failure[0]
		assertEquals("item [${ITEM_PATH}] failed to obtain expected value [${EXPECTED_VALUE}] in [${testee.elapsedWait}] seconds".toString(), failureElement.'@message')
	}
	
	@Test
	void testToXmlForWaiting()
	{
		testee.elapsedWait = 0
		testee.state = WAITING
		
		def testcaseElement = testee.toXml(xmlBuilder)
		assertTestCaseElementPresentAndNameAttributeIsCorrect(testcaseElement, AssertAsyncEqualsRunResult.TITLE, MESSAGE, 1)
		
		assertTrue(testcaseElement.'@name'.contains('incomplete'))
		
		def incompleteElement = testcaseElement.incomplete[0]
		assertEquals("item [${ITEM_PATH}] waiting to obtain expected value [${EXPECTED_VALUE}], elapsed wait [${testee.elapsedWait}] seconds".toString(), incompleteElement.'@message')
	}
	
	@Test (expected=IllegalStateException.class)
	void testToXMLForInvalidState()
	{
		testee.state = CREATED
		testee.toXml(xmlBuilder)
	}
}