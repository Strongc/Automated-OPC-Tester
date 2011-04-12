package ch.cern.opc.scriptRunner.results

import static RunResultUtil.formatMessage
import static RunResultUtil.toBoolean

protected class AssertFalseRunResult extends ObservableRunResult
{
	static final def TITLE = 'assertFalse'
	final def message
	final def isPassed
	
	def AssertFalseRunResult(message, actual)
	{
		this.message = formatMessage(message)
		isPassed = (RunResultUtil.AnalyzedBooleanType.FALSE == toBoolean(actual))
	}
	
	def toXml(xmlBuilder)
	{
		def element
		if(isPassed)
		{
			element = xmlBuilder.testcase(name:"${TITLE} passed: ${message}")
			{
				success(message:'value was false')
			}
		}
		else
		{
			element = xmlBuilder.testcase(name:"${TITLE} failed: ${message}")
			{
				failure(message:'value was not false')
			}
		}
	}	
}
