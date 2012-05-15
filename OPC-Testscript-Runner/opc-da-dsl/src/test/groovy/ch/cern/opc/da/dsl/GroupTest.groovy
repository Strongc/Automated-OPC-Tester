package ch.cern.opc.da.dsl;

import ch.cern.opc.client.OPCDAClientApi
import ch.cern.opc.client.OPCDAClientInstance
import ch.cern.opc.da.dsl.Group;
import static ch.cern.opc.da.dsl.TestingUtilities.setSingletonStubInstance

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;

class GroupTest 
{
	private final static def TESTEE_GROUP_NAME = 'testee.group'
	
	private final static def TEST_ITEM_PATH_1 = 'test.item.path_1'
	private final static def TEST_ITEM_PATH_2 = 'test.item.path_2'
	private final static def TEST_ITEM_PATH_3 = 'test.item.path_3'
	private final static def TEST_ITEM_PATH_4 = 'test.item.off.piste.path_4'
	private final static def TEST_ITEM_PATH_5 = 'rootitem'
	
	
	def OPC_ADDRESS_SPACE = [TEST_ITEM_PATH_1, TEST_ITEM_PATH_2, TEST_ITEM_PATH_3, TEST_ITEM_PATH_4, TEST_ITEM_PATH_5]
	
	def testee
	
	def createdGroupName
	def createdGroupRefreshRate
	
	def destroyedGroupName
	
	def addedItemGroupName
	def addedItemPath
	
	@Before
	void setup()
	{
		createdGroupName = null
		createdGroupRefreshRate = null
		
		destroyedGroupName = null
		
		addedItemGroupName = null
		addedItemPath = null
		
		def theClientInstance = [
			createGroup: {groupName, refreshRate ->
				println "createGroup: name [${groupName}] refresh rate[${refreshRate}]"
				createdGroupName = groupName
				createdGroupRefreshRate = refreshRate
				return true
			},
		destroyGroup: {groupName ->
				println "destroyGroup: name [${groupName}]"
				destroyedGroupName = groupName
				return true
			},
			addItem: {groupName, path ->
				println "addItem: group [${groupName}] item [${path}]"
				addedItemGroupName = groupName
				addedItemPath = path
				return true
			},
			getItemNames:{
				return OPC_ADDRESS_SPACE
			},
			registerAsyncUpdate:{}
		] as OPCDAClientApi
	
		setSingletonStubInstance(OPCDAClientInstance, theClientInstance)
	
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
		testee.addItem(TEST_ITEM_PATH_1)
		
		assertEquals(TEST_ITEM_PATH_1, testee.findItem(TEST_ITEM_PATH_1).path)
	}
	
	@Test
	void testAddItemAddsGroupNameToItem()
	{
		testee.addItem(TEST_ITEM_PATH_1)
		
		assertEquals(testee.name, testee.findItem(TEST_ITEM_PATH_1).groupName)
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
	void testConstructorHandlesNamesAsGStrings()
	{
		new Group("I am a gstring created at ${new Date()}")
	}

	@Test 
	void testAddItemCallsDllClientWithGroupNameAndItemPath()
	{
		testee.addItem(TEST_ITEM_PATH_1)
		assertEquals(testee.name, addedItemGroupName)
		assertEquals(TEST_ITEM_PATH_1, addedItemPath)
	}
	
	@Test
	void testGetItemAddsItemIfNotAlreadyInGroup()
	{
		assertNull(testee.findItem(TEST_ITEM_PATH_1))
		
		testee.item(TEST_ITEM_PATH_1)
	
		assertEquals(TEST_ITEM_PATH_1, testee.findItem(TEST_ITEM_PATH_1).path)
	}
	
	@Test
	void testGetItemDoesNotAddItemIfAlreadyInGroup()
	{
		testee.item(TEST_ITEM_PATH_1)
		assertEquals(1, testee.items.size())
		
		testee.item(TEST_ITEM_PATH_1)
		assertEquals(1, testee.items.size())
	}
	
	@Test
	void testItemsReturnsCollectionOfAlreadyAddedItems()
	{
		testee.item(TEST_ITEM_PATH_1)
		testee.item(TEST_ITEM_PATH_2)
		testee.item(TEST_ITEM_PATH_3)
		testee.item(TEST_ITEM_PATH_4)
		assertEquals(4, testee.items.size())
		
		assertEquals(0, testee.items('precise_no_match').size())
		assertEquals(1, testee.items(TEST_ITEM_PATH_1).size())
		assertEquals(0, testee.items('*off.piste*').size())
		assertEquals(0, testee.items('**off.piste').size())
		assertEquals(0, testee.items('**off.piste*').size())
		assertEquals(1, testee.items('**off.piste**').size())
		assertEquals(3, testee.items('*.item.path*').size())
		assertEquals(4, testee.items('**.path*').size())
		assertEquals(1, testee.items('*').size())
		assertEquals(5, testee.items('**').size())
		assertEquals(4, testee.items('*.*.*').size())
	}
	
	@Test
	void testItemsAddingAllItemsFromAddressSpaceMatchingPattern()
	{
		assertEquals(0, testee.items.size())
		testee.items('**')
		assertEquals(OPC_ADDRESS_SPACE.size(), testee.items.size())
	}
	
	@Test
	void testItemsAddingAllItemsFromAddressSpaceMatchingStringSubstitutionPattern()
	{
		def substitutionPattern = '**'
		assertEquals(0, testee.items.size())
		testee.items("${substitutionPattern}")
		assertEquals(OPC_ADDRESS_SPACE.size(), testee.items.size())
	}
	
	@Test
	void testDestroyGroupCallsDestroyGroup()
	{
		println("testee [${testee.toString()}] - calling destroy...")
		testee.destroy()
		
		assertEquals(TESTEE_GROUP_NAME, destroyedGroupName)
	}
}
