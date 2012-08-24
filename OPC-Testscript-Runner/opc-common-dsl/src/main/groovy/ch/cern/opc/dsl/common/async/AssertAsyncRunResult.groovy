package ch.cern.opc.dsl.common.async

import java.util.Map;

import ch.cern.opc.dsl.common.results.RunResult
import ch.cern.opc.dsl.common.results.ObservableRunResult
import groovy.xml.DOMBuilder
import static ch.cern.opc.dsl.common.async.AsyncState.*
import static ch.cern.opc.common.Log.*

abstract class AssertAsyncRunResult extends ObservableRunResult implements AsyncRunResult 
{
	private AsyncConditionManager asyncConditionContainer = null
	private AsyncState state = CREATED
	protected def elapsedWait = 0
	
	protected final def timeout
	
	protected final def itemPath
	
	/*
	 * concerns the 'significant' value for the condition, note that the value might be the 
	 * expected value, or the anti-expected value (i.e. the condition is considered to have 
	 * passed so long as the actual item value does not match this). The exact usage of the
	 * item value depends on the concrete type extending this abstract base class 
	 */
	protected final def itemValue
	
	AssertAsyncRunResult(timeout, itemPath, itemValue)
	{
		this.timeout = timeout
		this.itemPath = itemPath
		this.itemValue = itemValue
	}
	
	@Override
	def getItemPath()
	{
		return itemPath;
	}
	
	@Override
	def setContainingMap(Map asyncConditionContainer)
	{
		this.asyncConditionContainer = asyncConditionContainer
	}
	
	def getState()
	{
		return state
	}
	
	protected boolean isItemPathMatch(final candidatePath)
	{
		return isStringMatch(itemPath, candidatePath)
	}
			
	protected boolean isItemValueMatch(final candidateValue)
	{
		return isStringMatch(itemValue, candidateValue)
	}

	private boolean isStringMatch(targetString, candidateString)
	{
		if(targetString == null && candidateString == null)
		{
			return true
		}
		else if(targetString == null && candidateString != null)
		{
			return false
		}
		else if(targetString != null && candidateString == null)
		{
			return false
		}
		
		return targetString.toString().equals(candidateString.toString())
	}
	
	protected synchronized setState(newState)
	{
		if(newState != state)
		{
			state = newState
			
			if(asyncConditionContainer != null && state != WAITING)
			{
				asyncConditionContainer.removeAsyncCondition(this)
			} 
			
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
		logTrace("AssertAsyncEqualsRunResult.onTick - state [${state}] for async assertion: ${this.toString()}")
	}
	
	protected def registerWithManager(def manager)
	{
		state = WAITING
		manager.registerAsyncCondition(this)
	}
}
