package ch.cern.opc.scriptRunner;

import ch.cern.opc.client.ClientApi
import ch.cern.opc.client.ClientInstance
import ch.cern.opc.scriptRunner.results.ExceptionRunResult;

import ch.cern.opc.client.results.*

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;

class ScriptRunner_ScriptThrowsExceptions 
{
	def testee
	def file
	
	@Before
	void setup()
	{
		file = new File('temp_script_file.txt')
		
		def theClientInstance = [
			registerAsyncUpdate: {},
			end:{}
		] as ClientApi
	
		ClientInstance.metaClass.'static'.getInstance = {-> return theClientInstance}
		testee = new ScriptRunner()
	}
	
	@After
	void teardown()
	{
		file.delete()
	}
	
	@Test
	void testRunScriptWithExceptions()
	{
		def script = 'throw new Exception()' 
		file << script
		
		testee.runScript(file)
		assertEquals(1, testee.context.results.size)
	}
}	
