package ch.cern.opc

import static org.junit.Assert.*
import org.junit.*


class ScriptRunnerGuiTest 
{
	private def testee
	
	@Before
	void setup()
	{
		testee = new  ScriptRunnerGui()	
	}
	
	@Test
	void isOPCUAScriptHandlesNull()
	{
		assertFalse(testee.isOPCUAScript(null))
	}
	
	@Test
	void isOPCUAScriptReturnsTrueForOPCUAScriptFalseOtherwise()
	{
		assertTrue(testee.isOPCUAScript('C:\\temp\\path_to_a_UA_test.opcua.test'))
		assertFalse(testee.isOPCUAScript('C:\\temp\\path_to_a_DA_test.opc.test'))
		assertFalse(testee.isOPCUAScript('C:\\temp\\path_to_a_DA_test.opcda.test'))
	}

}
