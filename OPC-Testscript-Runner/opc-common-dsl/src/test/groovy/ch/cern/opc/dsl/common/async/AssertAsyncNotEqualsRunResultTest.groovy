package ch.cern.opc.dsl.common.async

import static org.junit.Assert.*
import org.junit.Before
import org.junit.Test

import ch.cern.opc.common.ItemValue
import ch.cern.opc.common.Timestamp
import ch.cern.opc.dsl.common.async.AssertAsyncNotEqualsRunResult
import static ch.cern.opc.dsl.common.async.AsyncState.*
import static ch.cern.opc.dsl.common.async.AsyncUpdateTestUtils.*
import static ch.cern.opc.common.Quality.State.*

import groovy.xml.DOMBuilder
import groovy.xml.dom.DOMCategory

class AssertAsyncNotEqualsRunResultTest 
{
	private static final def ITEM_PATH = 'path.to.item'
	private static final def ANTI_EXPECTED_VALUE = 'anti expected value'
	private static final def MESSAGE = 'assertion user message'
	private static final def TIMESTAMP = "2012/11/19-18:48:2.411";
	
	def testee
	def xmlBuilder = DOMBuilder.newInstance()
	
	@Before
	void setup()
	{
		testee = new AssertAsyncNotEqualsRunResult(MESSAGE, 10, ITEM_PATH, ANTI_EXPECTED_VALUE)
		
		com.sun.org.apache.xerces.internal.dom.ElementImpl.mixin(DOMCategory)
		groovy.xml.dom.DOMCategory.NodesHolder.mixin(DOMCategory)
	}
	
	@Test
	void testCheckUpdate_withActualValueNotMatchingAntiExpectedValue()
	{
		testee.checkUpdate(ITEM_PATH, createUpdate(ANTI_EXPECTED_VALUE + 'and then some'))
		assertFalse(FAILED == testee.state)
	}
	
	@Test
	void testCheckUpdate_withActualValueMatchingAntiExpectedValue()
	{
		testee.checkUpdate(ITEM_PATH, createUpdate(ANTI_EXPECTED_VALUE))
		assertEquals(FAILED, testee.state)
	}
	
	@Test
	void testCheckUpdate_withPathNotEqualToItemPath()
	{
		testee.checkUpdate(ITEM_PATH+'and.then.some', createUpdate(ANTI_EXPECTED_VALUE))
		assertFalse(FAILED == testee.state)
	}
	
	@Test
	void testTimedOutSetsStateToPassed()
	{
		testee.timedOut()
		assertEquals(PASSED, testee.state)
	}
	
	@Test
	void testCheckUpdateStoresTheValueWhichCausedTheFailure()
	{
		testee.checkUpdate(ITEM_PATH, createUpdate(ANTI_EXPECTED_VALUE, GOOD, TIMESTAMP, 2))
		assertEquals(FAILED, testee.state)
		
		assertEquals(ANTI_EXPECTED_VALUE, testee.theFailedUpdate.value)
		assertEquals(GOOD, testee.theFailedUpdate.quality.state)
		assertEquals(new Timestamp(TIMESTAMP), testee.theFailedUpdate.timestamp)
		assertEquals(2, testee.theFailedUpdate.datatype.datatypeId)
	}
	
	@Test
	void testToXml_withStateFailed()
	{
		testee.elapsedWait = 100
		testee.state = FAILED
		testee.theFailedUpdate = new ItemValue(ANTI_EXPECTED_VALUE, GOOD, TIMESTAMP, 123)
		
		def result = testee.toXml(xmlBuilder)
		assertTestCaseElementPresentAndNameAttributeIsCorrect(result, AssertAsyncNotEqualsRunResult.TITLE, MESSAGE, 1)
		
		assertTrue(result.'@name'.contains('failed'))

		def failureElement = result.failure[0]
		assertEquals("item [${ITEM_PATH}] received anti-expected value [${ANTI_EXPECTED_VALUE}] at [${TIMESTAMP}]. Elapsed wait [100] seconds".toString(), failureElement.'@message')
	}
	
	@Test
	void testToXml_withStatePassed()
	{
		testee.elapsedWait = 100
		testee.state = PASSED

		def result = testee.toXml(xmlBuilder)
		assertTestCaseElementPresentAndNameAttributeIsCorrect(result, AssertAsyncNotEqualsRunResult.TITLE, MESSAGE, 1)
		
		assertTrue(result.'@name'.contains('success'))

		def successElement = result.success[0]
		assertEquals("item [${ITEM_PATH}] did not match anti-expected value [${ANTI_EXPECTED_VALUE}] in [${testee.elapsedWait}] seconds".toString(), successElement.'@message')
	}
	
	@Test
	void testToXml_withStateWaiting()
	{
		testee.elapsedWait = 100
		testee.state = WAITING

		def result = testee.toXml(xmlBuilder)
		assertTestCaseElementPresentAndNameAttributeIsCorrect(result, AssertAsyncNotEqualsRunResult.TITLE, MESSAGE, 1)
		
		assertTrue(result.'@name'.contains('incomplete'))
		
		def incompleteElement = result.incomplete[0]
		assertEquals("item [${ITEM_PATH}] still waiting,  anti-expected value [${ANTI_EXPECTED_VALUE}], elapsed wait [${testee.elapsedWait}] seconds".toString(), incompleteElement.'@message')
	}
	
	@Test (expected=IllegalStateException.class)
	void testToXMLForInvalidState()
	{
		testee.state = CREATED
		testee.toXml(xmlBuilder)
	}
	
//	private static def assertTestCaseElementPresentAndNameAttributeIsCorrect(xml)
//	{
//		assertEquals('testcase root element should be called -testcase-',
//			'testcase', xml.name)
//		
//		assertTrue('testcase root element name attribute should contain the test type', 
//			xml.'@name'.contains(AssertAsyncNotEqualsRunResult.TITLE))
//		
//		assertTrue('testcase root element name attribute shoudl contain the user message',
//			xml.'@name'.contains(MESSAGE))
//		
//		assertEquals('Should be a single child element detailing the testcase outcome: pass/fail etc', 
//			1, xml.size())
//	}
}
