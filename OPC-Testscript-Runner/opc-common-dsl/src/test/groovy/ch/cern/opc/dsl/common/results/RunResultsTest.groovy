package ch.cern.opc.dsl.common.results

import java.beans.PropertyChangeListener


import static org.junit.Assert.*
import org.apache.commons.lang.NotImplementedException
import org.junit.Test
import org.junit.Before

import groovy.xml.DOMBuilder
import groovy.xml.dom.DOMCategory
import groovy.mock.interceptor.StubFor
import ch.cern.opc.dsl.common.client.GenericClient
import ch.cern.opc.dsl.common.async.AssertAsyncEqualsRunResult
import ch.cern.opc.dsl.common.async.AssertAsyncNotEqualsRunResult
import ch.cern.opc.dsl.common.async.AssertAsyncQualityRunResult
import ch.cern.opc.dsl.common.async.AsyncConditionManager
import ch.cern.opc.dsl.common.results.RunResult
import ch.cern.opc.dsl.common.results.RunResults
import ch.cern.opc.common.Quality
import static ch.cern.opc.common.Quality.State.*
import static ch.cern.opc.common.Datatype.*
import static ch.cern.opc.common.ItemAccessRight.*

import static ch.cern.opc.dsl.common.results.RunResults.NULL_MSG
import static ch.cern.opc.dsl.common.results.RunResults.EMPTY_MSG

class RunResultsTest 
{
	private def asyncManagerRegisteredConditionsCount = 0
	private def isAsyncManagerTicking = false
	
	private def isUpdateHandlerRegistered = false
	private def maxConditionTimeout = 10
	private def isCleanUpCalled = false
	
	def testee
	def client
	
	static final def LAST_DLL_ERR = 'this is the last error from the DLL'
	
	@Before
	void setup()
	{
		isCleanUpCalled = false
		
		client = [
			getLastError:{return LAST_DLL_ERR},
			registerForAsyncUpdates:{isUpdateHandlerRegistered = true},
			cleanUp:{isCleanUpCalled = true}
			] as GenericClient
		
		def stubAsyncManager = new StubFor(AsyncConditionManager)
		stubAsyncManager.demand.getRegisteredAsyncConditionsCount(100) 
		{
			println('note - decrementing registered conditions count...')
			return asyncManagerRegisteredConditionsCount > 0? asyncManagerRegisteredConditionsCount-- :0 
		}
		stubAsyncManager.demand.startTicking(100) {isAsyncManagerTicking = true}
		stubAsyncManager.demand.stopTicking(100) {isAsyncManagerTicking = false}
		stubAsyncManager.demand.registerAsyncCondition(100){}
		stubAsyncManager.demand.getMaxConditionTimeout(100){return maxConditionTimeout}
		
		/*
		* Non-obvious code: The testee.asyncManager and testee.asyncUpdater
		* fields we want to mock are final, thus cannot be set with object.field
		* (final fields must be initialised by the ctor).
		*
		* Create the testee within the use{} block of the stubbed classes. The
		* testee's ctor calls 'new AsyncConditionManager()'
		* Since this is within the scope of the stubs use{} blocks stubbed object
		* instance are created.
		*
		* Sorry.
		*/
	   stubAsyncManager.use
	   {
		   testee = new RunResults()
	   }
	   testee.client = client
		
	   testee.pingPeriod = 1//s
	}	
	
	@Test
	void testAssertingAddsAssertions()
	{
		testee.assertTrue('should pass', true)
		testee.assertTrue('should fail', false)
		
		testee.addException(new Exception('deliberate exception'))
		
		testee.assertFalse('should pass', false)
		testee.assertFalse('should fail', true)
		
		testee.assertEquals('should pass', 1.0, 1.0)
		testee.assertEquals('should fail', 1.0, 2.0)
		
		assertEquals(7, testee.XML.getChildNodes().length)
	}
	
	@Test
	void testAssertQuality_AddsQualityNodes()
	{
		assertEquals(0, testee.XML.getChildNodes().length)
		testee.assertQuality('I should pass', GOOD, new Quality(192))
		assertEquals(1, testee.XML.getChildNodes().length)
		testee.assertQuality('I should fail', BAD, new Quality(192))
		assertEquals(2, testee.XML.getChildNodes().length)
	}
	
