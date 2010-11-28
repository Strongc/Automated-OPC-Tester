package ch.cern.opc.scriptRunner.results


import groovy.xml.DOMBuilder
import groovy.xml.dom.DOMCategory
import ch.cern.opc.client.ClientInstance
import ch.cern.opc.scriptRunner.AsyncUpdateHandler

import org.w3c.dom.*;
import javax.xml.parsers.*

class RunResults 
{
	def results = []	
	def asyncUpdater = new AsyncUpdateHandler();
	
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
		asyncUpdater.register()
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
}
