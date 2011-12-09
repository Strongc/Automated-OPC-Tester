package ch.cern.opc.ua.clientlib.notification;


public class MockUaDslCallback implements OPCUAAsyncUpdateCallback 
{
	public boolean wasInvoked = false;
	public String rcvdNodeId = null;
	public String rcvdAttributeId = null;
	public SubscriptionNotification rcvdNotification = null;

	@Override
	public int onUpdate(String nodeId, String attributeId, SubscriptionNotification notification) 
	{
		rcvdNodeId = nodeId;
		rcvdAttributeId = attributeId;		
		rcvdNotification = notification;
		
		System.out.println("Mock OPC-UA DSL callback called node ["+nodeId+"] attr ["+attributeId+"] notification:"+notification);
		wasInvoked = true;
		
		return 0;
	}
}
