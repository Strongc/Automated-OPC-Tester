package ch.cern.opc.scriptRunner


class ScriptRunner 
{
	def runScript(scriptFile)
	{
		def scriptClosure = Eval.me("{->\n${scriptFile.text}\n}")
		runScriptClosure(scriptClosure, new ScriptContext())
	}
	
	private def runScriptClosure(script, scriptDelegate = null)
	{
		println "about to run the script"
		script.delegate = (scriptDelegate != null? scriptDelegate: this)
		script()
		println "finished running the script"
	}
}
