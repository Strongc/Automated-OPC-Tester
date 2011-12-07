package ch.cern.opc.ua.dsl

import ch.cern.opc.ua.clientlib.UaClientInterface
import ch.cern.opc.ua.clientlib.UaClient

import static org.junit.Assert.*
import org.junit.Test
import org.junit.Before

class ScriptContextTest 
{
	private static final def SUBSCRIPTION_NAME = 'name of a subscription'
	private def testee
	
	private def createdSubscriptions = []
	
	@Before
	void setup()
	{
		def theClientInstance = [
			startSubscription: {name->
				println "started subscription name [${name}]"
				createdSubscriptions << name
				return false
			}
		] as UaClientInterface
	
		UaClient.metaClass.'static'.instance = {return theClientInstance}
		
		testee = new ScriptContext()
	}
	
	@Test
	void testCreateNewSubscription()
	{
		assertTrue(createdSubscriptions.empty)
		
		def result = testee.subscription(SUBSCRIPTION_NAME)
		
		assertEquals(SUBSCRIPTION_NAME, createdSubscriptions[0])
		assertEquals(SUBSCRIPTION_NAME, result.name)
	}
	
	@Test
	void testGetAlreadyExistingSubscription()
	{
		def alreadyExistingSubscription = testee.subscription(SUBSCRIPTION_NAME)
		assertEquals(1, createdSubscriptions.size)
		
		def result = testee.subscription(SUBSCRIPTION_NAME)
		
		assertEquals(1, createdSubscriptions.size)
		assertSame(result, alreadyExistingSubscription)
	}
}
