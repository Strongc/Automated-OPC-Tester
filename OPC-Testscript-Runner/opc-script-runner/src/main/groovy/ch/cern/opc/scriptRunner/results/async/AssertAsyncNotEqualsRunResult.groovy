package ch.cern.opc.scriptRunner.results.async

import org.apache.commons.lang.NotImplementedException

import ch.cern.opc.scriptRunner.results.RunResult
import static ch.cern.opc.scriptRunner.results.RunResultUtil.formatMessage

class AssertAsyncNotEqualsRunResult extends AssertAsyncRunResult
{
	public static final def TITLE = 'assertAsyncNotEquals'
	final def antiExpectedValue
	final def message
	
	def AssertAsyncNotEqualsRunResult(message, timeout, itemPath, antiExpectedValue)
	{
		super(timeout, itemPath)
		this.message = formatMessage(message)
		this.antiExpectedValue = antiExpectedValue
	}
	
	@Override
	def toXml(xmlBuilder) 
	{
		def element
		
		switch(state)
		{
			case ASYNC_STATE.MATCHED:
				element = xmlBuilder.testcase(name:"${TITLE} failed: ${message}")
				{
					failure(message:"item [${itemPath}] matched anti-expected value [${antiExpectedValue}] in [${elapsedWait}] seconds")
				}
				break;
			case ASYNC_STATE.TIMED_OUT:
				element = xmlBuilder.testcase(name:"${TITLE} success: ${message}")
				{
					success(message:"item [${itemPath}] did not match anti-expected value [${antiExpectedValue}] in [${elapsedWait}] seconds")
				}
				break;
			case ASYNC_STATE.WAITING:
				element = xmlBuilder.testcase(name:"${TITLE} incomplete: ${message}")
				{
					incomplete(message:"item [${itemPath}] still waiting,  anti-expected value [${antiExpectedValue}], elapsed wait [${elapsedWait}] seconds")
				}
				break;
			default:
				throw new IllegalStateException("programming error: asynchronous assertions should not be asked for their XML in current state [${state}]")
		}
		return element

	}
	
	@Override
	def checkUpdate(itemPath, actualValue)
	{
		if(this.itemPath.equals(itemPath))
		{
			if(antiExpectedValue.equals(actualValue))
			{
				state = ASYNC_STATE.MATCHED
			}
		}
	}
	
	@Override
	String toString()
	{
		return "AssertAsyncNotEqualsRunResult: item [${itemPath}] anti expected value [${antiExpectedValue}]"
	}
}
