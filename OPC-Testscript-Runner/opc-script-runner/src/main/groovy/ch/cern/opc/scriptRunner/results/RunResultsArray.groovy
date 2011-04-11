package ch.cern.opc.scriptRunner.results

import java.util.Observable;

protected class RunResultsArray extends Observable 
{
	protected def results = []
	
	protected def add(runResult)
	{
		results.add(runResult)
		
		setChanged()
		notifyObservers(runResult)
	}
}