	@Test
	void testOnScriptEnd_CallsClientCleanUp()
	{
		assertFalse(isCleanUpCalled)
		
		testee.onScriptEnd()
		
		assertTrue(isCleanUpCalled)
	}
	
	@Test
	void testOnScriptEnd_NoRegisteredAsyncConditions()
	{
		asyncManagerRegisteredConditionsCount = 0
		isAsyncManagerTicking = true
		
		testee.onScriptEnd()
		
		assertFalse(isAsyncManagerTicking)
	}
	
	@Test
	void testOnScriptEnd_StopsAsyncTicker()
	{
		asyncManagerRegisteredConditionsCount = 0
		isAsyncManagerTicking = true

		testee.onScriptEnd()
		assertFalse(isAsyncManagerTicking)
	}
	
	@Test
	void testOnScriptEnd_WaitsForRegisteredAsyncConditionsToBeRemoved()
	{
		asyncManagerRegisteredConditionsCount = 1
		isAsyncManagerTicking = true
		
		testee.onScriptEnd()
		
		assertEquals("ticker should wait for active condition count to reach 0", 0, asyncManagerRegisteredConditionsCount)
	}

	
	@Test
	void testOnScriptEnd_WaitsForMinimumAmountOfTimeWithLongConditionTimeout()
	{
		testee.pingPeriod = 1
		maxConditionTimeout = 100
		
		asyncManagerRegisteredConditionsCount = 1
		
		def before = new Date().getTime()
		testee.onScriptEnd()
		def after = new Date().getTime()
		
		assertTrue(after-before >= testee.pingPeriod*1000)
		assertTrue(after-before < maxConditionTimeout*1000)
	}
	
	@Test
	void testOnScriptEnd_WaitsForMinimumAmountOfTimeWithShortConditionTimeout()
	{
		testee.pingPeriod = 100
		maxConditionTimeout = 1
		
		asyncManagerRegisteredConditionsCount = 1
		
		def before = new Date().getTime()
		testee.onScriptEnd()
		def after = new Date().getTime()
		
		assertTrue(after-before >= maxConditionTimeout*1000)
		assertTrue(after-before < testee.pingPeriod*1000)
	}

	
	@Test
	void testOnScriptStart_RegistersAsyncUpdaterAndStartsTicker()
	{
		assertFalse(isAsyncManagerTicking)
		
		testee.onScriptStart()
		
		assertTrue(isAsyncManagerTicking)
	}
	
	@Test
	void testAssertAsyncEquals_RegistersAsyncAssertObjectWithAsyncManager()
	{
		def isAsyncAssertRunResultRegisteredWithAsyncManager = false
		
		def stubAsyncAssertEquals = new StubFor(AssertAsyncEqualsRunResult)
		stubAsyncAssertEquals.demand.registerWithManager {isAsyncAssertRunResultRegisteredWithAsyncManager = true}
		
		stubAsyncAssertEquals.use 
		{
			testee.assertAsyncEquals(null, null, null, null)	
		}
		
		assertTrue(isAsyncAssertRunResultRegisteredWithAsyncManager)
	}
	
	@Test
	void testAssertAsyncEquals_AddsAsyncAssertObjectToResults()
	{
		assertEquals(0, testee.results.size)
		
		testee.assertAsyncEquals(null, null, null, null)
		
		assertEquals(1, testee.results.size)
	}
	
	@Test
	void testAssertAsyncNotEquals_RegistersAsyncAssertObjectWithManager()
	{
		def isAsyncAssertNotEqualsRegisteredWithAsyncManager = false
		
		def stubAsyncAssertNotEquals = new StubFor(AssertAsyncNotEqualsRunResult)
		stubAsyncAssertNotEquals.demand.registerWithManager {isAsyncAssertNotEqualsRegisteredWithAsyncManager = true}
		
		stubAsyncAssertNotEquals.use 
		{
			testee.assertAsyncNotEquals(null, null, null, null)	
		}
		
		assertTrue(isAsyncAssertNotEqualsRegisteredWithAsyncManager)
	}
	
	@Test
	void testAssertAsyncNotEquals_AddsAsyncAssertObjectToResults()
	{
		assertEquals(0, testee.results.size)
		
		testee.assertAsyncNotEquals(null, null, null, null)
		
		assertEquals(1, testee.results.size)
	}
	
