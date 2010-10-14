package ch.cern.opc.scriptRunner;
import groovy.lang.Closure;

import ch.cern.opc.client.ClientApi
import static ch.cern.opc.client.ClientApi.State.*
import static groovy.lang.Closure.*

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;

class ScriptRunner_BasicTest 
{
	def testee
	def clientState
	
	@Before
	void setup()
	{
		testee = new ScriptRunner()
	}
	
	@Test
	void testRunScriptRunsTheScript()
	{
		def wasScriptRun = false
		
		testee.runScriptClosure({wasScriptRun = true})
		assertTrue(wasScriptRun)
	}
	
	@Test
	void testRunScriptWithDelegateFindsDelegateProperties()
	{
		def scriptDelegate = [myProperty:false]
		def script = {myProperty = true}
		
		assertFalse(scriptDelegate.myProperty)
		testee.runScriptClosure(script, scriptDelegate)
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
		testee.runScriptClosure(script, s)
		
		assertEquals(1, groups.size())
	}	
}
