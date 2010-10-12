package ch.cern.opc.scriptRunner

import org.junit.Assert
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
			Assert.assertTrue(value)
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
			Assert.assertEquals(expected, actual)
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
		
		def xml = xmlBuilder.testsuites(name:'OPC Test Script Runner', tests:"${passes.size+failures.size}", failures:"${failures.size}", disabled:'0', errors:'0', time:'0')
		{
			testsuite(name:'Tests', tests:"${passes.size+failures.size}", failures:"${failures.size}", disabled:'0', errors:'0', time:'0')
			{
				passes.each{pass->
					testcase(name:"${pass}")
				}
				failures.each{fail->
					testcase(name:"${fail}")
					{
						failure(message:'failed')
					}
				}
			}
		}
		return xml
	}
	
	def formatMessage(message)
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

	def formatMessage(message, expected, actual)
	{
		return formatMessage(message) + " expected [${expected}] actual [${actual}]"
	}
}
