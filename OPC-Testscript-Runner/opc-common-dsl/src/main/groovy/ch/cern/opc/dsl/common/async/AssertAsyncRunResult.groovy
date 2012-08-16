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
	protected final int itemPathLength
	
	AssertAsyncRunResult(timeout, itemPath)
	{
		this.timeout = timeout
		this.itemPath = itemPath
		itemPathLength = (itemPath != null ? itemPath.size(): 0)
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
		final int candidatePathLength = (candidatePath != null ? candidatePath.size(): 0)
		
		// check lengths
		if(candidatePathLength != itemPathLength) return false
		
		// check last 3 characters (if longer than 3), usually OPC item addresses
		// differ only towards the end.
		if(candidatePathLength > 3)
		{
			for(int i in 3..1)
			{
				if(candidatePath[candidatePathLength-i] != itemPath[itemPathLength-i]) return false
			}
		}
		
		// check full match	
		return itemPath.equals(candidatePath)
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
