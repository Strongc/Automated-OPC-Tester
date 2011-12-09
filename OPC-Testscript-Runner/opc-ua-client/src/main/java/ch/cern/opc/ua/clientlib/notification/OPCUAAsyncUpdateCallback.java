package ch.cern.opc.ua.clientlib.notification;

public interface OPCUAAsyncUpdateCallback 
{
	int onUpdate(final String nodeId, final String attributeId, SubscriptionNotification notification);
}
