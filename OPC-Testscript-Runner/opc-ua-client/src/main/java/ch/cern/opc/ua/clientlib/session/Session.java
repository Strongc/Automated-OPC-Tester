package ch.cern.opc.ua.clientlib.session;

import static ch.cern.opc.common.Log.logDebug;
import static ch.cern.opc.common.Log.logError;
import static ch.cern.opc.common.Log.logInfo;
import static ch.cern.opc.common.Log.logWarning;
import static ch.cern.opc.ua.clientlib.session.SessionState.CLOSED;
import static ch.cern.opc.ua.clientlib.session.SessionState.ERROR;
import static ch.cern.opc.ua.clientlib.session.SessionState.INITIAL;
import static ch.cern.opc.ua.clientlib.session.SessionState.READY;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.opcfoundation.ua.application.Client;
import org.opcfoundation.ua.application.SessionChannel;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.core.EndpointDescription;

import ch.cern.opc.ua.clientlib.addressspace.AddressSpace;
import ch.cern.opc.ua.clientlib.browse.Browser;
import ch.cern.opc.ua.clientlib.notification.OPCUAAsyncUpdateCallback;
import ch.cern.opc.ua.clientlib.notification.SubscriptionNotification;
import ch.cern.opc.ua.clientlib.notification.SubscriptionNotificationHandler;
import ch.cern.opc.ua.clientlib.read.Reader;
import ch.cern.opc.ua.clientlib.subscription.Subscription;
import ch.cern.opc.ua.clientlib.writer.Writer;

public class Session implements SubscriptionNotificationHandler
{
	private SessionState state = INITIAL;
	private SessionChannel channel = null;
	
	private final Client client;
	private final EndpointDescription endpoint;
	
	private String[] namespace = {};
	
	private AddressSpace addressSpace = null;
	private Browser browser = null;
	private Reader reader = null;
	private Writer writer = null;
	private Map<Object, Subscription> subscriptions = new HashMap<Object, Subscription>();
	private PublicationThread publicationThread = null;
	
	public Session(final Client client, final EndpointDescription endpoint)
	{
		this.client = client;
		this.endpoint = endpoint;
	}

	public SessionState getState() 
	{
		return state;
	}
	
	public EndpointDescription getEndpoint()
	{
		return endpoint;
	}
	
	public Client getClient()
	{
		return client;
	}
	
	public boolean setup()
	{
		return moveToTargetState(READY);
	}
	
	public boolean close()
	{
		return moveToTargetState(CLOSED);
	}
	
	private boolean moveToTargetState(final SessionState targetState)
	{
		logDebug("Current state is ["+state+"], moving to target state ["+targetState+"]");
		
		while(state != targetState)
		{
			state = state.moveToNext(this);
			
			if(state == ERROR) 
			{
				logError("Error occurred setting session state to ["+targetState+"]. Current state is ["+state+"]");
				break;
			}
		}
		
		return state == targetState;
	}
	
	public String[] getNamespace()
	{
		if(ArrayUtils.isEmpty(namespace))
		{
			if(browser != null)
			{
				namespace = new Browser(channel).browseNamespace();
			}
		}
		
		return namespace; 
	}

	public AddressSpace getAddressspace()
	{
		if(addressSpace == null)
		{
			if(browser != null)
			{
				addressSpace = new AddressSpace(browser.browseAddressspace());
			}
		}
		
		return addressSpace;
	}
	
	protected void setChannel(final SessionChannel channel)
	{
		this.channel = channel;
		
		if(channel == null)
		{
			addressSpace = null;
			browser = null;
			reader = null;
			writer = null;			
			
			publicationThread.stop();
			publicationThread = null;
		}
		else
		{
			browser = new Browser(channel);
			reader = new Reader(channel);
			writer = new Writer(channel);
			publicationThread = new PublicationThread(channel, this);
		}
	}
	
	protected SessionChannel getChannel()
	{
		return channel;
	}

	public Browser getBrowser() 
	{
		return browser;
	}

	public Writer getWriter() 
	{
		return writer;
	}
	
