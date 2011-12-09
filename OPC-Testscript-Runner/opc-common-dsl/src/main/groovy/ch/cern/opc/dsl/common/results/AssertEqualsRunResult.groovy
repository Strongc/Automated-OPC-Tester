package ch.cern.opc.dsl.common.results

import ch.cern.opc.dsl.common.client.GenericClient
import static RunResultUtil.formatMessage

protected class AssertEqualsRunResult extends ObservableRunResult 
{
	static final def TITLE = 'assertEquals'
	
	final def message
	final def outputMessage
	final boolean isPassed
	
	def AssertEqualsRunResult(message, expected, actual, client)
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
			outputMessage = formatOutputMessage(expected, actual) + " last error from dll [${client.lastError}]"
			isPassed = false
		}
	}
	
	def toXml(xmlBuilder)
	{
		def element
		if(isPassed)
		{
			element = xmlBuilder.testcase(name:"${TITLE} passed: ${message}")
			{
				success(message:outputMessage)
			}
		}
		else
		{
			element = xmlBuilder.testcase(name:"${TITLE} failed: ${message}")
			{
				failure(message:outputMessage)
			}
		}
		return element
	}

	private def formatOutputMessage(expected, actual)
	{
		return "expected [${expected}] actual [${actual}]"
	}
}
