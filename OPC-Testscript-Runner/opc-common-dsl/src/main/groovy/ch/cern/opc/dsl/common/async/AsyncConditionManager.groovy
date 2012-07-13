package ch.cern.opc.dsl.common.async

import ch.cern.opc.common.Log
import ch.cern.opc.dsl.common.client.UpdateHandler
import static ch.cern.opc.dsl.common.async.AsyncState.*

@Mixin(Log)
class AsyncConditionManager implements UpdateHandler
{
	private final def asyncConditions = []
	private final AsyncTicker ticker = new AsyncTicker()
	
	def AsyncConditionManager()
	{
		logTrace("AsyncConditionManager instance created")
	}
	
	def synchronized registerAsyncCondition(asyncCondition)
	{
		asyncConditions << asyncCondition 
	}
	
	@Override
	public void onUpdate(itemId, attributeId, value, quality, type, timestamp)
	{
		if(itemId == null || value == null)
		{
			logWarning("WARNING onUpdate called with null - item null [${itemId==null?'Y':'N'}] value null [${value==null?'Y':'N'}]")
		}
		else
		{
			logDebug("onUpdate called")
			logDebug("-item [${itemId}]")
			logDebug("-value [${value}]")
			logDebug("-quality [${quality}]")
			logDebug("-type [${type}]")
			logDebug("-timestamp [${timestamp}]")
			logDebug("-thread [${Thread.currentThread().id}]")
			
			asyncUpdate(itemId, value)
		}
	}
	
	private def asyncUpdate(itemPath, actualValue)
	{
		logTrace("asyncUpdate started item [${itemPath}] value [${actualValue}] thread [${Thread.currentThread().id}] async conditions count [${asyncConditions.size()}]")
		synchronized(asyncConditions)
		{
			asyncConditions.each
			{
				it.checkUpdate(itemPath, actualValue)
			}
			
			removeNonWaitingAsyncConditions()
		}
		logTrace("asyncUpdate completed")
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
