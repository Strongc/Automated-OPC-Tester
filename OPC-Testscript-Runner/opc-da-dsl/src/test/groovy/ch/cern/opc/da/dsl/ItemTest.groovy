package ch.cern.opc.da.dsl

import static ch.cern.opc.common.Quality.State.*;
import ch.cern.opc.client.OPCDAClientApi
import ch.cern.opc.client.OPCDAClientInstance
import ch.cern.opc.da.dsl.Item
import ch.cern.opc.da.dsl.ScriptContext
import static ch.cern.opc.da.dsl.TestingUtilities.setSingletonStubInstance
import ch.cern.opc.common.ItemValue;
import ch.cern.opc.common.Log

import static org.junit.Assert.*
import org.junit.Test
import org.junit.Before
import groovy.mock.interceptor.*

class ItemTest
{
	static final def TESTEE_GROUP_NAME = 'testee.group'
	static final def TESTEE_ITEM_PATH = 'testee.item.path'
	static final def TESTEE_ITEM_VALUE = new ItemValue('42', 192, 'some timestamp', 8)
	
	ItemValue returnedValue = TESTEE_ITEM_VALUE

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
	
	class AssertQualityValues
	{
		final def message
		final def expected
		final def actual

		def AssertQualityValues(message, expected, actual)
		{
			this.message = message
			this.expected = expected
			this.actual = actual
		}
	}

	def requestedSetSyncValueParameters
	def requestedSetAsyncValueParameters
	def requestedAssertAsyncParameters
	def requestedAssertTrueFalseValues
	def requestedAssertQualityValues

	@Before
	void setup()
	{
		requestedReadItemSyncGroupName = null
		requestedReadItemSyncPath = null
		requestedAssertAsyncParameters = null

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

		assertEquals(TESTEE_GROUP_NAME, requestedReadItemSyncGroupName)
		assertEquals(TESTEE_ITEM_PATH, requestedReadItemSyncPath)
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
		assertEquals(TESTEE_ITEM_VALUE.value, requestedAssertTrueFalseValues.value)
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
		assertEquals(TESTEE_ITEM_VALUE.value, requestedAssertTrueFalseValues.value)
	}

	@Test
	void testGetQuality()
	{
		returnedValue = new ItemValue('', 192/*QUALITY GOOD*/, '', 0)
		assertTrue(testee.quality.equals(GOOD))

		returnedValue = new ItemValue('', 0/*QUALITY BAD*/, '', 0)
		assertTrue(testee.quality.equals(BAD))

		returnedValue = new ItemValue('', 64/*QUALITY UNCERTAIN*/, '', 0)
		assertTrue(testee.quality.equals(UNCERTAIN))

		returnedValue = new ItemValue('', 128/*QUALITY NOT APPLICABLE*/, '', 0)
		assertTrue(testee.quality.equals(NA))
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
		ScriptContext.metaClass.assertAsyncEquals = {message, timeout, value, path->
			requestedAssertAsyncParameters = new AssertAsyncValues(message, timeout, value, path)
		}

		ScriptContext.metaClass.assertAsyncNotEquals{message, timeout, value, path->
			requestedAssertAsyncParameters = new AssertAsyncValues(message, timeout, value, path)
		}

		ScriptContext.metaClass.assertTrue{message, value ->
			requestedAssertTrueFalseValues = new AssertTrueFalseValues(message, value)
		}

		ScriptContext.metaClass.assertFalse{message, value ->
			requestedAssertTrueFalseValues = new AssertTrueFalseValues(message, value)
		}
		
		ScriptContext.metaClass.assertQuality{message, expected, actual->
			requestedAssertQualityValues = new AssertQualityValues(message, expected, actual)
		}
		
		// not really necessary but in case of init problems the test will fail here. Fail early
		ScriptContext.instance
	}

}
