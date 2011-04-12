package ch.cern.opc.scriptRunner.results

import static RunResultUtil.formatMessage
import static RunResultUtil.toBoolean
import static RunResultUtil.AnalyzedBooleanType

protected class AssertTrueRunResult extends ObservableRunResult
{
	static final def TITLE = 'assertTrue'
	final def message
	final def isPassed
	
	def AssertTrueRunResult(message, actual)
	{
		this.message = formatMessage(message)
		isPassed = (RunResultUtil.AnalyzedBooleanType.TRUE == toBoolean(actual))
	}
	
	def toXml(xmlBuilder)
	{
		def element
		if(isPassed)
		{
			element = xmlBuilder.testcase(name:"${TITLE} passed: ${message}")
			{
				success(message:'value was true')
			}
		}
		else
		{
			element = xmlBuilder.testcase(name:"${TITLE} failed: ${message}")
			{
				failure(message:'value was not true')
			}
		}
	}	
}
