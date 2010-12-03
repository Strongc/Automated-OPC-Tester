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
				return 'someValue'
			},
			registerAsyncUpdate: {}
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
		
		testee.runScriptClosure(script, scriptDelegate)
		
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
		
		testee.runScriptClosure(script, scriptDelegate)
		
		assertEquals(3, scriptDelegate.group('group.1').items.size())
	}
	
	@Test
	void testRunScriptFromStringCreatesGroup()
	{
		def scriptClosure = Eval.me("{->group('group.1').with{g->g.item('item.1');g.item('item.2');println g.item('item.3').syncValue}}")
		
		testee.runScriptClosure(scriptClosure, scriptDelegate)
		
		assertEquals(1, scriptDelegate.groups.size())
		assertNotNull(scriptDelegate.group('group.1'))
	}
	
	@Test
	void testScriptCanLogmessages()
	{
		def script = Eval.me("{->logError('error text');logWarning('warn text');logInfo('info text');logDebug('debug text')}")
		
		try
		{
			testee.runScriptClosure(script, scriptDelegate)
		}
		catch(MissingMethodException e)
		{
			fail('log* methods missing')
		}
	}
}
