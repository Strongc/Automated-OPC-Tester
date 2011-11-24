package ch.cern.opc.ua.clientlib.notification;

import org.opcfoundation.ua.builtintypes.DataValue;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;

public class SubscriptionNotification 
{
	private final UnsignedInteger subscriptionId;
	private final UnsignedInteger clientHandle;
	private final DataValue value;
	
	public SubscriptionNotification(final UnsignedInteger subsriptionId, final UnsignedInteger clientHandle, final DataValue value) 
	{
		this.subscriptionId = subsriptionId;
		this.clientHandle = clientHandle;
		this.value = value;
	}

	public UnsignedInteger getSubscriptionId() 
	{
		return subscriptionId;
	}

	public UnsignedInteger getClientHandle() 
	{
		return clientHandle;
	}

	public DataValue getValue() 
	{
		return value;
	}
	
	@Override
	public String toString() 
	{
		return "subscription ["+subscriptionId.shortValue()+"] clientHandle ["+clientHandle.shortValue()+"] value ["+value+"]";
	}
	
}