	public Subscription createSubscription(final String name, final OPCUAAsyncUpdateCallback dslCallback)
	{
		logDebug("Creating subscription ["+name+"]");
		
		Subscription result = getSubscription(name);
		if(result != null)
		{
			logWarning("subscription ["+name+"] already exists, active flag ["+result.isActive()+"]");
			return result;
		}
		
		result = new Subscription(channel, dslCallback);
		startPublicationThread();
		
		if(result.isActive())
		{
			addToSubscriptionMap(name, result);
			logDebug("Started subscription, name ["+name+"], subscription count ["+subscriptions.size()+"]");
			return result;
		}
		else
		{
			logError("Failed to create server subscription with name ["+name+"]");
			return null;
		}
	}
	
	public boolean deleteSubscription(final String name)
	{
		logDebug("Deleting subscription ["+name+"] - exists? ["+(getSubscription(name)!=null?"Y":"N")+"]");
		Subscription subscription = getSubscription(name);
		
		if(subscription != null)
		{
			if(getSubscriptionCount() == 1)
			{
				logInfo("Deleting the last subscription - stopping the publication thread");
				publicationThread.stop();
			}
			
			boolean result = subscription.delete();
			if(!result) logError("Failed to delete subscription name ["+name+"] id ["+subscription.getSubscriptionId()+"]");
			
			removeFromSubscriptionMap(name);
			return result;
		}
		
		return false;
	}

	/**
	 * Double ids to map to subscriptions - higher layers use the friendly
	 * name, lower layers (i.e. the stack) uses the subscription id to refer
	 * to the subscription. 
	 * @param name
	 * @param subscription
	 */
	private void addToSubscriptionMap(final String name, final Subscription subscription) 
	{
		subscriptions.put(name, subscription);
		subscriptions.put(subscription.getSubscriptionId(), subscription);
	}
	
	/**
	 * Counterpart to addToSubscriptionMap. Slightly cmoplicated due to subscriptions
	 * map containing 2 keys to same subscription (name and Id).
	 * Basically method finds the subscription object to remove then loops through
	 * finding all references to the subscription and removes the keys.
	 * @param name
	 */
	private void removeFromSubscriptionMap(final String name)
	{
		Subscription subscription = subscriptions.get(name);
		if(subscription == null) return;
		
		List<Object> keysToRemove = new ArrayList<Object>();
		
		for(Map.Entry<Object, Subscription> entry: subscriptions.entrySet())
		{
			if(subscription.getSubscriptionId().equals(entry.getValue().getSubscriptionId()))
			{
				keysToRemove.add(entry.getKey());
			}
		}
		
		for(Object key: keysToRemove)
		{
			subscriptions.remove(key);
		}
	}
	
	public Subscription getSubscription(final String name)
	{
		return subscriptions.get(name);
	}
	
	public Subscription getSubscription(final UnsignedInteger id)
	{
		return subscriptions.get(id);
	}

	public Reader getReader() 
	{
		return reader;
	}
	
	protected boolean startPublicationThread()
	{
		return publicationThread.start();
	}
	
	protected boolean stopPublicationThread()
	{
		return publicationThread.stop();
	}

	/**
	 * WARNING
	 * This method is most likely invoked by a thread dedicated to
	 * handling the asynchronous arrival of publish requests from
	 * the server. 
	 */
	@Override
	public void handle(final SubscriptionNotification notification) 
	{
		if(notification == null)
		{
			logWarning("Session received null notification - ignoring");
			return;
		}
		
		Subscription subscription = subscriptions.get(notification.getSubscriptionId());
		if(subscription == null)
		{
			logError("Session received referencing unknown subscription (id["+notification.getSubscriptionId()+"]), notification: "+notification);
			return;
		}
		
		subscription.handle(notification);
	}

	public int getSubscriptionCount() 
	{
		Set<Subscription> subscriptionSet = new HashSet<Subscription>();
		
		for(Map.Entry<Object, Subscription> entry: subscriptions.entrySet())
		{
			subscriptionSet.add(entry.getValue());
		}
		
		return subscriptionSet.size();
	}
	
	protected void injectMockPublicationThread(PublicationThread publicationThread)
	{
		logError("injecting a mock publication thread...");
		this.publicationThread = publicationThread;
	}
}
