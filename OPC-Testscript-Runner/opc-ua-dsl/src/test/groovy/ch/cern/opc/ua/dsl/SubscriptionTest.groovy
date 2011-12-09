package ch.cern.opc.ua.dsl

import ch.cern.opc.ua.clientlib.UaClientInterface
import ch.cern.opc.ua.clientlib.UaClient

import static org.junit.Assert.*
import org.junit.Test
import org.junit.Before

class SubscriptionTest 
{
	static final def SUBSCRIPTION_NAME = "subscription name"
	static final def NODE_ONE_ID = "node_one_id"
	static final def NODE_TWO_ID = "node_two_id"
	static final def NODE_THREE_ID = "node_three_id"
	
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
		testee.monitor(
			new Node(NODE_ONE_ID),
			new Node(NODE_TWO_ID),
			new Node(NODE_THREE_ID))
		
		assertEquals(SUBSCRIPTION_NAME, monitorNodeValuesSubscription)
		
		assertEquals(3, monitorNodeValuesNodeIds.size())
		assertTrue(monitorNodeValuesNodeIds.contains(NODE_ONE_ID))
		assertTrue(monitorNodeValuesNodeIds.contains(NODE_TWO_ID))
		assertTrue(monitorNodeValuesNodeIds.contains(NODE_THREE_ID))
	}
	
	@Test
	void testMonitorCallsMonitorClientFunctionOnlyForUnmonitoredNodes()
	{
		testee.monitor(
			new Node(NODE_ONE_ID),
			new Node(NODE_ONE_ID),
			new Node(NODE_ONE_ID))
		
		assertEquals(SUBSCRIPTION_NAME, monitorNodeValuesSubscription)
		
		assertEquals(1, monitorNodeValuesNodeIds.size())
		assertTrue(monitorNodeValuesNodeIds.contains(NODE_ONE_ID))
	}

	
	
}
