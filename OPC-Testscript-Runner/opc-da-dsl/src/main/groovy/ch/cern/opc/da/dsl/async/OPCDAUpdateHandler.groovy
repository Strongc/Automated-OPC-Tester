package ch.cern.opc.da.dsl.async

import ch.cern.opc.client.UpdateValue
import ch.cern.opc.dsl.common.client.UpdateHandler
import java.util.concurrent.BlockingDeque
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.TimeUnit;

import static ch.cern.opc.common.Log.*

class OPCDAUpdateHandler 
{
	private static final def UPDATE_Q_TIMEOUT_POLL = 100 //ms
	final UpdateHandler genericHandler
	
	/*
	 * Thread control values
	 */
	private boolean stopUpdaterThread = true
	private boolean isUpdaterThreadRunning = false
	private final static def THREAD_CMD_TIMEOUT = 100 //ms
	private final static def THREAD_CMD_CHECK = 10 //ms
	
	/*
	 *  updates direct from the callback (i.e. from native code) arrive
	 *  and are pushed into this queue until the updater thread has a
	 *  chance to process them.
	 */
	final BlockingDeque<UpdateValue> updatesQueue = new LinkedBlockingDeque<UpdateValue>()
	
	def OPCDAUpdateHandler(UpdateHandler genericHandler)
	{
		this.genericHandler = genericHandler
	}
	
	def boolean startUpdaterThread()
	{
		if(isUpdaterThreadRunning) return true
		
		def updaterThread = {
			logDebug("UpdaterThread started - thread handles updates from OPC client dll")
			isUpdaterThreadRunning = true
			
			while(!stopUpdaterThread)
			{
				def update = updatesQueue.pollFirst(UPDATE_Q_TIMEOUT_POLL, TimeUnit.MILLISECONDS)
				if(update != null)
				{
					genericHandler.onUpdate(update.itemPath, null, update.value, update.quality, update.type, update.timestamp)
				}
			}
			
			logDebug("UpdaterThread stopped - updates from OPC client dll will not be handled");
			isUpdaterThreadRunning = false
		}
		
		stopUpdaterThread = false
		Thread.start(updaterThread)
		return waitForThreadRunningState(true)
	}
	
	def boolean stopUpdaterThread()
	{
		stopUpdaterThread = true
		return waitForThreadRunningState(false)
	}
	
	def boolean waitForThreadRunningState(def targetState)
	{
		for(def elapsedWait = 0; elapsedWait < THREAD_CMD_TIMEOUT; elapsedWait += THREAD_CMD_CHECK)
		{
			if(isUpdaterThreadRunning == targetState) return true
			Thread.sleep(THREAD_CMD_CHECK);
		}
		
		return isUpdaterThreadRunning == targetState;
	}
}
