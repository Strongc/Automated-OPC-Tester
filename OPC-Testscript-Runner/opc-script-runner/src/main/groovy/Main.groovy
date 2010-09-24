import ch.cern.opc.scriptRunner.ScriptContext;
import ch.cern.opc.scriptRunner.ScriptRunner
import ch.cern.opc.client.ClientInstance

class Main 
{
	static main(args) 
	{
		println 'setting up runner...'
		
		def script = {
			init('', 'Matrikon.OPC.Simulation')
			
			group('group.1').with
			{
				println item('testGroup.myBigFloat').syncValue
				println item('testGroup.myBool').syncValue
				println item('testGroup.myString').syncValue
			}
		}
		def scriptDelegate = new ScriptContext()
		
		new ScriptRunner().runScript(script, scriptDelegate)
	}
}
