package ch.cern.opc.ua.dsl.client

import ch.cern.opc.ua.clientlib.UaClient
import ch.cern.opc.ua.clientlib.UaClientInterface
import static ch.cern.opc.ua.dsl.client.TestingUtilities.setSingletonStubInstance
import static org.junit.Assert.*
import org.junit.Test;
import org.junit.Before;

class OPCUAClientTest
{
	private static final def LAST_ERROR = 'swimming with dolphins, or are they sharks?'
	private def testee
	
	@Before
	void setup()
	{
		def theClientInstance = [
			getLastError:{return LAST_ERROR}
		] as UaClientInterface

		setSingletonStubInstance(UaClient.class, theClientInstance)
	
		testee = new OPCUAClient();
	}
	
	@Test
    void testGetLastError() 
	{
        assertEquals(LAST_ERROR, testee.lastError);
    }
}
