package ch.cern.opc.dsl.common.async

import ch.cern.opc.dsl.common.results.RunResult

public interface AsyncRunResult extends RunResult 
{
	def checkUpdate(itemPath, actualValue)
	def timedOut()
}
