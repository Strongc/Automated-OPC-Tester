package ch.cern.opc.scriptRunner

import static ch.cern.opc.common.Log.*
import org.w3c.dom.Element;

class ScriptRunner 
{
	def Element runScript(scriptFile)
	{
		def scriptClosure = Eval.me("{->\n${scriptFile.text}\n}")
		def context = new ScriptContext()

		runScriptClosure(scriptClosure, context)
		
		return context.XML
	}
	
	private def runScriptClosure(script, scriptDelegate = null)
	{
		logInfo('about to run the script')
		script.delegate = (scriptDelegate != null? scriptDelegate: this)
		script()
		logInfo('finished running the script')
	}
}
