import ch.cern.opc.scriptRunner.ScriptRunner
import ch.cern.opc.client.OPCDAClientInstance
import static ch.cern.opc.common.Log.*

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
		logInfo('fetching script file...')
		logInfo("currently hardcoded to [${scriptPath}]")
		
		def file = new File(scriptPath)
		logInfo("opening script [${file}]\nContents...")
		file.eachLine{ln -> logInfo("\t"+ln)}
		
		new ScriptRunner().runScript(file)
	}
}

