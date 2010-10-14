import ch.cern.opc.scriptRunner.ScriptContext;
import ch.cern.opc.scriptRunner.ScriptRunner
import ch.cern.opc.client.ClientInstance

class Main 
{
	/**
	 * @param args
	 */
	/**
	 * @param args
	 */
	static main(args) 
	{
		final def scriptPath = 'C:\\TEMP\\the_test_script.opc.test' 
		println 'fetching script file...'
		println "currently hardcoded to [${scriptPath}]"
		
		def file = new File(scriptPath)
		println "opening script [${file}]\nContents..."
		file.eachLine{ln -> println("\t"+ln)}
		
		new ScriptRunner().runScript(file)
	}
}

