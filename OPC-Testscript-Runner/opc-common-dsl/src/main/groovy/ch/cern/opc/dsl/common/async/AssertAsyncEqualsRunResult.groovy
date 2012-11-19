package ch.cern.opc.dsl.common.async

import static ch.cern.opc.dsl.common.results.RunResultUtil.formatMessage
import static ch.cern.opc.dsl.common.async.AsyncState.*

import ch.cern.opc.common.ItemValue
import java.util.Map;

import org.apache.commons.lang.NotImplementedException

class AssertAsyncEqualsRunResult extends AssertAsyncRunResult
{
	public static final def TITLE = 'assertAsyncEquals'

	final def message

	def AssertAsyncEqualsRunResult(message, timeout, itemPath, expectedValue)
	{
		super(timeout, itemPath, expectedValue)
		this.message = formatMessage(message)
	}

	@Override
	def toXml(xmlBuilder)
	{
		def element

		switch(state)
		{
			case PASSED:
				element = xmlBuilder.testcase(name:"${TITLE} passed: ${message}")
				{
					success(message:"item [${itemPath}] obtained expected value [${itemValue}] in [${elapsedWait}] seconds")
				}
				break;
			case FAILED:
				element = xmlBuilder.testcase(name:"${TITLE} failed: ${message}")
				{
					failure(message:"item [${itemPath}] failed to obtain expected value [${itemValue}] in [${elapsedWait}] seconds")
				}
				break;
			case WAITING:
				element = xmlBuilder.testcase(name:"${TITLE} incomplete: ${message}")
				{
					incomplete(message:"item [${itemPath}] waiting to obtain expected value [${itemValue}], elapsed wait [${elapsedWait}] seconds")
				}
				break;
			default:
				throw new IllegalStateException('programming error: asynchronous assertions should not be asked for their XML in this state')
		}
		return element
	}

	@Override
	String toString()
	{
		return "AssertAsyncEqualsRunResult: item [${itemPath}] expected value [${itemValue}]"
	}

	@Override
	def timedOut()
	{
		state = FAILED
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
					state = PASSED
				}
			}
		}
	}

}
