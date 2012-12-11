package ch.cern.opc.dsl.common.results

import static RunResultUtil.formatMessage

class AssertDatatypeRunResult extends ObservableRunResult 
{
	static final def TITLE = 'assertDatatype'
	final def message
	final def outputMessage
	final def isPassed
	
	def AssertDatatypeRunResult(message, expected, actual, client)
	{
		this.message = formatMessage(message)
		isPassed = actual.equals(expected)
		outputMessage = "expected [${expected}] actual [${actual}]" + (!isPassed?" last error from dll [${client.lastError}]":"")
	}

	@Override
	public Object toXml(Object xmlBuilder) 
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

}
