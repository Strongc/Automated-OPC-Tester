package ch.cern.opc.dsl.common.results


import groovy.xml.DOMBuilder
import groovy.xml.dom.DOMCategory
import ch.cern.opc.dsl.common.client.GenericClient
import ch.cern.opc.common.Log
import ch.cern.opc.dsl.common.async.AssertAsyncEqualsRunResult
import ch.cern.opc.dsl.common.async.AssertAsyncNotEqualsRunResult
import ch.cern.opc.dsl.common.async.AssertAsyncQualityRunResult
import ch.cern.opc.dsl.common.async.AsyncConditionManager
import ch.cern.opc.dsl.common.sync.SynchronousAssertion

import static ch.cern.opc.dsl.common.results.RunResultUtil.toBoolean
import static ch.cern.opc.dsl.common.results.RunResultUtil.AnalyzedBooleanType

import org.w3c.dom.*
import javax.xml.parsers.*

@Mixin(Log)
class RunResults extends Observable
{
	private final AsyncConditionManager asyncManager
	private def client
	
	private final def results = []
	
	private def pingPeriod = 10 //seconds
	
	def RunResults()
	{
		asyncManager = new AsyncConditionManager()
	}
	
	def setClient(GenericClient client)
	{
		println("Setting the RunResult client implementation")
		this.client = client
		client.registerForAsyncUpdates(asyncManager);
	}
	
	def assertTrue(userMessage, actualValue)
	{
		def isPassed = AnalyzedBooleanType.TRUE == toBoolean(actualValue)
		
		def assertion = new SynchronousAssertion('assertTrue', isPassed, userMessage, 
			isPassed? 'value was true': 'value was not true'
		) 
		 
		add(assertion)
	}
	
	def assertFalse(userMessage, actualValue)
	{
		def isPassed = (AnalyzedBooleanType.FALSE == toBoolean(actualValue))
		
		def assertion = new SynchronousAssertion('assertFalse', isPassed, userMessage,
			isPassed? 'value was false': 'value was not false'
		) 
		
		add(assertion)
	}
	
	def assertEquals(userMessage, expectedValue, actualValue)
	{
		def isPassed = expectedValue.toString().equals(actualValue.toString())
		
		def assertion = new SynchronousAssertion('assertEquals', isPassed, userMessage,
			"expected [${expectedValue}] to equal actual [${actualValue}]" + (isPassed?"": " last error from dll [${client.lastError}]")
		) 
		
		add(assertion)
	}
	
	def assertNotEquals(userMessage, expectedValue, actualValue)
	{
		def isPassed = !(expectedValue.toString().equals(actualValue.toString()))
		
		def assertion = new SynchronousAssertion('assertNotEquals', isPassed, userMessage,
			"expected [${expectedValue}] to not equal actual [${actualValue}]" + (isPassed?"": " last error from dll [${client.lastError}]")
		)
		
		add(assertion)
	}
	
	def assertQuality(userMessage, expectedQuality, actualQuality)
	{
		def isPassed = actualQuality.equals(expectedQuality)
		
		def assertion = new SynchronousAssertion('assertQuality', isPassed, userMessage,
			"expected [${expectedQuality}] actual [${actualQuality}]" + (!isPassed?" last error from dll [${client.lastError}]":"")
		) 
		
		add(assertion)
	}
	
	def assertDatatype(userMessage, expectedDatatype, actualDatatype)
	{
		def isPassed = actualDatatype.equals(expectedDatatype)
		
		def assertion = new SynchronousAssertion('assertDatatype', isPassed, userMessage,
			"expected [${expectedDatatype}] actual [${actualDatatype}]" + (!isPassed?" last error from dll [${client.lastError}]":"")
		) 
		
		add(assertion)
	}
	
	def assertAccessRights(userMessage, expectedRights, actualRights)
	{
		def isPassed = actualRights.equals(expectedRights)
		
		def assertion = new SynchronousAssertion('assertAccessRights', isPassed, userMessage,
			"expected [${expectedRights}] actual [${actualRights}]" + (!isPassed?" last error from dll [${client.lastError}]":"")
		)
		
		add(assertion)
	}
	
	def addException(exception)
	{
		add(new ExceptionRunResult(exception))
	}
	
	def assertAsyncEquals(message, timeoutMs, expected, itemPath)
	{
		addAsyncAssertion(new AssertAsyncEqualsRunResult(message, timeoutMs, itemPath, expected))
	}
	
	def assertAsyncNotEquals(message, timeoutMs, antiExpected, itemPath)
	{
		addAsyncAssertion(new AssertAsyncNotEqualsRunResult(message, timeoutMs, itemPath, antiExpected))
	}
	
	def assertAsyncQuality(message, timeoutMs, expectedQuality, itemPath)
	{
		addAsyncAssertion(new AssertAsyncQualityRunResult(message, timeoutMs, itemPath, expectedQuality))
	}
	
	def setPingPeriodInSeconds(pingPeriod)
	{
		this.pingPeriod = pingPeriod
	}
	
	def fail(message)
	{
		add(new SynchronousAssertion('fail', false, message, 'explicit failure'))
	}

	private def addAsyncAssertion(def asyncAssertion)
	{
		asyncAssertion.registerWithManager(asyncManager)
		add(asyncAssertion)
	}
	
	def getXML()
	{
		def xmlBuilder = DOMBuilder.newInstance()
		def root = xmlBuilder.testsuite(name:'OPC tests')

		results.each {
			def element = it.toXml(xmlBuilder).cloneNode(true)
			root.appendChild(element)
		}
		
		return root
	}
	
	def onScriptStart()
	{
		logInfo('onScriptStart called')
		asyncManager.startTicking()
	}
	
	def onScriptEnd()
	{
		logInfo("onScriptEnd called")
		
		for(def i = asyncManager.registeredAsyncConditionsCount; i>0; i = asyncManager.registeredAsyncConditionsCount)
		{
			def snoozeTm = Math.min(asyncManager.maxConditionTimeout, pingPeriod) 
			logInfo("waiting for [${i}] asynchronous asserts to complete... max wait[${asyncManager.maxConditionTimeout}s], check again in [${snoozeTm}s]")
			sleep(snoozeTm*1000)
		}

		asyncManager.stopTicking()
		logInfo("stopped ticking, remaining conditions count [${asyncManager.registeredAsyncConditionsCount}]")
		
		logInfo("Cleaning up client connection")
		client.cleanUp()
		
		logInfo('onScriptEnd complete')
	}
	
	private def add(result)
	{
		results.add(result)
		updateObservers(result)
	}
	
	private def updateObservers(result)
	{
		setChanged()
		notifyObservers(result)
	}
}
