package ch.cern.opc.dsl.common.async

import ch.cern.opc.common.Log
import ch.cern.opc.dsl.common.client.UpdateHandler
import static ch.cern.opc.dsl.common.async.AssertAsyncRunResult.ASYNC_STATE.CREATED as CREATED
import static ch.cern.opc.dsl.common.async.AssertAsyncRunResult.ASYNC_STATE.WAITING as WAITING
import static ch.cern.opc.dsl.common.async.AssertAsyncRunResult.ASYNC_STATE.PASSED as PASSED
import static ch.cern.opc.dsl.common.async.AssertAsyncRunResult.ASYNC_STATE.FAILED as FAILED

@Mixin(Log)
class AsyncConditionManager implements UpdateHandler
{
	private final def asyncConditions = []
	private final AsyncTicker ticker = new AsyncTicker()
	
	def synchronized registerAsyncCondition(asyncCondition)
	{
		asyncConditions << asyncCondition 
	}
	
	@Override
	public synchronized void onUpdate(itemId, attributeId, value)
	{
		if(itemId == null || value == null)
		{
			logWarning("WARNING onUpdate called with null - item null [${itemId==null?'Y':'N'}] value null [${value==null?'Y':'N'}]")
		}
		else
		{
			logDebug("asyncUpdateHandler.onUpdate called for item [${itemId}] value [${value}] thread [${Thread.currentThread().id}]")
			asyncUpdate(itemId, value)
		}
	}
	
	private def asyncUpdate(itemPath, actualValue)
	{
		asyncConditions.each
		{
			it.checkUpdate(itemPath, actualValue)
		}
		
		removeNonWaitingAsyncConditions()
	}
	
	def synchronized getRegisteredAsyncConditionsCount()
	{
		return asyncConditions.size() 
	}
	
	def synchronized getMaxConditionTimeout()
	{
		def result = 0
		asyncConditions.each
		{
			if(it.state == WAITING)
			{
				result = it.timeout > result? it.timeout: result
			}
		}
		
		return result
	}
	
	def synchronized onTick()
	{
		asyncConditions.each
		{
			it.onTick()
		}
		
		removeNonWaitingAsyncConditions()
	}
	
	def synchronized startTicking()
	{
		ticker.start(this)
	}
	
	def synchronized stopTicking()
	{
		ticker.stop()	
		removeNonWaitingAsyncConditions()
		logInfo("stopping the asynchronous condition manager - timing out the remaining [${asyncConditions.size()}] conditions")
		asyncConditions.each{it.timedOut()}
	}
	
	private def removeNonWaitingAsyncConditions()
	{
		def nonWaitingAsyncConditions = []
		asyncConditions.each
		{
			if(WAITING != it.state)
			{
				nonWaitingAsyncConditions << it
			}
		}
		
		asyncConditions.removeAll(nonWaitingAsyncConditions)
	}
}
