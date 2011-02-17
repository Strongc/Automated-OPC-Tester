package ch.cern.opc.scriptRunner;

import ch.cern.opc.client.ClientApi
import ch.cern.opc.client.ClientInstance

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;

class ItemTest 
{
	static final def TESTEE_GROUP_NAME = 'testee.group'
	static final def TESTEE_ITEM_PATH = 'testee.item.path'
	static final def TESTEE_ITEM_VALUE = '42'
	
	def testee
	
	def requestedReadItemSyncGroupName
	def requestedReadItemSyncPath
	
	class WriteItemValues
	{
		def group
		def item
		def theValue

		def WriteItemValues(group, item, theValue)
		{
			this.group = group
			this.item = item
			this.theValue = theValue
		}
	}

	def requestedSetSyncValueParameters
	def requestedSetAsyncValueParameters
	
	@Before
	void setup()
	{
		requestedReadItemSyncGroupName = null
		requestedReadItemSyncPath = null
		
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
		] as ClientApi
	
		ClientInstance.metaClass.'static'.getInstance = {-> return theClientInstance}
		
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
}
