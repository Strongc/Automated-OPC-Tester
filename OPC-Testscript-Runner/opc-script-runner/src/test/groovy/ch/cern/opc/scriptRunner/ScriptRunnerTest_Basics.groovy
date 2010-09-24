package ch.cern.opc.scriptRunner;
import groovy.lang.Closure;

import ch.cern.opc.client.ClientApi
import static ch.cern.opc.client.ClientApi.State.*
import static groovy.lang.Closure.*

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;

class ScriptRunnerTest_Basics 
{
	def testee
	def clientState
	
	@Before
	void setup()
	{
		testee = new ScriptRunner()
	}
	
	@Test
	void testRunScriptEvalsTheScript()
	{
		def wasScriptRun = false
		
		testee.runScript({wasScriptRun = true})
		assertTrue(wasScriptRun)
	}
	
	@Test
	void testRunScriptWithDelegateFindsDelegateProperties()
	{
		def scriptDelegate = [myProperty:false]
		def script = {myProperty = true}
		
		assertFalse(scriptDelegate.myProperty)
		testee.runScript(script, scriptDelegate)
		assertTrue(scriptDelegate.myProperty)
	}
	
	@Test
	void testRunScriptWithCreateGroupCreatesGroupOnDelegate()
	{
		def groups = [:]
		MockScriptRunnerDelegate.metaClass.createGroup = {name ->
			println "creating a group [name: ${name}], groups [${groups}]"
			groups["woo"] = new Object()
		}
		
		def s = new MockScriptRunnerDelegate()
		s.createGroup('testGroup')
		
		def script = {createGroup('scriptGroup')}
		testee.runScript(script, s)
		
		assertEquals(1, groups.size())
	}	
}
