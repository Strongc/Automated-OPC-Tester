package ch.cern.opc.scriptRunner.results.async

import ch.cern.opc.scriptRunner.results.RunResult

public interface AsyncRunResult extends RunResult 
{
	def checkUpdate(itemPath, actualValue)
	def timedOut()
}
