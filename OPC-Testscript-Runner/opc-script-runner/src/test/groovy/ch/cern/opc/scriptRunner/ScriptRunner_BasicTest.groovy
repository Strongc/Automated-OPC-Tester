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
	def stubDelegate
	def clientState
	
	@Before
	void setup()
	{
		testee = new ScriptRunner()
		stubDelegate = [onScriptStart:{}, onScriptEnd:{}]
	}
	
	@Test
	void testRunScriptRunsTheScript()
	{
		def wasScriptRun = false
		
		testee.runScriptClosure({wasScriptRun = true}, stubDelegate)
		assertTrue(wasScriptRun)
	}
	
	@Test
	void testRunScriptWithDelegateFindsDelegateProperties()
	{
		stubDelegate.myProperty = false
		
		def script = {myProperty = true}
		
		assertFalse(stubDelegate.myProperty)
		testee.runScriptClosure(script, stubDelegate)
		assertTrue(stubDelegate.myProperty)
	}

	@Test
	void testRunScriptCallsOnScriptStartBeforeAndOnScriptEndAfter()
	{
		def events = []
		
		def scriptDelegate = [
			onScriptStart:{events << 'script started'},
			onScriptEnd:{events << 'script ended'}]
		
		testee.runScriptClosure({events << 'script run'}, scriptDelegate)
		
		assertEquals(3, events.size())
		assertEquals('script started', events[0])
		assertEquals('script run', events[1])
		assertEquals('script ended', events[2])
	}	
}
