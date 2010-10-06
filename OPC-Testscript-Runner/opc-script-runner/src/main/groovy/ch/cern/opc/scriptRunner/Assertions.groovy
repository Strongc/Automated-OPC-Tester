package ch.cern.opc.scriptRunner

import static org.junit.Assert.*
import groovy.xml.DOMBuilder

class Assertions 
{
	final static def NULL_MSG = "null assertion message";
	final static def EMPTY_MSG = "empty assertion message";

	def passes = []	
	def failures = []
	
	def assertTrue(message, value)
	{
		def loggedMessage = formatMessage(message)
		try
		{
			org.junit.Assert.assertTrue(value)
			passes.add("assertTrue passed - message: ${loggedMessage}")
		}
		catch(AssertionError e)
		{
			failures.add("assertTrue failed - message: ${loggedMessage}") 
		}
	}
	
	def assertEquals(message, expected, actual)
	{
		try
		{
			org.junit.Assert.assertEquals(expected, actual)
			passes.add("assertEquals passed - message: ${formatMessage(message)}")
		}
		catch(AssertionError e)
		{
			failures.add("assertEquals failed - message: ${formatMessage(message, expected, actual)}")
		}
	}
	
	def getXML()
	{
		def xmlBuilder = DOMBuilder.newInstance()
		def output = xmlBuilder.person(x:123,  name:'James', cheese:'edam') 
		{
		    project(name:'groovy')
		    project(name:'geronimo')
		}
		return output
	}
	
	private def formatMessage(message)
	{
		if(message == null)
		{
			return NULL_MSG
		}
		
		if(message.trim().empty)
		{
			return EMPTY_MSG
		}
		
		return message
	}
	
	private def formatMessage(message, expected, actual)
	{
		return formatMessage(message) + " expected [${expected}] actual [${actual}]"
	}
}
