package ch.cern.opc.scriptRunner;

import ch.cern.opc.client.ClientApi
import ch.cern.opc.client.ClientInstance

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;

class ScriptRunner_ScriptExecutionTest 
{
	def testee
	def scriptDelegate
	
	def createdGroupName
	def createdGroupRefreshRate
	def addedItemGroupName
	def addedItemPath
	def readItemSyncGroup
	def readItemSyncPath
	
	@Before
	void setup()
	{
		createdGroupName = null
		createdGroupRefreshRate = null
		addedItemGroupName = null
		addedItemPath= null
		
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
			},
			readItemSync: {groupName, path ->
				println "readItemSync: group [${groupName}] item [${path}]"
				return 'arse'
			}	
		] as ClientApi
	
		ClientInstance.metaClass.'static'.getInstance = {-> return theClientInstance}
		
		testee = new ScriptRunner()
		scriptDelegate = new ScriptContext()
	}

	@Test
	void testRunScriptCreateGroup()
	{
		def script = {
			createGroup('group.1')
		}
		
		testee.runScript(script, scriptDelegate)
		
		assertEquals(1, scriptDelegate.groups.size())
	}
	
	@Test
	void testRunScriptCreateGroupAndAddItems()
	{
		def script = {
			group('group.1').with{
				
				item('item.1')
				item('item.2')
				item('item.3')
				
				println item('item.1').syncValue
				println item('item.2').syncValue
				println item('item.2').syncValue
			}
		}
		
		testee.runScript(script, scriptDelegate)
		
		assertEquals(3, scriptDelegate.group('group.1').items.size())
	}
	
	@Test
	void testRunScriptFromStringCreatesGroup()
	{
		def script = "{->group('group.1')}"
		
		testee.runScript(Eval.me(script), scriptDelegate)
		
		assertEquals(1, scriptDelegate.groups.size())
		assertNotNull(scriptDelegate.group('group.1'))
	}
}