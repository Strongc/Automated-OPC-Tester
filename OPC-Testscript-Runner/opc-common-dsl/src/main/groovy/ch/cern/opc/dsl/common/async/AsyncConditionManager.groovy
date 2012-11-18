package ch.cern.opc.dsl.common.async

import static ch.cern.opc.common.Log.*
import ch.cern.opc.common.ItemValue
import ch.cern.opc.dsl.common.client.UpdateHandler
import static ch.cern.opc.dsl.common.async.AsyncState.*

class AsyncConditionManager implements UpdateHandler
{
	/*
	 * asyncConditions is a 'string:[]' mapping (string to array)
	 *   string - the item path
	 *   [] - array of async conditions for this item
	 */
	private final def asyncConditions = [:]
	
	private final AsyncTicker ticker = new AsyncTicker()
	
	def registerAsyncCondition(AsyncRunResult condition)
	{
		if(!isValidCondition(condition, 'remove')) return

		logDebug("adding async assertion for item [${condition.itemPath}]")
		synchronized(asyncConditions)
		{
			def itemConditions = asyncConditions[condition.itemPath]
			
			// create collection to hold all conditions for this item, if required
			if(itemConditions == null) 
			{
				itemConditions = []
				asyncConditions[condition.itemPath] = itemConditions
			}
			
			// and add
			itemConditions << condition
			
			asyncConditions[condition]
		} 
	}
	
	def removeAsyncCondition(AsyncRunResult condition)
	{
		if(!isValidCondition(condition, 'remove')) return

		synchronized(asyncConditions)
		{
			def itemConditions = asyncConditions[condition.itemPath]
			
			if(itemConditions != null)
			{
				itemConditions.remove(condition)

				// if was last condition for item remove map entry
				if(itemConditions.isEmpty())
				{
					asyncConditions.remove(condition.itemPath)
				}
			}
		}
	}
	
	private boolean isValidCondition(condition, actionString)
	{
		if(condition == null)
		{
			logWarning("attempt to ${actionString} null condition, probably a programming error")
			return false
		}
		
		if(condition.itemPath == null || condition.itemPath.isEmpty())
		{
			logWarning("attempt to ${actionString} asynchronous condition with invalid item path [${condition.itemPath}]")
			return false
		}

		return true
	}

	
	@Override
	public void onUpdate(itemId, attributeId, ItemValue value)
	{
		if(itemId == null || value == null)
		{
			logWarning("WARNING onUpdate called with null - item null [${itemId==null?'Y':'N'}] value null [${value==null?'Y':'N'}]")
		}
		else
		{
			logDebug("onUpdate called")
			logDebug("-item [${itemId}]")
			logDebug("${value}")
			
			asyncUpdate(itemId, value)
		}
	}
	
	private def asyncUpdate(itemPath, actualValue)
	{
//		long start = System.currentTimeMillis();
				
		logDebug("asyncUpdate started item [${itemPath}] value [${actualValue}] thread [${Thread.currentThread().id}] async conditions count [${asyncConditions.size()}]")
		
		synchronized(asyncConditions)
		{
			def itemConditions = asyncConditions[itemPath]
			itemConditions.each{ it.checkUpdate(itemPath, actualValue) }
		}
		
		removeNonWaitingAsyncConditions()

//		long elapsedTime = System.currentTimeMillis() - start;
//		logError("asyncUpdate completed for item [${itemPath}], elapsed time [${elapsedTime}ms]")
	}
	
	def getRegisteredAsyncConditionsCount()
	{
		int count = 0
		
		synchronized(asyncConditions)
		{
			asyncConditions*.value.each
			{itemConditions->
				count += itemConditions.size()
			}
		} 
		
		return count
	}
	
	def getMaxConditionTimeout()
	{
		int maxTimeout = 0
		
		synchronized(asyncConditions)
		{
			asyncConditions*.value.each
			{itemConditions->
				itemConditions.each
				{condition->
					if(WAITING == condition.state)
					{
						maxTimeout = condition.timeout > maxTimeout? condition.timeout: maxTimeout
					} 
				}
			}
		}
		
		return maxTimeout
	}
	
	def onTick()
	{
		synchronized(asyncConditions)
		{
			asyncConditions*.value.each
			{itemConditions->
				itemConditions.each { it.onTick() }
			}
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
		removeNonWaitingAsyncConditions()
		logInfo("stopping the asynchronous condition manager - timing out the remaining [${asyncConditions.size()}] conditions")
		
		synchronized(asyncConditions)
		{
			asyncConditions*.value.each
			{itemConditions->
				itemConditions.each {it.timedOut() }
			}
		}
	}
	
	private def removeNonWaitingAsyncConditions()
	{
		def nonWaitingAsyncConditions = []
		
		synchronized(asyncConditions)
		{
			// collect non-waiting conditions
			asyncConditions*.value.each
			{itemConditions->
				itemConditions.each 
				{condition->
					if(WAITING != condition.state)
					{
						nonWaitingAsyncConditions << condition
					}
				}
			}
			
			// remove them from the map
			nonWaitingAsyncConditions.each{ removeAsyncCondition(it) }
		}
	}
}
