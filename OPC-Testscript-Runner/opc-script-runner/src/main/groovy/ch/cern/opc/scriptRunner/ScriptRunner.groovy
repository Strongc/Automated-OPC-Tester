package ch.cern.opc.scriptRunner

import static ch.cern.opc.common.Log.*

class ScriptRunner 
{
	def runScript(scriptFile)
	{
		def scriptClosure = Eval.me("{->\n${scriptFile.text}\n}")
		runScriptClosure(scriptClosure, new ScriptContext())
	}
	
	private def runScriptClosure(script, scriptDelegate = null)
	{
		logInfo('about to run the script')
		script.delegate = (scriptDelegate != null? scriptDelegate: this)
		script()
		logInfo('finished running the script')
	}
}
