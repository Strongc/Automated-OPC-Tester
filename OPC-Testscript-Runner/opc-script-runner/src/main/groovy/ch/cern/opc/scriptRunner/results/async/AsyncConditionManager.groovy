package ch.cern.opc.scriptRunner.results.async

import ch.cern.opc.common.Log

import static ch.cern.opc.scriptRunner.results.async.AssertAsyncRunResult.ASYNC_STATE.CREATED as CREATED
import static ch.cern.opc.scriptRunner.results.async.AssertAsyncRunResult.ASYNC_STATE.WAITING as WAITING
import static ch.cern.opc.scriptRunner.results.async.AssertAsyncRunResult.ASYNC_STATE.PASSED as PASSED
import static ch.cern.opc.scriptRunner.results.async.AssertAsyncRunResult.ASYNC_STATE.FAILED as FAILED

@Mixin(Log)
class AsyncConditionManager 
{
	private final def asyncConditions = []
	private final AsyncTicker ticker = new AsyncTicker()
	
	def synchronized registerAsyncCondition(asyncCondition)
	{
		asyncConditions << asyncCondition 
	}
	
	def synchronized asyncUpdate(itemPath, actualValue)
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
