package ch.cern.opc.scriptRunner.results
import ch.cern.opc.scriptRunner.results.RunResults;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;

import groovy.xml.DOMBuilder
import groovy.xml.dom.DOMCategory
import ch.cern.opc.client.ClientApi
import ch.cern.opc.client.ClientInstance

import static ch.cern.opc.scriptRunner.results.RunResults.NULL_MSG
import static ch.cern.opc.scriptRunner.results.RunResults.EMPTY_MSG

class RunResultsTest 
{
	def testee
	
	@Before
	void setup()
	{
		testee = new RunResults()
		
		def theClientInstance = [
			getLastError:{it->
				return 'last error from DLL'
			}
			] as ClientApi
	
		ClientInstance.metaClass.'static'.getInstance = {-> return theClientInstance}
	}	
	
	@Test
	void testAddAssertTrue()
	{
		testee.assertTrue('should pass', true)
		testee.assertTrue('should fail', false)
		
		testee.addException(new Exception('deliberate exception'))
		
		testee.assertFalse('should pass', false)
		testee.assertFalse('should fail', true)
		
		testee.assertEquals('should pass', 1.0, 1.0)
		testee.assertEquals('should fail', 1.0, 2.0)
		
		assertEquals(7, testee.XML.getChildNodes().length)
	}
}
