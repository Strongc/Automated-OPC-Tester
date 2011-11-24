package ch.cern.opc.ua.clientlib.session;

import static ch.cern.opc.ua.clientlib.session.SessionState.CLOSED;
import static ch.cern.opc.ua.clientlib.session.SessionState.ERROR;
import static ch.cern.opc.ua.clientlib.session.SessionState.INITIAL;
import static ch.cern.opc.ua.clientlib.session.SessionState.READY;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.opcfoundation.ua.application.Client;
import org.opcfoundation.ua.application.SessionChannel;
import org.opcfoundation.ua.core.EndpointDescription;

import ch.cern.opc.ua.clientlib.addressspace.AddressSpace;
import ch.cern.opc.ua.clientlib.browse.Browser;
import ch.cern.opc.ua.clientlib.notification.SubscriptionNotification;
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
		System.out.println("Current state is ["+state+"], target state is ["+targetState+"]");
		
		while(state != targetState)
		{
			state = state.moveToNext(this);
			
			if(state == ERROR) break;
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
	
	public Subscription createSubscription(final String subscriptionId)
	{
		Subscription subscription = new Subscription(channel);
		if(subscription.isCreated())
		{
			addToSubscriptionMap(subscriptionId, subscription);
			System.out.println("Started subscription, name ["+subscriptionId+"], subscription count ["+subscriptions.size()+"]");
			return subscription;
		}
		else
		{
			System.err.println("Failed to create server subscription with name ["+subscriptionId+"]");
			return null;
		}
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
	
	public Subscription getSubscription(final String subscriptionId)
	{
		return subscriptions.get(subscriptionId);
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
			System.err.println("WARNING: null notification received at handler - ignoring");
			return;
		}
		
		Subscription subscription = subscriptions.get(notification.getSubscriptionId());
		if(subscription == null)
		{
			System.err.println("WARNING: notification received referencing invalid subscription, notification: "+notification);
			return;
		}
		
		subscription.onNotification(notification);
	}
}
