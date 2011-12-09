package ch.cern.opc.dsl.common.results


import groovy.xml.DOMBuilder
import groovy.xml.dom.DOMCategory
import ch.cern.opc.dsl.common.client.GenericClient
import ch.cern.opc.common.Log
import ch.cern.opc.dsl.common.async.AssertAsyncEqualsRunResult;
import ch.cern.opc.dsl.common.async.AssertAsyncNotEqualsRunResult;
import ch.cern.opc.dsl.common.async.AsyncConditionManager;

import org.w3c.dom.*;
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
	
	def assertTrue(message, value)
	{
		add(new AssertTrueRunResult(message, value))
	}
	
	def assertFalse(message, value)
	{
		add(new AssertFalseRunResult(message, value))
	}
	
	def assertEquals(message, expected, actual)
	{
		add(new AssertEqualsRunResult(message, expected, actual, client))
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
	
	def setPingPeriodInSeconds(pingPeriod)
	{
		this.pingPeriod = pingPeriod
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
