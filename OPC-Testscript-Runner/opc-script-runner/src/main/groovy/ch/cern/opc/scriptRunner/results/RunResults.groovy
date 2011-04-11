package ch.cern.opc.scriptRunner.results


import groovy.xml.DOMBuilder
import groovy.xml.dom.DOMCategory
import ch.cern.opc.client.ClientInstance
import ch.cern.opc.scriptRunner.AsyncUpdateHandler
import ch.cern.opc.common.Log

import org.w3c.dom.*;
import javax.xml.parsers.*
import ch.cern.opc.scriptRunner.results.async.AssertAsyncEqualsRunResult
import ch.cern.opc.scriptRunner.results.async.AssertAsyncNotEqualsRunResult
import ch.cern.opc.scriptRunner.results.async.AsyncConditionManager

@Mixin([Log, RunResultsArray])
class RunResults
{
	private final def asyncManager
	private final def asyncUpdater
	
	private def pingPeriod = 10 //seconds
	
	def RunResults()
	{
		asyncManager = new AsyncConditionManager()
		asyncUpdater = new AsyncUpdateHandler(asyncManager)
	}
	
	def assertTrue(message, value)
	{
		println 'calling RunResults.add'
		add(new AssertTrueRunResult(message, value))
		println 'called RunResults.add'
	}
	
	def assertFalse(message, value)
	{
		add(new AssertFalseRunResult(message, value))
	}
	
	def assertEquals(message, expected, actual)
	{
		add(new AssertEqualsRunResult(message, expected, actual))
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
		asyncUpdater.register()
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
		logInfo('onScriptEnd complete')
	}

}
