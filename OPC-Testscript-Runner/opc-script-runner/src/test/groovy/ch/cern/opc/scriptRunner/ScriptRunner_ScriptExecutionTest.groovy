package ch.cern.opc.scriptRunner

import ch.cern.opc.client.OPCDAClientApi
import ch.cern.opc.client.OPCDAClientInstance
import ch.cern.opc.da.dsl.ScriptContext
import ch.cern.opc.dsl.common.results.ObservableRunResult

import static org.junit.Assert.*
import org.junit.Test
import org.junit.Before
import org.junit.After

class ScriptRunner_ScriptExecutionTest 
{
	def testee
	def file
	
	@Before
	void setup()
	{
		def theClientInstance = [
			createGroup: {groupName, refreshRate ->
				println "createGroup: name [${groupName}] refresh rate[${refreshRate}]"
				return true
			},
			addItem: {groupName, path ->
				println "addItem: group [${groupName}] item [${path}]"
				return true
			},
			readItemSync: {groupName, path ->
				println "readItemSync: group [${groupName}] item [${path}]"
				return 'someValue'
			},
			destroy: {},
			registerAsyncUpdate: {},
			end:{}
		] as OPCDAClientApi
	
		OPCDAClientInstance.metaClass.'static'.getInstance = {-> return theClientInstance}
		
		file = new File('temp_script_file.txt')
		
		testee = new ScriptRunner()
	}
	
	@After
	void teardown()
	{
		file.delete()
	}

	@Test
	void testRunScriptCreateGroup()
	{
		file << 'createGroup(\'group.1\')'
		
		testee.runScript(file)
		
		assertEquals(1, testee.context.groups.size())
	}
	
	@Test
	void testRunScriptCreateGroupAndAddItems()
	{
		
		file << """\
			group('group.1').with{
				
				item('item.1')
				item('item.2')
				item('item.3')
				
				println item('item.1').syncValue
				println item('item.2').syncValue
				println item('item.2').syncValue
			}
		"""

		testee.runScript(file)
		
		assertEquals(3, testee.context.group('group.1').items.size())
	}
	
	@Test
	void testRunScriptFromStringCreatesGroup()
	{
		file << """\
			group('group.1').with
			{
				g->g.item('item.1');
				g.item('item.2');
				println g.item('item.3').syncValue
			}
		"""
		
		testee.runScript(file)
		
		assertEquals(1, testee.context.groups.size())
		assertNotNull(testee.context.group('group.1'))
	}
	
	@Test
	void testScriptCanLogmessages()
	{
		file << """\
			logError('error text')
			logWarning('warn text')
			logInfo('info text')
			logDebug('debug text')
		"""
		
		try
		{
			testee.runScript(file)
		}
		catch(MissingMethodException e)
		{
			fail('log* methods missing')
		}
	}
	
	@Test
	void testAddingAssertionUpdatesResultsObserver()
	{
		def updateInfo = null
		
		def observer = {Object[] args ->
			updateInfo = args[1]
		} as Observer
		
		file << """\
			assertTrue('I am a success', true)
		"""

		testee.runScript(file, observer)
		
		assertTrue(updateInfo instanceof ObservableRunResult)
	}
}
