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
	private final static def NODE_ID = 'ns=1;s=testnode'
	private final static def NODE_VALUES = createDataValues('testnode_value')

	private final static def SRC_TIME = new DateTime()
	private final static def SVR_TIME = new DateTime()

	private def rcvdWriteNodeValueParameters = [:]
	private def rcvdAsyncAssertParameters = [:]

	private def testee

	@Before
	void setup()
	{
		rcvdWriteNodeValueParameters = [:]
		rcvdAsyncAssertParameters = [:]

		def theClientInstance = [
			readNodeValue:
			{id->
				return NODE_VALUES
			},
			writeNodeValueSync:
			{id, values->
				rcvdWriteNodeValueParameters['syncWrite'] = true
				rcvdWriteNodeValueParameters['id'] = id
				rcvdWriteNodeValueParameters['values'] = values
				return true
			},
			writeNodeValueAsync:
			{id, values->
				rcvdWriteNodeValueParameters['syncWrite'] = false
				rcvdWriteNodeValueParameters['id'] = id
				rcvdWriteNodeValueParameters['values'] = values
				return true
			},
			registerAsyncUpdate:
			{callback->
				println 'registerAsyncUpdate called'
			}
			] as UaClientInterface
		
		setSingletonStubInstance(UaClient.class, theClientInstance)
		
		ScriptContext.instance.metaClass.assertAsyncEquals = 
		{message, timeout, expectedValue, id->
			rcvdAsyncAssertParameters['message']=message
			rcvdAsyncAssertParameters['timeout']=timeout
			rcvdAsyncAssertParameters['expectedValue']=expectedValue
			rcvdAsyncAssertParameters['id']=id
		}
		
		ScriptContext.instance.metaClass.assertAsyncNotEquals =
		{message, timeout, expectedValue, id->
			rcvdAsyncAssertParameters['message']=message
			rcvdAsyncAssertParameters['timeout']=timeout
			rcvdAsyncAssertParameters['expectedValue']=expectedValue
			rcvdAsyncAssertParameters['id']=id
		}

		
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
	
	@Test(expected = IllegalArgumentException)
	void testCtorRejectsInvalidNodeId()
	{
		testee = new Node('non OPC-UA spec conformant ID')
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
		assertEquals(0, rcvdWriteNodeValueParameters.size())
		
		testee.syncValue = ['123', '456'] as String[]
		
		assertTrue(rcvdWriteNodeValueParameters['syncWrite'])
		assertEquals(NodeId.decode(NODE_ID), rcvdWriteNodeValueParameters['id'])
		assertTrue(isEquals(['123', '456'] as String[], rcvdWriteNodeValueParameters['values']))
	}
	
	@Test
	void testWriteAsyncValueArray()
	{
		assertEquals(0, rcvdWriteNodeValueParameters.size())
		
		testee.asyncValue = ['123', '456'] as String[]
		
		assertFalse(rcvdWriteNodeValueParameters['syncWrite'])
		assertEquals(NodeId.decode(NODE_ID), rcvdWriteNodeValueParameters['id'])
		assertTrue(isEquals(['123', '456'] as String[], rcvdWriteNodeValueParameters['values']))
	}

	@Test
	void testAssertAsyncEquals()
	{
		testee.assertAsyncEquals('test message', 999, 'some value')

		assertEquals('test message', rcvdAsyncAssertParameters['message'])
		assertEquals(999, rcvdAsyncAssertParameters['timeout'])
		assertEquals('some value', rcvdAsyncAssertParameters['expectedValue'])
		assertEquals('id must be registered as a string, the async handler requires it', 
			testee.id, rcvdAsyncAssertParameters['id'])
	}
	
	@Test
	void testAssertAsyncNotEquals()
	{
		testee.assertAsyncNotEquals('test message', 999, 'some value')
		
		assertEquals('test message', rcvdAsyncAssertParameters['message'])
		assertEquals(999, rcvdAsyncAssertParameters['timeout'])
		assertEquals('some value', rcvdAsyncAssertParameters['expectedValue'])
		assertEquals('id must be registered as a string, the async handler requires it',
			testee.id, rcvdAsyncAssertParameters['id'])
	}
	
	private static DataValue[] createDataValues(value)
	{
		def valueVariant = new Variant(value)
		def result = new DataValue(valueVariant, StatusCode.GOOD, SRC_TIME, SVR_TIME)
		
		return [result] as DataValue[]
	}
}