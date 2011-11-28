package ch.cern.opc.ua.dsl

import ch.cern.opc.ua.clientlib.UaClient
import ch.cern.opc.ua.clientlib.UaClientInterface
import org.opcfoundation.ua.builtintypes.*
import static ch.cern.opc.ua.dsl.client.TestingUtilities.setSingletonStubInstance
import static org.junit.Assert.*

import static org.apache.commons.lang.ArrayUtils.isEquals;
import org.junit.Test
import org.junit.Before

class NodeTest 
{
	private final static def NODE_ID = 'ns=1:id=testnode'
	private final static def NODE_VALUES = createDataValues('testnode_value')
	
	private final static def SRC_TIME = new DateTime()
	private final static def SVR_TIME = new DateTime()
	
	private def rcvdWriteId
	private def rcvdWriteValues
	private def returnedWriteResponse
	
	
	private def testee
	
	@Before
	void setup()
	{
		rcvdWriteId = null
		rcvdWriteValues = null
		
		def theClientInstance = [
			readNodeValue:{id->
				return NODE_VALUES
			},
			writeNodeValue:{id, values->
				rcvdWriteId = id
				rcvdWriteValues = values
				return returnedWriteResponse},
			] as UaClientInterface
		
		setSingletonStubInstance(UaClient.class, theClientInstance)
		
		testee = new Node(NODE_ID)
	}
	
	@Test(expected = IllegalArgumentException)
	void testCtorRejectsNullNodeId()
	{
		testee = new Node(null)
	}
	
	@Test(expected = IllegalArgumentException)
	void testCtorRejectsEmptyNodeId()
	{
		testee = new Node('')
	}
	
	@Test(expected = IllegalArgumentException)
	void testCtorRejectsBlankNodeId()
	{
		testee = new Node('   ')
	}

	@Test
	void testGetSyncValue()
	{
		def expected = new ValueWrapper(NODE_VALUES[0])
		assertEquals(expected.value, testee.syncValue.value)
	}
	
	@Test
	void testWriteSyncValueArray()
	{
		assertNull(rcvdWriteId)
		assertNull(rcvdWriteValues)
		returnedWriteResponse = true
		
		testee.syncValue = ['123', '456'] as String[]
		
		assertEquals(NODE_ID, rcvdWriteId)
		assertTrue(isEquals(['123', '456'] as String[], rcvdWriteValues))
	}
	
	private static DataValue[] createDataValues(value)
	{
		def valueVariant = new Variant(value)
		def result = new DataValue(valueVariant, StatusCode.GOOD, SRC_TIME, SVR_TIME)
		
		return [result] as DataValue[]
	}
}
