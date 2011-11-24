package ch.cern.opc.da.dsl.client

import static org.junit.Assert.*;
import ch.cern.opc.client.OPCDAClientApi
import ch.cern.opc.client.OPCDAClientInstance
import ch.cern.opc.da.dsl.client.OPCDAClient;
import ch.cern.opc.dsl.common.client.UpdateHandler
import static ch.cern.opc.da.dsl.TestingUtilities.setSingletonStubInstance
import org.junit.Test
import org.junit.Before

class OPCDAClientTest 
{
	private final static def LAST_ERROR = "licking wall sockets"
	
	private def isCallbackRegistered = false
	private def testee
	
	@Before
	void setup()
	{
		isCallbackRegistered = false
		
		def theClientInstance = [
			getLastError:{it->
				return LAST_ERROR
			},
			registerAsyncUpdate:{callback->
				isCallbackRegistered = true
			}
		] as OPCDAClientApi
	
		setSingletonStubInstance(OPCDAClientInstance, theClientInstance)
		
		testee = new OPCDAClient()
	}
	
	@Test
	void testGetLastError()
	{
		assertEquals(LAST_ERROR, testee.lastError)
	}
	
	@Test
	void testSetUpdateHandlerRegistersCallbackHandler()
	{
		assertFalse(isCallbackRegistered)
		
		def handler = [onUpdate:{itemId, attributeId, value->println "handled update"}] as UpdateHandler
		testee.updateHandler = handler 
	}
}
