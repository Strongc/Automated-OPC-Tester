package ch.cern.opc.dsl.common.async

import ch.cern.opc.dsl.common.results.RunResult;
import ch.cern.opc.dsl.common.results.ObservableRunResult;
import groovy.xml.DOMBuilder

abstract class AssertAsyncRunResult extends ObservableRunResult implements AsyncRunResult 
{
	public static enum ASYNC_STATE {CREATED, WAITING, PASSED, FAILED}
	
	private ASYNC_STATE state = ASYNC_STATE.CREATED
	protected def elapsedWait = 0
	
	protected final def timeout
	protected final def itemPath
	
	AssertAsyncRunResult(timeout, itemPath)
	{
		this.timeout = timeout
		this.itemPath = itemPath
	}
	
	def getState()
	{
		return state
	}
	
	protected setState(newState)
	{
		if(newState != state)
		{
			state = newState
			if(countObservers() > 0)
			{
				setChanged()
				notifyObservers(this.toXml(DOMBuilder.newInstance()))
			}
		}
	}
	
	def getElapsedWait()
	{
		return elapsedWait
	}
	
	protected def onTick()
	{
		elapsedWait++
		if(elapsedWait >= timeout)
		{
			timedOut()
		}
		println("AssertAsyncEqualsRunResult.onTick - state [${state}] for this: ${this}")
	}
	
	protected def registerWithManager(def manager)
	{
		state = ASYNC_STATE.WAITING
		manager.registerAsyncCondition(this)
	}
}
