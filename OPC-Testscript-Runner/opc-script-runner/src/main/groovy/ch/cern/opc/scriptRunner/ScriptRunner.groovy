package ch.cern.opc.scriptRunner

import static ch.cern.opc.common.Log.*
import org.w3c.dom.Element;
import ch.cern.opc.client.ClientInstance

class ScriptRunner 
{
	static def context = null
	
	def Element runScript(scriptFile)
	{
		def scriptClosure = Eval.me("{->\ntry{${scriptFile.text}}catch(e){addException(e);logError('exception thrown')}\n}")
		context = new ScriptContext()

		runScriptClosure(scriptClosure, context)
		println "ClientInstance.instance [${(ClientInstance.instance == null)?'NULL':'NON NULL'}]"
		ClientInstance.instance.destroy();
		
		return context.XML
	}
	
	private def runScriptClosure(script, scriptDelegate = null)
	{
		logInfo('about to run the script')
		script.delegate = (scriptDelegate != null? scriptDelegate: this)
		
		script.delegate.onScriptStart()
		script()
		script.delegate.onScriptEnd()
				
		logInfo('finished running the script')
	}
}
