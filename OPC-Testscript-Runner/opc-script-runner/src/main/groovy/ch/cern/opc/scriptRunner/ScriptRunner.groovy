package ch.cern.opc.scriptRunner

import static ch.cern.opc.common.Log.*
import org.w3c.dom.Element;

class ScriptRunner 
{
	private def context = null
	
	def Element runScript(scriptFile, resultsObserver = null, isOPCUA = false)
	{
		def scriptClosure = Eval.me("{->\ntry{${scriptFile.text}}catch(e){addException(e);logError('exception thrown')}\n}")
		
		context = getDSL(isOPCUA)
		if(resultsObserver != null)
		{
			context.addObserver(resultsObserver)
		}

		runScriptClosure(scriptClosure, context)
		
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
	
	private def getDSL(isOPCUA)
	{
		if(isOPCUA)
		{
			return new ch.cern.opc.ua.dsl.ScriptContext()
		}
		else
		{
			return new ch.cern.opc.da.dsl.ScriptContext()
		}
	}
}
