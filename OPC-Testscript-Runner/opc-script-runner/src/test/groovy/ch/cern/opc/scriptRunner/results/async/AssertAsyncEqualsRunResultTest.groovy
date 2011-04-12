package ch.cern.opc.scriptRunner.results.async

import static org.junit.Assert.*
import org.junit.Before
import org.junit.Test

import groovy.xml.DOMBuilder
import groovy.xml.dom.DOMCategory

import static ch.cern.opc.scriptRunner.results.async.AssertAsyncRunResult.ASYNC_STATE.CREATED as CREATED
import static ch.cern.opc.scriptRunner.results.async.AssertAsyncRunResult.ASYNC_STATE.WAITING as WAITING
import static ch.cern.opc.scriptRunner.results.async.AssertAsyncRunResult.ASYNC_STATE.TIMED_OUT as TIMED_OUT
import static ch.cern.opc.scriptRunner.results.async.AssertAsyncRunResult.ASYNC_STATE.MATCHED as MATCHED


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
	void testCheckUpdateSetsStatusToMatchedIfUpdateMatchesExpected()
	{
		testee.checkUpdate(ITEM_PATH, EXPECTED_VALUE)
		assertEquals(MATCHED, testee.state)
	}
	
	@Test
	void testCheckUpdateDoesNotSetStatusToMatchedIfUpdateForDifferentItem()
	{
		testee.checkUpdate('different.item.path', EXPECTED_VALUE)
		assertFalse(MATCHED == testee.state)
	}
	
	@Test
	void testCheckUpdateDoesNotSetStausToMatchedIfUpdateHasNonExpectedValue()
	{
		testee.checkUpdate(ITEM_PATH, 'but not the expected value')
		assertFalse(MATCHED == testee.state)
	}
	
	@Test
	void testToXmlForMatched()
	{
		testee.elapsedWait = TIMEOUT - 1
		testee.state = MATCHED

		def testcaseElement = testee.toXml(xmlBuilder)
		assertTestCaseElementPresentAndNameAttributeIsCorrect(testcaseElement)
		
		assertTrue(testcaseElement.'@name'.contains('passed'))
		
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
		
		def incompleteElement = testcaseElement.incomplete[0]
		assertEquals("item [${ITEM_PATH}] waiting to obtain expected value [${EXPECTED_VALUE}], elapsed wait [${testee.elapsedWait}] seconds".toString(), incompleteElement.'@message')
	}
	
	@Test (expected=IllegalStateException.class)
	void testToXMLForInvalidState()
	{
		testee.state = CREATED
		testee.toXml(xmlBuilder)
	}
	
	private static def assertTestCaseElementPresentAndNameAttributeIsCorrect(xml)
	{
		assertEquals('testcase root element should be called -testcase-',
			'testcase', xml.name)
		
		assertTrue('testcase root element name attribute should contain the test type',
			xml.'@name'.contains(AssertAsyncEqualsRunResult.TITLE))
		
		assertTrue('testcase root element name attribute shoudl contain the user message',
			xml.'@name'.contains(MESSAGE))
		
		assertEquals('Should be a single child element detailing the testcase outcome: pass/fail etc',
			1, xml.size())
	}
	
}