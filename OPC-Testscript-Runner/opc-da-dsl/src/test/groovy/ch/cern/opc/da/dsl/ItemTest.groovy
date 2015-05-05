package ch.cern.opc.da.dsl

import static ch.cern.opc.common.Quality.State.*
import static ch.cern.opc.common.Datatype.*
import static ch.cern.opc.common.ItemAccessRight.*
import ch.cern.opc.client.OPCDAClientApi
import ch.cern.opc.client.OPCDAClientInstance
import ch.cern.opc.da.dsl.Item
import ch.cern.opc.da.dsl.ScriptContext
import static ch.cern.opc.da.dsl.TestingUtilities.setSingletonStubInstance
import ch.cern.opc.common.ItemValue
import ch.cern.opc.common.Log
import ch.cern.opc.common.Timestamp

import static org.junit.Assert.*
import org.junit.Test
import org.junit.Before
import groovy.mock.interceptor.*

class ItemTest
{
	static final def TESTEE_GROUP_NAME = 'testee.group'
	static final def TESTEE_ITEM_PATH = 'testee.item.path'
	static final def TESTEE_ITEM_VALUE = new ItemValue('42', 192, '2015/05/06-01:02:3.456', 8)

	ItemValue returnedValue = TESTEE_ITEM_VALUE

	static final def MESSAGE = 'user assertion message'
	static final def ASYNC_TIMEOUT = 1

	def testee

	def groupNamePassedToClientDll
	def itemPathPassedToClientDll
	def itemAccessRightsReturnedFromClientDll
	def itemDatatypeReturnedFromClientDll

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

	class ItemAssertionValues
	{
		final def message
		final def expected
		final def actual

