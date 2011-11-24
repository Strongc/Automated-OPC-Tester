package ch.cern.opc.ua.clientlib.session;

import org.opcfoundation.ua.common.ServiceResultException;

@SuppressWarnings("serial")
class SessionStateChangeException extends Exception 
{
	public SessionStateChangeException(String message, ServiceResultException e) 
	{
		super(message, e);
	}

	public SessionStateChangeException(String message) 
	{
		super(message);
	}
}
