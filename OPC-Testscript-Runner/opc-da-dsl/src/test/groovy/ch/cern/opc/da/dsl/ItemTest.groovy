package ch.cern.opc.da.dsl

import ch.cern.opc.client.OPCDAClientApi
import ch.cern.opc.client.OPCDAClientInstance
import ch.cern.opc.da.dsl.Item
import ch.cern.opc.da.dsl.ScriptContext
import static ch.cern.opc.da.dsl.TestingUtilities.setSingletonStubInstance

import static org.junit.Assert.*
import org.junit.Test
import org.junit.Before
import groovy.mock.interceptor.*

class ItemTest 
{
	static final def TESTEE_GROUP_NAME = 'testee.group'
	static final def TESTEE_ITEM_PATH = 'testee.item.path'
	static final def TESTEE_ITEM_VALUE = '42'
	
	static final def MESSAGE = 'user assertion message'
	static final def ASYNC_TIMEOUT = 1
	
	def testee
	
	def requestedReadItemSyncGroupName
	def requestedReadItemSyncPath
	
	class WriteItemValues
	{
		final def group
		final def item
		final def theValue
		
		def WriteItemValues(group, item, theValue)
		{
			this.group = group
			this.item = item
			this.theValue = theValue
		}
	}
	
	class AssertAsyncValues
	{
		final def message
		final def timeout
		final def value
		final def path
		
		def AssertAsyncValues(message, timeout, value, path)
		{
			this.message = message
			this.timeout = timeout
			this.value = value
			this.path = path
		}
	}
	
	class AssertTrueFalseValues
	{
		final def message
		final def value
		
		def AssertTrueFalseValues(message, value)
		{
			this.message = message
			this.value = value
		}
	}
	
	def requestedSetSyncValueParameters
	def requestedSetAsyncValueParameters
	def requestedAssertAsyncParameters
	def requestedAssertTrueFalseValues
	
	@Before
	void setup()
	{
		requestedReadItemSyncGroupName = null
		requestedReadItemSyncPath = null
		requestedAssertAsyncParameters = null
		
		stubClientInstance()
		stubScriptContext()
		
		testee = new Item(TESTEE_GROUP_NAME, TESTEE_ITEM_PATH)
	}
	
	@Test
	void testGetItemPath()
	{
		assertEquals(TESTEE_ITEM_PATH, testee.path)
	} 
	
	@Test
	void testGetItemGroupName()
	{
		assertEquals(TESTEE_GROUP_NAME, testee.groupName)
	}
	
	@Test(expected = IllegalArgumentException)
	void testConstructorThrowsExceptionForNullGroupName()
	{
		new Item(null, TESTEE_ITEM_PATH)
	}
	
	@Test(expected = IllegalArgumentException)
	void testConstructorThrowsExceptionForNullPath()
	{
		new Item(TESTEE_GROUP_NAME, null)
	}
	
	@Test(expected = IllegalArgumentException)
	void testConstructorThrowsExceptionForEmptyGroupName()
	{
		new Item('', TESTEE_ITEM_PATH)
	}
	
	@Test(expected = IllegalArgumentException)
	void testConstructorThrowsExceptionForEmptyPath()
	{
		new Item(TESTEE_GROUP_NAME, '')
	}
	
	@Test
	void testConstructorHandlesGStringPaths()
	{
		new Item(TESTEE_GROUP_NAME, "GString created at ${new Date()}")
	}
	
	@Test
	void testSyncValueUsesCorrectGroupAndPath()
	{
		testee.syncValue
		
		assertEquals(TESTEE_GROUP_NAME, requestedReadItemSyncGroupName)
		assertEquals(TESTEE_ITEM_PATH, requestedReadItemSyncPath)
	}
	
	@Test
	void testSyncValueReturnsValue()
	{
		assertEquals(TESTEE_ITEM_VALUE, testee.syncValue)
	}
	
	@Test
	void testSyncValueWritesValue()
	{
		testee.syncValue = "123"
		assertEquals("123", requestedSetSyncValueParameters.theValue)
		assertEquals(TESTEE_ITEM_PATH, requestedSetSyncValueParameters.item)
		assertEquals(TESTEE_GROUP_NAME, requestedSetSyncValueParameters.group)
	}
	
