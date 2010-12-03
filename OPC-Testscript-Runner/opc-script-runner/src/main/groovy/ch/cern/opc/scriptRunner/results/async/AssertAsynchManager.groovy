package ch.cern.opc.scriptRunner.results.async

import static ch.cern.opc.scriptRunner.results.async.AssertAsyncEqualsRunResult.ASYNC_STATE.WAITING
import static ch.cern.opc.scriptRunner.results.async.AssertAsyncEqualsRunResult.ASYNC_STATE.TIMED_OUT

protected class AssertAsynchManager 
{
	private final def asyncAsserts = []
	private final AssertAsynchTicker ticker = new AssertAsynchTicker()
	
	def synchronized registerAsyncAssert(asyncAssert)
	{
		asyncAsserts << asyncAssert 
	}
	
	def synchronized asyncUpdate(itemPath, actualValue)
	{
		asyncAsserts.each
		{
			it.checkUpdate(itemPath, actualValue)
		}
		
		removeNonWaitingAsyncAsserts()
	}
	
	def synchronized getRegisteredAsyncAssertsCount()
	{
		return asyncAsserts.size() 
	}
	
	def synchronized onTick()
	{
		asyncAsserts.each
		{
			it.onTick()
		}
		
		removeNonWaitingAsyncAsserts()
	}
	
	def synchronized timeoutAllRemainingAsyncAsserts()
	{
		asyncAsserts.each
		{
			it.state = TIMED_OUT
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
