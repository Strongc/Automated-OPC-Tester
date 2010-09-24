package ch.cern.opc.scriptRunner;

import ch.cern.opc.client.ClientApi
import ch.cern.opc.client.ClientInstance

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;

class GroupTest 
{
	final static def TESTEE_GROUP_NAME = 'testee.group'
	final static def TEST_ITEM_PATH = 'test.item.path'
	
	def testee
	
	def createdGroupName
	def createdGroupRefreshRate
	
	def addedItemGroupName
	def addedItemPath
	
	@Before
	void setup()
	{
		createdGroupName = null
		createdGroupRefreshRate = null
		
		addedItemGroupName = null
		addedItemPath = null
		
		def theClientInstance = [
			createGroup: {groupName, refreshRate ->
				println "createGroup: name [${groupName}] refresh rate[${refreshRate}]"
				createdGroupName = groupName
				createdGroupRefreshRate = refreshRate
				return true
			},
			addItem: {groupName, path ->
				println "addItem: group [${groupName}] item [${path}]"
				addedItemGroupName = groupName
				addedItemPath = path
				return true
			}
		] as ClientApi
	
		ClientInstance.metaClass.'static'.getInstance = {-> return theClientInstance}
	
		testee = new Group(TESTEE_GROUP_NAME)
	}
	
	@Test
	void testDefaultConstructorCreatesGroupWithEmptyItemsMap()
	{
		assertEquals(0, new Group('no.items.testee').items.size())
	}
	
	@Test
	void testGetGroupName()
	{
		assertEquals(TESTEE_GROUP_NAME, testee.name)
	}
	
	@Test
	void testFindItemReturnsNullForInvalidItem()
	{
		assertNull(testee.findItem('i.do.not.exist'))
	}
	
	@Test
	void testAddItemAndFindItem()
	{
		testee.addItem(TEST_ITEM_PATH)
		
		assertEquals(TEST_ITEM_PATH, testee.findItem(TEST_ITEM_PATH).path)
	}
	
	@Test
	void testAddItemAddsGroupNameToItem()
	{
		testee.addItem(TEST_ITEM_PATH)
		
		assertEquals(testee.name, testee.findItem(TEST_ITEM_PATH).groupName)
	}
	
	@Test(expected = IllegalArgumentException)
	void testConstructorFailsIfNullGroupNameSupplied()
	{
		new Group()
	}
	
	@Test(expected = IllegalArgumentException)
	void testConstructorFailsIfEmptyGroupNameSupplied()
	{
		new Group('')
	}
	
	@Test
	void testConstructorCallsDllClientWithCorrectGroupNameAndRefreshRate()
	{
		new Group('the.group.name')
		assertEquals('the.group.name', createdGroupName)
		assertEquals(1000, createdGroupRefreshRate)
	}
	
	@Test 
	void testAddItemCallsDllClientWithGroupNameAndItemPath()
	{
		testee.addItem(TEST_ITEM_PATH)
		assertEquals(testee.name, addedItemGroupName)
		assertEquals(TEST_ITEM_PATH, addedItemPath)
	}
	
	@Test
	void testGetItemAddsItemIfNotAlreadyInGroup()
	{
		assertNull(testee.findItem(TEST_ITEM_PATH))
		
		testee.item(TEST_ITEM_PATH)
	
		assertEquals(TEST_ITEM_PATH, testee.findItem(TEST_ITEM_PATH).path)
	}
	
	@Test
	void testGetItemDoesNotAddItemIfAlreadyInGroup()
	{
		testee.item(TEST_ITEM_PATH)
		assertEquals(1, testee.items.size())
		
		testee.item(TEST_ITEM_PATH)
		assertEquals(1, testee.items.size())
	}
	
}