	@Test
	void testAsyncValueWritesValue()
	{
		testee.asyncValue = '456'
		assertEquals('456', requestedSetAsyncValueParameters.theValue)
		assertEquals(TESTEE_ITEM_PATH, requestedSetAsyncValueParameters.item)
		assertEquals(TESTEE_GROUP_NAME, requestedSetAsyncValueParameters.group)
	}
	
	@Test
	void testAssertAsyncEquals_callsScriptContextWithCorrectParams()
	{
		use(ScriptContextInstanceStubber)
		{
			testee.assertAsyncEquals(MESSAGE, ASYNC_TIMEOUT, 'a')
		}
		
		assertEquals(MESSAGE, requestedAssertAsyncParameters.message)
		assertEquals(ASYNC_TIMEOUT, requestedAssertAsyncParameters.timeout)
		assertEquals('a', requestedAssertAsyncParameters.value)
		assertEquals(TESTEE_ITEM_PATH, requestedAssertAsyncParameters.path)
	}
	
	@Test
	void testAssertAsyncNotEquals_callsScriptContextWithCorrectParams()
	{
		testee.assertAsyncNotEquals(MESSAGE, ASYNC_TIMEOUT, 'a')
		assertEquals(MESSAGE, requestedAssertAsyncParameters.message)
		assertEquals(ASYNC_TIMEOUT, requestedAssertAsyncParameters.timeout)
		assertEquals('a', requestedAssertAsyncParameters.value)
		assertEquals(TESTEE_ITEM_PATH, requestedAssertAsyncParameters.path)
	}
	
	@Test
	void testAssertTrue_passesMessageAndValue()
	{
		testee.assertTrue(MESSAGE)
		
		assertEquals(MESSAGE, requestedAssertTrueFalseValues.message)
		assertEquals(TESTEE_ITEM_VALUE, requestedAssertTrueFalseValues.value)
	}
	
	@Test
	void testAssertFalse_passesMessageAndValue()
	{
		testee.assertFalse(MESSAGE)
		
		assertEquals(MESSAGE, requestedAssertTrueFalseValues.message)
		assertEquals(TESTEE_ITEM_VALUE, requestedAssertTrueFalseValues.value)
	}
	
	/**
	* ClientInstance stubbed with different method than ScriptContext.
	* Entire ClientInstance class has to be stubbed out and replaced
	* with a mock class. Reason is that creating a genuine ClientInstance
	* instance loads the DLL. Not very unit test.
	*/
	private def stubClientInstance()
	{
		def theClientInstance = [
				readItemSync: {groupName, path ->
					requestedReadItemSyncGroupName = groupName
					requestedReadItemSyncPath = path
					return TESTEE_ITEM_VALUE
				},
				writeItemSync: {groupName, path, value ->
					requestedSetSyncValueParameters = new WriteItemValues(groupName, path, value)
					return true
				},
				writeItemAsync: {groupName, path, value ->
					requestedSetAsyncValueParameters = new WriteItemValues(groupName, path, value)
					return true
				}
			] as OPCDAClientApi
		
		setSingletonStubInstance(OPCDAClientInstance, theClientInstance)
	}
	
	/**
	 * ScriptContext stubbed with different method than ClientInstance.
	 * ScriptContext creates a genuine instance of the ScriptContext class
	 * then adds some assertAsync* methods to it. 
	 * 
	 * At runtime assertAsync* methods are mixed in (via '@mixin') from
	 * the RunResults class. We allow this to happen then override them with
	 * test stub implementations.
	 */
	private def stubScriptContext()
	{
		def instance = ScriptContext.instance
		
		instance.metaClass.assertAsyncEquals = {message, timeout, value, path->
			requestedAssertAsyncParameters = new AssertAsyncValues(message, timeout, value, path)
		}
		
		instance.metaClass.assertAsyncNotEquals{message, timeout, value, path->
			requestedAssertAsyncParameters = new AssertAsyncValues(message, timeout, value, path)
		}
		
		instance.metaClass.assertTrue{message, value ->
			requestedAssertTrueFalseValues = new AssertTrueFalseValues(message, value)
		}
		
		instance.metaClass.assertFalse{message, value ->
			requestedAssertTrueFalseValues = new AssertTrueFalseValues(message, value)
		}
	}
	
}
