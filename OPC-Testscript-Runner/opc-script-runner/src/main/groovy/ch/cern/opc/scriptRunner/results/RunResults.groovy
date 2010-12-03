package ch.cern.opc.scriptRunner.results


import groovy.xml.DOMBuilder
import groovy.xml.dom.DOMCategory
import ch.cern.opc.client.ClientInstance
import ch.cern.opc.scriptRunner.AsyncUpdateHandler

import org.w3c.dom.*;
import javax.xml.parsers.*
import ch.cern.opc.scriptRunner.results.async.AssertAsyncEqualsRunResult;
import ch.cern.opc.scriptRunner.results.async.AssertAsynchManager

class RunResults 
{
	private static final def PING_PERIOD_FOR_ASYNC_COMPLETION = 2000
	private static final def MAX_WAIT_FOR_ASYNC_COMPLETION_MS = 60000
	
	def results = []	
	private final def asyncManager
	private final def asyncUpdater
	
	def RunResults()
	{
		asyncManager = new AssertAsynchManager()
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
		if(asyncManager.registeredAsyncAssertsCount > 0)
		{
			for(def elapsedWait = 0; elapsedWait < MAX_WAIT_FOR_ASYNC_COMPLETION_MS && asyncManager.registeredAsyncAssertsCount > 0; elapsedWait += PING_PERIOD_FOR_ASYNC_COMPLETION)
			{
				logInfo("waiting for [${asyncManager.registeredAsyncAssertsCount}] asynchronous asserts to complete... waiting for [${MAX_WAIT_FOR_ASYNC_COMPLETION_MS-elapsedWait}]")
				sleep(PING_PERIOD_FOR_ASYNC_COMPLETION)
			}   
		}
		asyncManager.stopTicking()
	}

}