		def ItemAssertionValues(message, expected, actual)
		{
			this.message = message
			this.expected = expected
			this.actual = actual
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



	def requestedSetSyncValueParameters
	def requestedSetAsyncValueParameters
	def requestedAssertAsyncParameters
	def requestedAssertTrueFalseValues
	def requestedAssertQualityValues
	def requestedAssertDatatypeValues
	def requestedAssertAccessRightsValues

	@Before
	void setup()
	{
		groupNamePassedToClientDll = null
		itemPathPassedToClientDll = null
		requestedAssertAsyncParameters = null
		itemAccessRightsReturnedFromClientDll = 0
		itemDatatypeReturnedFromClientDll = 0

		Log.logLevel('trace')
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

		assertEquals(TESTEE_GROUP_NAME, groupNamePassedToClientDll)
		assertEquals(TESTEE_ITEM_PATH, itemPathPassedToClientDll)
	}

	@Test
	void testSyncValueReturnsValue()
	{
		assertEquals(TESTEE_ITEM_VALUE.value, testee.syncValue)
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
		assertEquals(TESTEE_ITEM_VALUE.value, requestedAssertTrueFalseValues.actual)
	}

	@Test
	void testAssertQuality_passesMessageAndExpectedAndQuality()
	{
		testee.assertQuality(MESSAGE, GOOD)

		assertEquals(MESSAGE, requestedAssertQualityValues.message)
		assertEquals(GOOD, requestedAssertQualityValues.expected)
		assertEquals(TESTEE_ITEM_VALUE.quality, requestedAssertQualityValues.actual)
	}

	@Test
	void testAssertFalse_passesMessageAndValue()
	{
		testee.assertFalse(MESSAGE)

		assertEquals(MESSAGE, requestedAssertTrueFalseValues.message)
		assertEquals(TESTEE_ITEM_VALUE.value, requestedAssertTrueFalseValues.actual)
	}

	@Test
	void testAssertDatatype_passesMessageAndValue()
	{
		itemDatatypeReturnedFromClientDll = VT_EMPTY
		
		testee.assertDatatype(MESSAGE, VT_BSTR)

		assertEquals(MESSAGE, requestedAssertDatatypeValues.message)
		assertEquals(VT_BSTR, requestedAssertDatatypeValues.expected)
		assertEquals(VT_EMPTY, requestedAssertDatatypeValues.actual)
	}

	@Test
	void testAssertAccessRights()
	{
		itemAccessRightsReturnedFromClientDll = UNKNOWN_ACCESS

		testee.assertAccessRights(MESSAGE, READ_WRITE_ACCESS)

		assertEquals(MESSAGE, requestedAssertAccessRightsValues.message)
		assertEquals(UNKNOWN_ACCESS, requestedAssertAccessRightsValues.actual)
		assertEquals(READ_WRITE_ACCESS, requestedAssertAccessRightsValues.expected)
	}

	@Test
	void testGetQuality()
	{
		returnedValue = new ItemValue('', 192/*QUALITY GOOD*/, '', 0)
		assertTrue(testee.quality.equals(GOOD))
		
		assertEquals(testee.groupName, groupNamePassedToClientDll)
		assertEquals(testee.path, itemPathPassedToClientDll)

		returnedValue = new ItemValue('', 0/*QUALITY BAD*/, '', 0)
		assertTrue(testee.quality.equals(BAD))

		returnedValue = new ItemValue('', 64/*QUALITY UNCERTAIN*/, '', 0)
		assertTrue(testee.quality.equals(UNCERTAIN))

		returnedValue = new ItemValue('', 128/*QUALITY NOT APPLICABLE*/, '', 0)
		assertTrue(testee.quality.equals(NA))
	}

	@Test
	void testGetDatatype()
	{
		itemDatatypeReturnedFromClientDll = VT_EMPTY
		assertTrue(testee.datatype.equals(VT_EMPTY))
		
		assertEquals(testee.groupName, groupNamePassedToClientDll)
		assertEquals(testee.path, itemPathPassedToClientDll)

		itemDatatypeReturnedFromClientDll = VT_I2
		assertTrue(testee.datatype.equals(VT_I2))

		itemDatatypeReturnedFromClientDll = VT_BSTR
		assertTrue(testee.datatype.equals(VT_BSTR))

		itemDatatypeReturnedFromClientDll = VT_BOOL
		assertTrue(testee.datatype.equals(VT_BOOL))

		itemDatatypeReturnedFromClientDll = VT_UNRECOGNISED
		assertTrue(testee.datatype.equals(VT_UNRECOGNISED))
	}
	
	@Test
	void testGetTimestamp()
	{
		returnedValue = new ItemValue('', 192/*QUALITY GOOD*/, 'some invalid timestamp', 0)
		assertTrue(testee.timestamp.equals(new Timestamp('some invalid timestamp')))
		
		returnedValue = new ItemValue('', 192/*QUALITY GOOD*/, '2015/05/06-01:02:3.456', 0)
		assertTrue(testee.timestamp.equals(new Timestamp('2015/05/06-01:02:3.456')))
	}

	@Test
	void testAssertAsyncQuality_callsScriptContextWithCorrectParams()
	{
		testee.assertAsyncQuality(MESSAGE, ASYNC_TIMEOUT, GOOD)
		assertEquals(MESSAGE, requestedAssertAsyncParameters.message)
		assertEquals(ASYNC_TIMEOUT, requestedAssertAsyncParameters.timeout)
		assertEquals(GOOD, requestedAssertAsyncParameters.value)
		assertEquals(TESTEE_ITEM_PATH, requestedAssertAsyncParameters.path)
	}

	@Test
	void testGetItemAccessRightsReturnsValueFromDll()
	{
		itemAccessRightsReturnedFromClientDll = UNKNOWN_ACCESS
		assertEquals(UNKNOWN_ACCESS, testee.accessRights);

		itemAccessRightsReturnedFromClientDll = READ_ACCESS
		assertEquals(READ_ACCESS, testee.accessRights);

		itemAccessRightsReturnedFromClientDll = WRITE_ACCESS
		assertEquals(WRITE_ACCESS, testee.accessRights);

		itemAccessRightsReturnedFromClientDll = READ_WRITE_ACCESS
		assertEquals(READ_WRITE_ACCESS, testee.accessRights);
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
						groupNamePassedToClientDll = groupName
						itemPathPassedToClientDll = path
						return returnedValue
					},
					writeItemSync: {groupName, path, value ->
						requestedSetSyncValueParameters = new WriteItemValues(groupName, path, value)
						return true
					},
					writeItemAsync: {groupName, path, value ->
						requestedSetAsyncValueParameters = new WriteItemValues(groupName, path, value)
						return true
					},
					registerAsyncUpdate: {callback ->
						Log.logTrace("stub client: loading a callback update")
					},
					getItemDatatype: {groupName, path->
						groupNamePassedToClientDll = groupName
						itemPathPassedToClientDll = path
						return itemDatatypeReturnedFromClientDll
					},
					getItemAccessRights: {groupName, path->
						groupNamePassedToClientDll = groupName
						itemPathPassedToClientDll = path
						return itemAccessRightsReturnedFromClientDll
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
		ScriptContext.metaClass.assertAsyncEquals = {message, timeout, expectedValue, path->
			requestedAssertAsyncParameters = new AssertAsyncValues(message, timeout, expectedValue, path)
		}

		ScriptContext.metaClass.assertAsyncNotEquals{message, timeout, antiExpectedValue, path->
			requestedAssertAsyncParameters = new AssertAsyncValues(message, timeout, antiExpectedValue, path)
		}

		ScriptContext.metaClass.assertTrue{message, actualValue ->
			requestedAssertTrueFalseValues = new ItemAssertionValues(message, true, actualValue)
		}

		ScriptContext.metaClass.assertFalse{message, actualValue ->
			requestedAssertTrueFalseValues = new ItemAssertionValues(message, false, actualValue)
		}

		ScriptContext.metaClass.assertQuality{message, expectedQuality, actualQuality->
			requestedAssertQualityValues = new ItemAssertionValues(message, expectedQuality, actualQuality)
		}

		ScriptContext.metaClass.assertAsyncQuality{message, timeout, expectedQuality, path->
			requestedAssertAsyncParameters = new AssertAsyncValues(message, timeout, expectedQuality, path)
		}

		ScriptContext.metaClass.assertDatatype{message, expectedDatatype, actualDatatype ->
			requestedAssertDatatypeValues = new ItemAssertionValues(message, expectedDatatype, actualDatatype)
		}

		ScriptContext.metaClass.assertAccessRights{message, expectedAccessRights, actualAccessRights ->
			requestedAssertAccessRightsValues = new ItemAssertionValues(message, expectedAccessRights, actualAccessRights)
		}

		// not really necessary but in case of init problems the test will fail here. Fail early
		ScriptContext.instance
	}

}
