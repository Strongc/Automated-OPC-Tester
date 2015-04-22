package ch.cern.opc.ua.clientlib.session;

import static ch.cern.opc.common.Log.logDebug;
import static ch.cern.opc.common.Log.logError;
import static ch.cern.opc.common.Log.logTrace;
import static ch.cern.opc.common.Log.logWarning;
import static org.apache.commons.lang3.ArrayUtils.isEmpty;

import org.opcfoundation.ua.application.SessionChannel;
import org.opcfoundation.ua.builtintypes.ExpandedNodeId;
import org.opcfoundation.ua.builtintypes.ExtensionObject;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.common.ServiceFaultException;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.DataChangeNotification;
import org.opcfoundation.ua.core.MonitoredItemNotification;
import org.opcfoundation.ua.core.PublishRequest;
import org.opcfoundation.ua.core.PublishResponse;
import org.opcfoundation.ua.core.StatusCodes;
import org.opcfoundation.ua.core.SubscriptionAcknowledgement;
import org.opcfoundation.ua.encoding.DecodingException;
import org.opcfoundation.ua.transport.AsyncResult;

import ch.cern.opc.ua.clientlib.notification.SubscriptionNotification;
import ch.cern.opc.ua.clientlib.notification.SubscriptionNotificationHandler;

class PublicationThread implements Runnable
{
	private final static int MAX_STOP_START_WAIT_MS = 1000;
	private final static int STOP_START_SNOOZE_MS = 100;
	
	private final SessionChannel channel;
	private final SubscriptionNotificationHandler notificationHandler;
	private Boolean keepRunning = false;
	private Boolean isRunning = false;
	
	protected PublicationThread(SessionChannel channel, final SubscriptionNotificationHandler notificationHandler)
	{
		if(channel == null) throw new IllegalArgumentException("Cannot create PublishThread with null channel");
		if(notificationHandler == null) throw new IllegalArgumentException("Cannot create PublishThread with null notification handler");
		
		this.channel = channel;
		this.notificationHandler = notificationHandler;
		keepRunning = false;
	}
	
	public boolean isRunning()
	{
		return isRunning;
	}
	
	private void setIsRunning(final boolean value)
	{
		synchronized (isRunning) 
		{
			isRunning = value; 
		}
	}
	
	private void setKeepRunning(final boolean value)
	{
		synchronized (keepRunning) 
		{
			keepRunning = value;
		}
	}
	
	public boolean stop()
	{
		if(!isRunning()) return true;
		
		logDebug("Stopping publication thread");
		setKeepRunning(false);
		
		return waitForRunState(false, MAX_STOP_START_WAIT_MS);
	}

	private boolean waitForRunState(final boolean targetRunState, final int maxWait) 
	{
		for(int elapsedWait = 0; elapsedWait < maxWait; elapsedWait += STOP_START_SNOOZE_MS)
		{
			if(isRunning() == targetRunState) return true;
			
			try 
			{
				Thread.sleep(STOP_START_SNOOZE_MS);
			} 
			catch (InterruptedException e){} 
		}
		
		return false;
	}
	
	public boolean start()
	{
		if(isRunning()) return true;
		
		logDebug("Starting publication thread");
		setKeepRunning(true);
		
		new Thread(this).start();
		return waitForRunState(true, MAX_STOP_START_WAIT_MS);
	}

	@Override
	public void run() 
	{
		setIsRunning(true);
		logDebug("Started publication thread");
		
		mainLoop();
		
		setIsRunning(false);
		logDebug("Stopped publication thread");
	}

	private void mainLoop() 
	{
		PublishResponse lastResponse = null;
		
		while(keepRunning)
		{
			try 
			{
				final AsyncResult asyncResult = channel.PublishAsync(createPublishRequest(lastResponse));
				logTrace("Waiting for a response for publish request");
				
				final PublishResponse response = (PublishResponse) asyncResult.waitForResult();
				handleResponse(response);
				lastResponse = response;
			} 
			catch (ServiceFaultException e) 
			{
				// ignore timeouts - they're OK
				if(!StatusCodes.Bad_Timeout.equals(e.getStatusCode()))
				{
					e.printStackTrace();
				}
			} 
			catch (ServiceResultException e) 
			{
				e.printStackTrace();
			}
		}
	}

	private void handleResponse(final PublishResponse response) 
	{
		final ExtensionObject[] notifications = response.getNotificationMessage().getNotificationData();
		
		if(isEmpty(notifications))
		{
			logWarning("publication handler thread received empty notification");
			return;
		}
		
		for(ExtensionObject notificationObject : notifications)
		{
			if(notificationObject == null) continue;
			final ExpandedNodeId objectTypeId = notificationObject.getTypeId();
			
			if(objectTypeId.equals(DataChangeNotification.BINARY) || objectTypeId.equals(DataChangeNotification.XML))
			{
				sendNotification(response.getSubscriptionId(), notificationObject);
			}
			else
			{
				logWarning("publication handler thread received notification with unrecognised payload type ["+objectTypeId+"], ignoring");
			}
		}
	}

	private void sendNotification(final UnsignedInteger subscriptionId, final ExtensionObject object) {
		try 
		{
			// changed for upgrade to latest UA stack - probably broken
			DataChangeNotification notification = object.decode(null);
			
			MonitoredItemNotification[] items = notification.getMonitoredItems();
			for(MonitoredItemNotification item : items)
			{
				if(item == null) continue;
				
				notificationHandler.handle(
						new SubscriptionNotification(
								subscriptionId, 
								item.getClientHandle(), 
								item.getValue()));
			}
			
		} 
		catch (DecodingException e) 
		{
			logError("Publication handler thread failed to decode notification object");
			e.printStackTrace();
		}
	}

	private PublishRequest createPublishRequest(final PublishResponse lastResponse) 
	{
		final PublishRequest request = new PublishRequest();
		
		if(lastResponse != null)
		{
			
			SubscriptionAcknowledgement ack = new SubscriptionAcknowledgement(
					lastResponse.getSubscriptionId(), 
					lastResponse.getNotificationMessage().getSequenceNumber());
			request.setSubscriptionAcknowledgements(new SubscriptionAcknowledgement[]{ack});
		}
		
		return request;
	}
}