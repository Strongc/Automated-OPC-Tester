package ch.cern.opc.scriptRunner.results
import ch.cern.opc.scriptRunner.results.RunResults;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;

import groovy.xml.DOMBuilder
import groovy.xml.dom.DOMCategory
import groovy.mock.interceptor.StubFor
import ch.cern.opc.client.ClientApi
import ch.cern.opc.client.ClientInstance
import ch.cern.opc.scriptRunner.results.async.AsyncConditionManager
import ch.cern.opc.scriptRunner.AsyncUpdateHandler
import ch.cern.opc.scriptRunner.results.async.AssertAsyncEqualsRunResult

import static ch.cern.opc.scriptRunner.results.RunResults.NULL_MSG
import static ch.cern.opc.scriptRunner.results.RunResults.EMPTY_MSG

class RunResultsTest 
{
	private def asyncManagerRegisteredConditionsCount = 0
	private def isAsyncManagerTicking = false
	private def asyncManagerTimedOutRemainingAsyncConditions = false
	
	private def isAsyncUpdateHandlerRegistered = false
	
	def testee
	
	@Before
	void setup()
	{
		
		def theClientInstance = [
				getLastError:{it->
					return 'last error from DLL'
				}
				] as ClientApi
		
		ClientInstance.metaClass.'static'.getInstance = {-> return theClientInstance}
		
		def stubAsyncManager = new StubFor(AsyncConditionManager)
		stubAsyncManager.demand.getRegisteredAsyncConditionsCount(100) {return asyncManagerRegisteredConditionsCount}
		stubAsyncManager.demand.startTicking(100) {isAsyncManagerTicking = true}
		stubAsyncManager.demand.stopTicking(100) {isAsyncManagerTicking = false}
		stubAsyncManager.demand.timeoutAllRemainingAsyncConditions(100) {asyncManagerTimedOutRemainingAsyncConditions = true}
		stubAsyncManager.demand.registerAsyncCondition(100){}
		
		def stubAsyncUpdater = new StubFor(AsyncUpdateHandler)
		stubAsyncUpdater.demand.register(100) {isAsyncUpdateHandlerRegistered = true}

		/*
		* Non-obvious code: The testee.asyncManager and testee.asyncUpdater
		* fields we want to mock are final, thus cannot be set with object.field
		* (final fields must be initialised by the ctor).
		*
		* Create the testee within the use{} block of the stubbed classes. The
		* testee's ctor calls 'new AsyncConditionManager()' and 'new AsyncUpdateHandler'.
		* Since this is within the scope of the stubs use{} blocks stubbed object
		* instance are created.
		*
		* Sorry.
		*/
		stubAsyncManager.use 
		{
			stubAsyncUpdater.use 
			{ 
				testee = new RunResults()
			}
		}
	}	
	
	@Test
	void testAddAssertTrue()
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
	void testOnScriptEnd_NoRegisteredAsyncConditions()
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
		testee.maxWait = 100
		
		assertFalse(asyncManagerTimedOutRemainingAsyncConditions)
		testee.onScriptEnd()
		assertTrue(asyncManagerTimedOutRemainingAsyncConditions)
	}
	
	@Test
	void testOnScriptStart_RegistersAsyncUpdaterAndStartsTicker()
	{
		assertFalse(isAsyncUpdateHandlerRegistered)
		assertFalse(isAsyncManagerTicking)
		
		testee.onScriptStart()
		
		assertTrue(isAsyncUpdateHandlerRegistered)
		assertTrue(isAsyncManagerTicking)
	}
	
	@Test
	void testAssertAsyncEquals_RegistersAsyncAssertObjectWithAsyncManager()
	{
		def isAsyncAssertRunResultRegisteredWithAsyncManager = false
		
		def stubAsyncAssert = new StubFor(AssertAsyncEqualsRunResult)
		stubAsyncAssert.demand.registerWithManager(100) {isAsyncAssertRunResultRegisteredWithAsyncManager = true}

		stubAsyncAssert.use 
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
	
}
