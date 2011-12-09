package ch.cern.opc.ua.clientlib.notification;


public interface SubscriptionNotificationHandler 
{
	public void handle(final SubscriptionNotification notification);
}
