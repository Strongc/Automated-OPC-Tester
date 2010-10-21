package ch.cern.opc.scriptRunner;

import ch.cern.opc.client.ClientApi
import ch.cern.opc.client.ClientInstance

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
		testee = new ScriptRunner()
		file = new File('temp_script_file.txt')
	}
	
	@After
	void teardown()
	{
		file.delete()
	}

	@Test
	void testRunScriptWithExceptions()
	{
		def script = 'throw new Exception(e)' 
		file << script
		
		testee.runScript(file)
		assertEquals(1, testee.context.exceptions.size)
	}
}	
