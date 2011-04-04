package ch.cern.opc.scriptRunner.results


import groovy.xml.DOMBuilder
import groovy.xml.dom.DOMCategory
import ch.cern.opc.client.ClientInstance
import ch.cern.opc.scriptRunner.AsyncUpdateHandler
import ch.cern.opc.common.Log

import org.w3c.dom.*;
import javax.xml.parsers.*
import ch.cern.opc.scriptRunner.results.async.AssertAsyncEqualsRunResult;
import ch.cern.opc.scriptRunner.results.async.AsyncConditionManager

@Mixin(Log)
class RunResults 
{
	private static final def PING_PERIOD_FOR_ASYNC_COMPLETION = 2000
	private static final def MAX_WAIT_FOR_ASYNC_COMPLETION_MS = 60000
	
	def results = []	
	private final def asyncManager
	private final def asyncUpdater
	protected def maxWait = MAX_WAIT_FOR_ASYNC_COMPLETION_MS
	
	def RunResults()
	{
		asyncManager = new AsyncConditionManager()
		asyncUpdater = new AsyncUpdateHandler(asyncManager)
	}
	
	def assertTrue(message, value)
	{
		results.add(new AssertTrueRunResult(message, value))
	}
	
	def assertFalse(message, value)
	{
		results.add(new AssertFalseRunResult(message, value))
	}
	
	def assertEquals(message, expected, actual)
	{
		results.add(new AssertEqualsRunResult(message, expected, actual))
	}
	
	def addException(exception)
	{
		results.add(new ExceptionRunResult(exception))
	}
	
	def assertAsyncEquals(message, timeoutMs, expected, itemPath)
	{
		def assertAsync = new AssertAsyncEqualsRunResult(message, timeoutMs, itemPath, expected)
		assertAsync.registerWithManager(asyncManager)
		
		results.add(assertAsync)
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
		logInfo('onScriptEnd called')
		if(asyncManager.registeredAsyncConditionsCount > 0)
		{
			for(def elapsedWait = 0; elapsedWait < maxWait && asyncManager.registeredAsyncConditionsCount > 0; elapsedWait += PING_PERIOD_FOR_ASYNC_COMPLETION)
			{
				logInfo("waiting for [${asyncManager.registeredAsyncConditionsCount}] asynchronous asserts to complete... waiting for [${maxWait-elapsedWait}]")
				sleep(PING_PERIOD_FOR_ASYNC_COMPLETION)
			}   
			asyncManager.timeoutAllRemainingAsyncConditions()
		}
		asyncManager.stopTicking()
	}

}
