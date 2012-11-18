package ch.cern.opc.dsl.common.async

import ch.cern.opc.common.ItemValue
import ch.cern.opc.dsl.common.results.RunResult

public interface AsyncRunResult extends RunResult 
{
	def getItemPath()
	def checkUpdate(itemPath, ItemValue actualValue)
	def timedOut()
	def setContainingMap(Map asyncConditionContainer)
}
