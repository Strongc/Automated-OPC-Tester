package ch.cern.opc.ua.dsl

import ch.cern.opc.ua.clientlib.UaClientInterface
import ch.cern.opc.ua.clientlib.UaClient

import static org.junit.Assert.*
import org.junit.Test
import org.junit.Before

class SubscriptionTest 
{
	static final def SUBSCRIPTION_NAME = "subscription name"
	static final Node NODE_ONE = new Node('ns=1;s=counter1')
	static final def NODE_TWO = new Node('ns=1;s=counter2')
	static final def NODE_THREE = new Node('ns=1;s=counter3')
	
	private def startSubscriptionReturnValue = true
	private def monitorNodeValuesSubscription
	private def monitorNodeValuesNodeIds
	
	private def testee
	
	@Before 
	void initialiseMockClientInstanceVariables()
	{
		monitorNodeValuesSubscription = ""
		monitorNodeValuesNodeIds = []
	}
	
	@Before
	void setup()
	{
		def theClientInstance = [
			startSubscription: {name->
				println "started subscription name [${name}], returning [${startSubscriptionReturnValue}]"
				return startSubscriptionReturnValue
			},
			monitorNodeValues: {subscriptionName, nodeIds->
				println "monitorNodeValues subscription [${subscriptionName}] nodes [${nodeIds}]"
				monitorNodeValuesSubscription = subscriptionName
				monitorNodeValuesNodeIds.addAll(nodeIds)
				return true
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
	
	@Test
	void testMonitorCallsMonitorClientFunction()
	{
		testee.monitor(NODE_ONE, NODE_TWO, NODE_THREE)
		
		assertEquals(SUBSCRIPTION_NAME, monitorNodeValuesSubscription)
		
		assertEquals(3, monitorNodeValuesNodeIds.size())
		assertTrue(monitorNodeValuesNodeIds.contains(NODE_ONE.uaId))
		assertTrue(monitorNodeValuesNodeIds.contains(NODE_TWO.uaId))
		assertTrue(monitorNodeValuesNodeIds.contains(NODE_THREE.uaId))
	}
	
	@Test
	void testMonitorCallsMonitorClientFunctionOnlyForUnmonitoredNodes()
	{
		testee.monitor(NODE_ONE, NODE_ONE, NODE_ONE)
		
		assertEquals(SUBSCRIPTION_NAME, monitorNodeValuesSubscription)
		
		assertEquals(1, monitorNodeValuesNodeIds.size())
		assertTrue(monitorNodeValuesNodeIds.contains(NODE_ONE.uaId))
	}
}
