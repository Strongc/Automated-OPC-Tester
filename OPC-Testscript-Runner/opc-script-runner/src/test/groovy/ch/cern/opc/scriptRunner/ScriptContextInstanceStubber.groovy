package ch.cern.opc.scriptRunner

import ch.cern.opc.da.dsl.ScriptContext

class ScriptContextInstanceStubber 
{
	def static getInstance()
	{
		println 'ScriptContextInstanceStubber.getInstance called'
		def stubbedScriptContextInstance = [
			assertAsyncEquals: {message, timeout, value, path->
				println 'called stubbed instance.assertAsyncEquals'
//					requestedAssertAsyncParameters = new AssertAsyncValues(message, timeout, value, path)
			},
			assertAsyncNotEquals: {message, timeout, value, path->
				println 'called stubbed instance.assertAsyncNotEquals'
//					requestedAssertAsyncParameters = new AssertAsyncValues(message, timeout, value, path)
			}
		] as ScriptContext
	
		return stubbedScriptContextInstance
	}
}
