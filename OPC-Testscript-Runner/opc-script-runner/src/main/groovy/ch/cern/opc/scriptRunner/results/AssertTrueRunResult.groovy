package ch.cern.opc.scriptRunner.results

import static RunResultUtil.formatMessage

class AssertTrueRunResult implements RunResult
{
	final def title = 'assertTrue'
	final def message
	final def isPassed
	
	def AssertTrueRunResult(message, actual)
	{
		this.message = formatMessage(message)
		isPassed = toBoolean(actual)
	}
	
	def toXml(xmlBuilder)
	{
		def element
		if(isPassed)
		{
			element = xmlBuilder.testcase(name:"${title} passed: ${message}")
			{
				success(message:'value was true')
			}
		}
		else
		{
			element = xmlBuilder.testcase(name:"${title} failed: ${message}")
			{
				failure(message:'value was not true')
			}
		}
	}
	
	private def toBoolean(actual)
	{
		if(actual != null)
		{
			if(actual.class == String)
			{
				return Boolean.toBoolean(actual)
			}
			else
			{
				return actual.asBoolean()
			}
		}
		return false
	}
}
