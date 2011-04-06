package ch.cern.opc.scriptRunner.results.async

import static ch.cern.opc.scriptRunner.results.async.AssertAsyncRunResult.ASYNC_STATE.WAITING
import static ch.cern.opc.scriptRunner.results.async.AssertAsyncRunResult.ASYNC_STATE.TIMED_OUT

protected class AsyncConditionManager 
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
	
	def synchronized onTick()
	{
		asyncConditions.each
		{
			it.onTick()
		}
		
		removeNonWaitingAsyncConditions()
	}
	
	def synchronized timeoutAllRemainingAsyncConditions()
	{
		asyncConditions.each
		{
			it.state = TIMED_OUT
		}
		removeNonWaitingAsyncConditions()
	}
	
	def startTicking()
	{
		ticker.start(this)
	}
	
	def stopTicking()
	{
		ticker.stop()	
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
