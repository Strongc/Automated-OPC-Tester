package ch.cern.opc.scriptRunner.results.async

import static ch.cern.opc.scriptRunner.results.async.AssertAsyncEqualsRunResult.ASYNC_STATE.WAITING

protected class AssertAsynchManager 
{
	private final def asyncAsserts = []
	private final AssertAsynchTicker ticker = new AssertAsynchTicker()
	
	def registerAsyncAssert(asyncAssert)
	{
		asyncAsserts << asyncAssert 
	}
	
	def asyncUpdate(itemPath, actualValue)
	{
		asyncAsserts.each
		{
			it.checkUpdate(itemPath, actualValue)
		}
		
		removeNonWaitingAsyncAsserts()
	}
	
	def getRegisteredAsyncAssertsCount()
	{
		return asyncAsserts.size() 
	}
	
	def onTick()
	{
		asyncAsserts.each
		{
			it.onTick()
		}
		
		removeNonWaitingAsyncAsserts()
	}
	
	def startTicking()
	{
		ticker.start(this)
	}
	
	def stopTicking()
	{
		ticker.stop()	
	}
	
	private def removeNonWaitingAsyncAsserts()
	{
		def nonWaitingAsyncAsserts = []
		asyncAsserts.each
		{
			if(WAITING != it.state)
			{
				nonWaitingAsyncAsserts << it
			}
		}
		
		asyncAsserts.removeAll(nonWaitingAsyncAsserts)
	}
}
