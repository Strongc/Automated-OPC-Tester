package ch.cern.opc.ua.clientlib.session;

import ch.cern.opc.ua.clientlib.notification.SubscriptionNotification;

public interface SubscriptionNotificationHandler 
{
	public void handle(final SubscriptionNotification notification);
}
