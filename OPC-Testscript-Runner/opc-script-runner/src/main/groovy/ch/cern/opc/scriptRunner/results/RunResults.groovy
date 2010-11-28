package ch.cern.opc.scriptRunner.results


import groovy.xml.DOMBuilder
import ch.cern.opc.client.ClientInstance
import ch.cern.opc.scriptRunner.AsyncUpdateHandler

class RunResults 
{
	final static def NULL_MSG = "null assertion message";
	final static def EMPTY_MSG = "empty assertion message";
	
	def passes = []	
	def failures = []
	def exceptions = []
	def asyncUpdater = new AsyncUpdateHandler();
	
	def assertTrue(message, Boolean value)
	{
		if(value)
		{
			addPass('assertTrue', formatMessage(message))
		}
		else
		{
			addFail('assertTrue', formatMessage(message))
		}
	}
	
	def assertFalse(message, Boolean value)
	{
		if(!value)
		{
			addPass('assertFalse', formatMessage(message))
		}
		else
		{
			addFail('assertFalse', formatMessage(message))
		}
	}
	
	def assertTrue(message, value)
	{
		if(value.isNumber())
		{
			return assertTrue(message, value.toBigDecimal() != 0)
		}
		else
		{
			return assertTrue(message, value.toString().toBoolean())
		}
	}
	
	def assertFalse(message, value)
	{
		if(value.isNumber())
		{
			return assertFalse(message, value.toBigDecimal() != 0)
		}
		else
		{
			return assertFalse(message, value.toString().toBoolean())
		}
	}
	
	def assertEquals(message, expected, actual)
	{
		// ensure expected and actual compared as strings
		if(expected.toString().equals(actual.toString()))
		{
			addPass('assertEquals', formatMessage(message))
		}
		else
		{
			addFail('assertEquals', formatMessage(message, expected, actual))
		}
	}
	
	def assertAsyncEquals(message, timeoutMs, expected, itemPath)
	{
		asyncUpdater.register()
	}
	
	def getXML()
	{
		def xmlBuilder = DOMBuilder.newInstance()
		
		def xml = xmlBuilder.testsuites(name:'OPC Test Script Runner', tests:"${passes.size+failures.size}", failures:"${failures.size + exceptions.size}", disabled:'0', errors:'0', time:'0')
		{
			testsuite(name:'Tests', tests:"${passes.size+failures.size}", failures:"${failures.size + exceptions.size}", disabled:'0', errors:'0', time:'0')
			{
				passes.each{pass->
					testcase(name:"${pass}")
					{
						success(message:'test passed')
					}
				}
				failures.each{fail->
					testcase(name:"${fail}")
					{
						failure(message:"failed: last error from dll [${ClientInstance.instance.lastError}]")
					}
				}
				exceptions.each{e->
					exception(name:'exception', message:"${e.message}")
					{
						e.stackTrace.each
						{
							line(line:"${it.toString()}")
						}
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
	
	def addPass(testTypeName, message)
	{
		passes.add("${testTypeName} passed - message: ${message}")
	}
	
	def addFail(testTypeName, message)
	{
		failures.add("${testTypeName} failed - message: ${message}")
	}
	
	def addException(Exception e)
	{
		exceptions.add(e)
	}
}
