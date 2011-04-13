package ch.cern.opc.scriptRunner

import static ch.cern.opc.common.Log.*
import org.w3c.dom.Element;
import ch.cern.opc.client.ClientInstance

class ScriptRunner 
{
	private def context = null
	
	def Element runScript(scriptFile, resultsObserver = null)
	{
		def scriptClosure = Eval.me("{->\ntry{${scriptFile.text}}catch(e){addException(e);logError('exception thrown')}\n}")
		
		context = new ScriptContext()
		if(resultsObserver != null)
		{
			context.addObserver(resultsObserver)
		}

		runScriptClosure(scriptClosure, context)
		ClientInstance.instance.end();
		
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
