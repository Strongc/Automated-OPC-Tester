package ch.cern.opc.dsl.common.async

import static org.junit.Assert.*
import org.junit.Before
import org.junit.Test

import static ch.cern.opc.dsl.common.async.AsyncState.*
import static ch.cern.opc.common.Quality.State.*
import static ch.cern.opc.dsl.common.async.AsyncUpdateTestUtils.*
import ch.cern.opc.common.ItemValue
import groovy.xml.DOMBuilder
import groovy.xml.dom.DOMCategory

class AssertAsyncQualityRunResultTest 
{
	private static final def TIMEOUT = 10
	private static final def ITEM_PATH = 'path.to.item'
	private static final def MESSAGE = 'assertion user message'
	private static final def EXPECTED_QUALITY = GOOD
	
	
	private AssertAsyncQualityRunResult testee
	def xmlBuilder
	
	@Before
	void setup()
	{
		xmlBuilder = DOMBuilder.newInstance()
		
		com.sun.org.apache.xerces.internal.dom.ElementImpl.mixin(DOMCategory)
		groovy.xml.dom.DOMCategory.NodesHolder.mixin(DOMCategory)
		
		testee = new AssertAsyncQualityRunResult(MESSAGE, TIMEOUT, ITEM_PATH, EXPECTED_QUALITY)
	}
	
	@Test
	void testCheckUpdateDoesNotChangeStateIfUpdateHasExpectedQuality()
	{
		assertEquals(CREATED, testee.state)
		testee.checkUpdate(ITEM_PATH, createUpdate(null, EXPECTED_QUALITY));
		assertEquals(CREATED, testee.state)
	}
	
	@Test
	void testCheckUpdateSetsStateToFailIfUnexpectedStateArrives()
	{
		def qualityGoodExpected = new AssertAsyncQualityRunResult(MESSAGE, TIMEOUT, ITEM_PATH, GOOD)
		assertEquals(CREATED, qualityGoodExpected.state)
		
		qualityGoodExpected.checkUpdate(ITEM_PATH, createUpdate(null, BAD));
		assertEquals(FAILED, qualityGoodExpected.state)
		
		def qualityBadExpected = new AssertAsyncQualityRunResult(MESSAGE, TIMEOUT, ITEM_PATH, BAD)
		assertEquals(CREATED, qualityBadExpected.state)

		qualityBadExpected.checkUpdate(ITEM_PATH, createUpdate(null, GOOD));
		assertEquals(FAILED, qualityBadExpected.state)
	}
	
	@Test
	void testCheckUpdateIgnoresUpdatesWithInvalidPath()
	{
		testee.checkUpdate(null, createUpdate('some_value', BAD))
		assertEquals(CREATED, testee.state)
		
		testee.checkUpdate('', createUpdate('some_value', BAD))
		assertEquals(CREATED, testee.state)
		
		testee.checkUpdate(' ', createUpdate('some_value', BAD))
		assertEquals(CREATED, testee.state)
		
		testee.checkUpdate('not.the.right.path', createUpdate('some_value', BAD))
		assertEquals(CREATED, testee.state)
	}
	
	@Test
	void testCheckUpdateStoresTheValueWhichCausedTheFailure()
	{
		testee.checkUpdate(ITEM_PATH, createUpdate('some_value', BAD, 'some_timestamp', 123))
		assertEquals(FAILED, testee.state)
		
		assertEquals('some_value', testee.theFailedUpdate.value)
		assertEquals(BAD, testee.theFailedUpdate.quality.state)
		assertEquals('some_timestamp', testee.theFailedUpdate.timestamp)
		assertEquals(123, testee.theFailedUpdate.datatype)
	}
	
	@Test
	void testTimedOutSetsStateToPassed()
	{
		testee.timedOut()
		assertEquals(PASSED, testee.state)
	}
	
	@Test
	void testToXml_withStateWaiting()
	{
		testee.elapsedWait = 100
		testee.state = WAITING

		def result = testee.toXml(xmlBuilder)
		assertTestCaseElementPresentAndNameAttributeIsCorrect(result, AssertAsyncQualityRunResult.TITLE, MESSAGE, 1)
		
		assertTrue(result.'@name'.contains('incomplete'))
		
		def incompleteElement = result.incomplete[0]
		assertEquals("item [${ITEM_PATH}] still waiting, no quality updates contrary to expected quality [${EXPECTED_QUALITY}] have been received, elapsed wait [${testee.elapsedWait}] seconds".toString(), incompleteElement.'@message')
	}
	
	@Test
	void testToXml_withStateFailed()
	{
		testee.elapsedWait = 100
		testee.theFailedUpdate = new ItemValue('some_value', BAD, 'some_timestamp', 123)
		testee.state = FAILED
		
		def result = testee.toXml(xmlBuilder)
		assertTestCaseElementPresentAndNameAttributeIsCorrect(result, AssertAsyncQualityRunResult.TITLE, MESSAGE, 1)
		
		assertTrue(result.'@name'.contains('failed'))

		def failureElement = result.failure[0]
		assertEquals("item [${ITEM_PATH}] expected quality was [${EXPECTED_QUALITY}] received unexpected quality [${BAD}] at [some_timestamp]. Elapsed wait [100] seconds".toString(), failureElement.'@message')
	}
	
	@Test
	void testToXml_withStateFailedIfTheFailuedUpdateIsNull()
	{
		testee.elapsedWait = 100
		testee.theFailedUpdate = null
		testee.state = FAILED
		
		def result = testee.toXml(xmlBuilder)
		
		def failureElement = result.failure[0]
		assertEquals("item [${ITEM_PATH}] expected quality was [${EXPECTED_QUALITY}] received unexpected quality [NULL QUALITY] at [NULL TIMESTAMP]. Elapsed wait [100] seconds".toString(), failureElement.'@message')
	}

	@Test
	void testToXml_withStatePassed()
	{
		testee.elapsedWait = 100
		testee.state = PASSED

		def result = testee.toXml(xmlBuilder)
		assertTestCaseElementPresentAndNameAttributeIsCorrect(result, AssertAsyncQualityRunResult.TITLE, MESSAGE, 1)
		
		assertTrue(result.'@name'.contains('success'))

		def successElement = result.success[0]
		assertEquals("item [${ITEM_PATH}] received no quality updates contrary to expected quality [${EXPECTED_QUALITY}] in [${testee.elapsedWait}] seconds".toString(), successElement.'@message')
	}


}
