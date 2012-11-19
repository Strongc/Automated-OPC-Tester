package ch.cern.opc.dsl.common.async

import java.util.Map;

import org.apache.commons.lang.NotImplementedException

import ch.cern.opc.common.ItemValue
import ch.cern.opc.dsl.common.results.RunResult
import static ch.cern.opc.dsl.common.results.RunResultUtil.formatMessage
import static ch.cern.opc.dsl.common.async.AsyncState.*

class AssertAsyncNotEqualsRunResult extends AssertAsyncRunResult
{
	public static final def TITLE = 'assertAsyncNotEquals'
	final def message
	private ItemValue theFailedUpdate = null
	
	def AssertAsyncNotEqualsRunResult(message, timeout, itemPath, antiExpectedValue)
	{
		super(timeout, itemPath, antiExpectedValue)
		this.message = formatMessage(message)
	}
	
	@Override
	def toXml(xmlBuilder) 
	{
		def element
		
		switch(state)
		{
			case FAILED:
				element = xmlBuilder.testcase(name:"${TITLE} failed: ${message}")
				{
					failure(message:"item [${itemPath}] received anti-expected value [${itemValue}] at [${failureTimestamp}]. Elapsed wait [${elapsedWait}] seconds")
				}
				break;
			case PASSED:
				element = xmlBuilder.testcase(name:"${TITLE} success: ${message}")
				{
					success(message:"item [${itemPath}] did not match anti-expected value [${itemValue}] in [${elapsedWait}] seconds")
				}
				break;
			case WAITING:
				element = xmlBuilder.testcase(name:"${TITLE} incomplete: ${message}")
				{
					incomplete(message:"item [${itemPath}] still waiting,  anti-expected value [${itemValue}], elapsed wait [${elapsedWait}] seconds")
				}
				break;
			default:
				throw new IllegalStateException("programming error: asynchronous assertions should not be asked for their XML in current state [${state}]")
		}
		return element
	}
	
	@Override
	def timedOut()
	{
		state = PASSED
	}
	
	@Override
	def checkUpdate(itemPath, final ItemValue actualValue)
	{
		if(isItemPathMatch(itemPath))
		{
			if(actualValue != null)
			{
				if(isItemValueMatch(actualValue.value))
				{
					theFailedUpdate = actualValue
					state = FAILED
				}
			}
		}
	}
	
	@Override
	String toString()
	{
		return "AssertAsyncNotEqualsRunResult: item [${itemPath}] anti expected value [${itemValue}]"
	}
	
	private String getFailureTimestamp()
	{
		return (theFailedUpdate != null? theFailedUpdate.timestamp.toString(): 'NULL TIMESTAMP')
	}

}
