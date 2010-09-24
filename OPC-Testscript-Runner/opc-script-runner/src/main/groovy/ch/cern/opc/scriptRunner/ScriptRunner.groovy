package ch.cern.opc.scriptRunner


class ScriptRunner 
{
	
	def runScript(script, scriptDelegate = null)
	{
		println "about to run the script"
		script.delegate = (scriptDelegate != null? scriptDelegate: this)
		script()
		println "finished running the script"
	}
}