	@Test
	void testAssertAsyncQuality_RegistersAsyncAssertObjectWithManager()
	{
		def isAsyncAssertQualityRegisteredWithAsyncManager = false
		
		def stubAsyncAssertQuality = new StubFor(AssertAsyncQualityRunResult)
		stubAsyncAssertQuality.demand.registerWithManager {isAsyncAssertQualityRegisteredWithAsyncManager = true}
		
		stubAsyncAssertQuality.use
		{
			testee.assertAsyncQuality(null, null, null, null)
		}
		
		assertTrue(isAsyncAssertQualityRegisteredWithAsyncManager)
	}
	
	@Test
	void testAssertAsyncQuality_AddsAsyncAssertObjectToResults()
	{
		assertEquals(0, testee.results.size)
		
		testee.assertAsyncQuality(null, null, null, null)
		
		assertEquals(1, testee.results.size)
	}

	
	@Test
	void testAddingAssertTrueUpdatesObserver()
	{
		def updateInfo = null
		
		def observer = {Object[] args -> updateInfo = args[1]} as Observer
		testee.addObserver(observer)
		
		testee.assertTrue(null, null)
		
		assertTrue(updateInfo instanceof RunResult)
	}
	
	@Test
	void testAddingRunResultUpdatesObserver()
	{
		def updateInfo = null
		
		def observer = {Object[] args ->
			updateInfo = args[1]
		} as Observer
		
		testee.addObserver(observer)

		def newRunResult = {throw new NotImplementedException('do not call impl')} as RunResult
		testee.add(newRunResult)
		
		assertSame(newRunResult, updateInfo)
	}
	
	@Test
	void testAssertTruePassing()
	{
		testee.assertTrue('should pass', true)
		
		def assertion = testee.results[testee.results.size-1]
		
		assertEquals('assertTrue', assertion.title)
		assertTrue(assertion.isPassed)
		assertEquals('should pass', assertion.userMessage)
		assertEquals('value was true', assertion.passFailMessage)
	}
	
	@Test
	void testAssertTrueFailing()
	{
		testee.assertTrue('should fail', false)
		
		def assertion = testee.results[testee.results.size-1]
		
		assertFalse(assertion.isPassed)
		assertEquals('value was not true', assertion.passFailMessage)
	}
	
	@Test
	void testAssertFalsePassing()
	{
		testee.assertFalse('should pass', false)
		
		def assertion = testee.results[testee.results.size-1]
		
		assertEquals('assertFalse', assertion.title)
		assertTrue(assertion.isPassed)
		assertEquals('should pass', assertion.userMessage)
		assertEquals('value was false', assertion.passFailMessage)
	}

	@Test
	void testAssertFalseFailing()
	{
		testee.assertFalse('should fail', true)
		
		def assertion = testee.results[testee.results.size-1]
		
		assertFalse(assertion.isPassed)
		assertEquals('value was not false', assertion.passFailMessage)
	}
	
	@Test
	void testAssertEqualsPassing()
	{
		testee.assertEquals('should pass', 'some value', 'some value')
		
		def assertion = testee.results[testee.results.size-1]
		
		assertEquals('assertEquals', assertion.title)
		assertTrue(assertion.isPassed)
		assertEquals('should pass', assertion.userMessage)
		assertEquals('expected [some value] actual [some value]'.toString(), assertion.passFailMessage)
	}

	@Test
	void testAssertEqualsFailing()
	{
		testee.assertEquals('should fail', 'some value', 'different value')
		
		def assertion = testee.results[testee.results.size-1]
		
		assertFalse(assertion.isPassed)
		assertEquals("expected [some value] actual [different value] last error from dll [${LAST_DLL_ERR}]".toString(), assertion.passFailMessage)
	}
	
	@Test
	void testAssertEqualsWithNullValues()
	{
		testee.assertEquals('should fail', 'expected value', null)
		assertFalse(testee.results[testee.results.size-1].isPassed)
		
		testee.assertEquals('should fail', null, 'actual value')
		assertFalse(testee.results[testee.results.size-1].isPassed)
		
		testee.assertEquals('should pass', null, null)
		assertTrue(testee.results[testee.results.size-1].isPassed)
	}
	
	@Test
	void testAssertEqualsWithEmptyValues()
	{
		testee.assertEquals('should fail', 'expected value', '')
		assertFalse(testee.results[testee.results.size-1].isPassed)
		
		testee.assertEquals('should fail', '', 'actual value')
		assertFalse(testee.results[testee.results.size-1].isPassed)
		
		testee.assertEquals('should pass', '', "")
		assertTrue(testee.results[testee.results.size-1].isPassed)
	}
	
