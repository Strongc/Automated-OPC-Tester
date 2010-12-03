package ch.cern.opc.scriptRunner.results.async

import ch.cern.opc.scriptRunner.results.RunResult
import static ch.cern.opc.scriptRunner.results.RunResultUtil.formatMessage

protected class AssertAsyncEqualsRunResult implements RunResult
{
	public static enum ASYNC_STATE {CREATED, WAITING, TIMED_OUT, PASSED}
	
	public static final def TITLE = 'assertAsyncEquals' 
	
	final def timeout
	final def itemPath
	final def expectedValue
	final def message
	
	private ASYNC_STATE state = ASYNC_STATE.CREATED
	private def elapsedWait = 0
	
	def AssertAsyncEqualsRunResult(message, timeout, itemPath, expectedValue)
	{
		this.message = formatMessage(message)
		this.timeout = timeout
		this.itemPath = itemPath
		this.expectedValue = expectedValue
	}
	
	def toXml(xmlBuilder)
	{
		def element
		
		switch(state)
		{
			case ASYNC_STATE.PASSED:
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
	
	def getState()
	{
		return state
	}
	
	def getElapsedWait()
	{
		return elapsedWait
	}
	
	@Override
	String toString()
	{
		return "item [${itemPath}] expected value [${expectedValue}]"	
	}
	
	protected def checkUpdate(itemPath, actualValue)
	{
		if(this.itemPath.equals(itemPath))
		{
			if(this.expectedValue.equals(actualValue))
			{
				state = ASYNC_STATE.PASSED
			}
		}
		println("AssertAsyncEqualsRunResult.checkUpdate - state [${state}] checked input [item:${itemPath} actual:${actualValue}] against this: ${this}")
	}
	
	protected def onTick()
	{
		elapsedWait++
		if(elapsedWait >= timeout)
		{
			state = ASYNC_STATE.TIMED_OUT
		}
		println("AssertAsyncEqualsRunResult.onTick - state [${state}] for this: ${this}")
	}
	
	protected def registerWithManager(def manager)
	{
		state = ASYNC_STATE.WAITING
		manager.registerAsyncAssert(this)
	}
}
