package ch.cern.opc.da.dsl

class ScriptContextInstanceStubber 
{
	def static getInstance()
	{
		println 'ScriptContextInstanceStubber.getInstance called'
		def stubbedScriptContextInstance = [
			assertAsyncEquals: {message, timeout, value, path->
				println 'called stubbed instance.assertAsyncEquals'
			},
			assertAsyncNotEquals: {message, timeout, value, path->
				println 'called stubbed instance.assertAsyncNotEquals'
			}
		] as ScriptContext
	
		return stubbedScriptContextInstance
	}
}