	@Test
	void testAssertQualityPassing()
	{
		testee.assertQuality('should pass', GOOD, GOOD)
		
		def assertion = testee.results[testee.results.size-1]
		
		assertEquals('assertQuality', assertion.title)
		assertTrue(assertion.isPassed)
		assertEquals('should pass', assertion.userMessage)
		assertEquals('expected [GOOD] actual [GOOD]'.toString(), assertion.passFailMessage)
	}
	
	@Test
	void testAssertQualityFailing()
	{
		testee.assertQuality('should fail', GOOD, BAD)
		
		def assertion = testee.results[testee.results.size-1]
		
		assertFalse(assertion.isPassed)
		assertEquals("expected [GOOD] actual [BAD] last error from dll [${LAST_DLL_ERR}]".toString(), assertion.passFailMessage)
	}

	@Test
	void testAssertQualityWithNullValues()
	{
		testee.assertQuality('should fail', GOOD, null)
		assertFalse(testee.results[testee.results.size-1].isPassed)
		
		testee.assertQuality('should fail', null, GOOD)
		assertFalse(testee.results[testee.results.size-1].isPassed)
		
		testee.assertQuality('should pass', null, null)
		assertTrue(testee.results[testee.results.size-1].isPassed)
	}
	
	@Test
	void testAssertDatatypePassing()
	{
		testee.assertDatatype('should pass', VT_BOOL, VT_BOOL)
		
		def assertion = testee.results[testee.results.size-1]
		
		assertEquals('assertDatatype', assertion.title)
		assertTrue(assertion.isPassed)
		assertEquals('should pass', assertion.userMessage)
		assertEquals('expected [VT_BOOL] actual [VT_BOOL]'.toString(), assertion.passFailMessage)
	}
	
	@Test
	void testAssertDatatypeFailing()
	{
		testee.assertDatatype('should fail', VT_BOOL, VT_UNRECOGNISED)
		
		def assertion = testee.results[testee.results.size-1]
		
		assertFalse(assertion.isPassed)
		assertEquals("expected [VT_BOOL] actual [VT_UNRECOGNISED] last error from dll [${LAST_DLL_ERR}]".toString(), assertion.passFailMessage)
	}

	@Test
	void testAssertDatatypeWithNullValues()
	{
		testee.assertDatatype('should fail', VT_BOOL, null)
		assertFalse(testee.results[testee.results.size-1].isPassed)
		
		testee.assertDatatype('should fail', null, VT_INT)
		assertFalse(testee.results[testee.results.size-1].isPassed)
		
		testee.assertDatatype('should pass', null, null)
		assertTrue(testee.results[testee.results.size-1].isPassed)
	}
	
	@Test
	void testAssertAccessRightsPassing()
	{
		testee.assertAccessRights('should pass', READ_WRITE_ACCESS, READ_WRITE_ACCESS)
		
		def assertion = testee.results[testee.results.size-1]
		
		assertEquals('assertAccessRights', assertion.title)
		assertTrue(assertion.isPassed)
		assertEquals('should pass', assertion.userMessage)
		assertEquals('expected [READ_WRITE_ACCESS] actual [READ_WRITE_ACCESS]', assertion.passFailMessage)
	}	
	
	@Test
	void testAssertAccessRightsFailing()
	{
		testee.assertAccessRights('should fail', READ_WRITE_ACCESS, READ_ACCESS)
		
		def assertion = testee.results[testee.results.size-1]
		
		assertFalse(assertion.isPassed)
		assertEquals("expected [READ_WRITE_ACCESS] actual [READ_ACCESS] last error from dll [${LAST_DLL_ERR}]".toString(), assertion.passFailMessage)
	}

	@Test
	void testAssertAccessRightsWithNullValues()
	{
		testee.assertAccessRights('should fail', WRITE_ACCESS, null)
		assertFalse(testee.results[testee.results.size-1].isPassed)
		
		testee.assertAccessRights('should fail', null, READ_ACCESS)
		assertFalse(testee.results[testee.results.size-1].isPassed)
		
		testee.assertAccessRights('should pass', null, null)
		assertTrue(testee.results[testee.results.size-1].isPassed)
	}

}
