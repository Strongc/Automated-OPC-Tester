package ch.cern.opc.ua.dsl

import ch.cern.opc.ua.clientlib.UaClientInterface
import ch.cern.opc.ua.clientlib.UaClient

import static org.junit.Assert.*
import org.junit.Test
import org.junit.Before

class SubscriptionTest 
{
	static final def SUBSCRIPTION_NAME = "subscription name"
	
	private def startSubscriptionReturnValue = true
	
	private def testee
	
	@Before
	void setup()
	{
		def theClientInstance = [
			startSubscription: {name->
				println "started subscription name [${name}], returning [${startSubscriptionReturnValue}]"
				return startSubscriptionReturnValue
			}
		] as UaClientInterface
	
		UaClient.metaClass.'static'.instance = {return theClientInstance}

		testee = new Subscription(SUBSCRIPTION_NAME)
	}
	
	@Test
	void testIsStarted()
	{
		startSubscriptionReturnValue = true
		assertTrue(new Subscription(SUBSCRIPTION_NAME).isStarted)
		
		startSubscriptionReturnValue = false
		assertFalse(new Subscription(SUBSCRIPTION_NAME).isStarted)
	}
	
	
}
