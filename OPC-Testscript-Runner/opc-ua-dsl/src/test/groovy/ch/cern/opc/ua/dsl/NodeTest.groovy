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
	private def currentNodeValue // individual tests to set this as required

	private final static def SRC_TIME = new DateTime()
	private final static def SVR_TIME = new DateTime()

	private def rcvdWriteNodeValueParameters = [:]
	private def rcvdAsyncAssertParameters = [:]
	private def rcvdSyncAssertion

	private def testee

	@Before
	void setup()
	{
		rcvdWriteNodeValueParameters = [:]
		rcvdAsyncAssertParameters = [:]
		rcvdSyncAssertion = null

		def theClientInstance = [
			readNodeValue:
			{id->
				return currentNodeValue
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
			},
			getLastError:
			{
				return 'last client error'
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
		
		ScriptContext.instance.metaClass.add =
		{syncAssertion->
			rcvdSyncAssertion = syncAssertion
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
	void testGetSyncValue_ScalarPrimitives()
	{
		// prime mock with expected value and return as wrapped value.
		def setupExpectedValue = {rawValue ->
			currentNodeValue = createDataValues(rawValue)
			return new ValueWrapper(currentNodeValue[0])	
		}
		
		assertEquals(setupExpectedValue('string.value').value, testee.syncValue.value)
		
		assertEquals(setupExpectedValue(true).value, testee.syncValue.value)
		assertEquals(setupExpectedValue(false).value, testee.syncValue.value)
		
		assertEquals(setupExpectedValue(-123 as Short).value, testee.syncValue.value)
		assertEquals(setupExpectedValue(new UnsignedShort(123)).value, testee.syncValue.value)

		assertEquals(setupExpectedValue(-123 as Integer).value, testee.syncValue.value)
		assertEquals(setupExpectedValue(new UnsignedInteger(123)).value, testee.syncValue.value)
		
		assertEquals(setupExpectedValue(-123 as Long).value, testee.syncValue.value)
		assertEquals(setupExpectedValue(new UnsignedLong(321)).value, testee.syncValue.value)
		
		assertEquals(setupExpectedValue(1.23 as Float).value, testee.syncValue.value)
		assertEquals(setupExpectedValue(1.23 as Double).value, testee.syncValue.value)
		
		assertEquals(setupExpectedValue('a'.bytes[0] as byte).value, testee.syncValue.value)
		assertEquals(setupExpectedValue(new UnsignedByte('b'.bytes[0])).value, testee.syncValue.value)
	}
	
	@Test
	void testAssertEquals_PassingForScalarPrimitives()
	{
		def setupPassingAssertion = {expectedValue ->
			currentNodeValue = createDataValues(expectedValue) // prime the mock interface
			testee.assertEquals('user message', expectedValue) // run the assertion
		}

		setupPassingAssertion('expected.value')
		assertTrue(rcvdSyncAssertion.isPassed)
		
		setupPassingAssertion(true)
		assertTrue(rcvdSyncAssertion.isPassed)
		
		setupPassingAssertion(false)
		assertTrue(rcvdSyncAssertion.isPassed)

		setupPassingAssertion(123)
		assertTrue(rcvdSyncAssertion.isPassed)

		setupPassingAssertion(1.23 as Float)
		assertTrue(rcvdSyncAssertion.isPassed)
		
		setupPassingAssertion(1.23 as Double)
		assertTrue(rcvdSyncAssertion.isPassed)
		
		setupPassingAssertion('a'.bytes[0] as byte)
		assertTrue(rcvdSyncAssertion.isPassed)
	}
	
	@Test
	void testAssertEquals_FailingForScalarPrimitives()
	{
		def setupFailingAssertion = {expectedValue, actualValue ->
			currentNodeValue = createDataValues(actualValue) // prime the mock interface
			testee.assertEquals('user message', expectedValue) // run the assertion
		}
		
		setupFailingAssertion('expected.value', 'actual.value')
		assertFalse(rcvdSyncAssertion.isPassed)
		
		setupFailingAssertion(true, false)
		assertFalse(rcvdSyncAssertion.isPassed)
		
		setupFailingAssertion(123, 321)
		assertFalse(rcvdSyncAssertion.isPassed)
		
		setupFailingAssertion(1.23 as Float, 3.21 as Float)
		assertFalse(rcvdSyncAssertion.isPassed)

		setupFailingAssertion(1.23 as Double, 3.21 as Double)
		assertFalse(rcvdSyncAssertion.isPassed)

		setupFailingAssertion('a'.bytes[0] as byte, 'z'.bytes[0] as byte)
		assertFalse(rcvdSyncAssertion.isPassed)
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