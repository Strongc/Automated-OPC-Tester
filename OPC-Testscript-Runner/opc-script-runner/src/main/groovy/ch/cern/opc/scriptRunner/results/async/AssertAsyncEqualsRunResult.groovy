package ch.cern.opc.scriptRunner.results.async

import static ch.cern.opc.scriptRunner.results.RunResultUtil.formatMessage

protected class AssertAsyncEqualsRunResult extends AssertAsyncRunResult
{
	public static final def TITLE = 'assertAsyncEquals' 
	
	final def expectedValue
	final def message
	
	def AssertAsyncEqualsRunResult(message, timeout, itemPath, expectedValue)
	{
		super(timeout, itemPath)
		this.message = formatMessage(message)
		this.expectedValue = expectedValue
	}
	
	@Override
	def toXml(xmlBuilder)
	{
		def element
		
		switch(state)
		{
			case ASYNC_STATE.MATCHED:
				element = xmlBuilder.testcase(name:"${TITLE} passed: ${message}")
				{
					success(message:"item [${itemPath}] obtained expected value [${expectedValue}] in [${elapsedWait}] seconds")
				}
				break;
			case ASYNC_STATE.TIMED_OUT:
				element = xmlBuilder.testcase(name:"${TITLE} failed: ${message}")
				{
					failure(message:"item [${itemPath}] failed to obtain expected value [${expectedValue}] in [${elapsedWait}] seconds")
				}
				break;
			case ASYNC_STATE.WAITING:
				element = xmlBuilder.testcase(name:"${TITLE} incomplete: ${message}")
				{
					incomplete(message:"item [${itemPath}] waiting to obtain expected value [${expectedValue}], elapsed wait [${elapsedWait}] seconds")
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
		return "AssertAsyncEqualsRunResult: item [${itemPath}] expected value [${expectedValue}]"	
	}
	
	@Override
	def checkUpdate(itemPath, actualValue)
	{
		if(this.itemPath.equals(itemPath))
		{
			if(this.expectedValue.equals(actualValue))
			{
				state = ASYNC_STATE.MATCHED
			}
		}
		println("AssertAsyncEqualsRunResult.checkUpdate - state [${state}] checked input [item:${itemPath} actual:${actualValue}] against this: ${this}")
	}
}
