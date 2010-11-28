package ch.cern.opc.scriptRunner.results

import ch.cern.opc.client.ClientInstance

protected class AssertEqualsRunResult implements RunResult 
{
	final static def NULL_MSG = "null assertion message";
	final static def EMPTY_MSG = "empty assertion message";
	
	final def title = 'assertEquals'
	
	final def message
	final def outputMessage
	final boolean isPassed
	
	def AssertEqualsRunResult(message, expected, actual)
	{
		this.message = formatMessage(message)
		
		// ensure expected and actual compared as strings
		if(expected.toString().equals(actual.toString()))
		{
			outputMessage = formatOutputMessage(expected, actual)
			isPassed = true
		}
		else
		{
			outputMessage = formatOutputMessage(expected, actual) + " last error from dll [${ClientInstance.instance.lastError}]"
			isPassed = false
		}
	}
	
	def toXml(xmlBuilder)
	{
		def element
		if(isPassed)
		{
			element = xmlBuilder.testcase(name:"assertEquals passed: ${message}")
			{
				success(message:outputMessage)
			}
		}
		else
		{
			element = xmlBuilder.testcase(name:"assertEquals failed: ${message}")
			{
				failure(message:outputMessage)
			}
		}
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

	private def formatOutputMessage(expected, actual)
	{
		return "expected [${expected}] actual [${actual}]"
	}
}
