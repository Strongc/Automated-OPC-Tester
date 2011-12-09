package ch.cern.opc.ua.clientlib.session;

import static ch.cern.opc.ua.clientlib.session.SessionState.CONNECTED;
import static ch.cern.opc.ua.clientlib.session.SessionState.ERROR;

import org.opcfoundation.ua.application.Client;
import org.opcfoundation.ua.application.SessionChannel;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.ActivateSessionResponse;
import org.opcfoundation.ua.core.EndpointDescription;

class ToConnectedState implements StateChanger 
{

	@Override
	public SessionState changeState(Session session) 
	{
		try 
		{
			SessionChannel channel = createChannel(session.getClient(), session.getEndpoint());
			
			activateChannel(channel);
			
			session.setChannel(channel);
		} 
		catch (SessionStateChangeException e) 
		{
			System.err.println(e.getMessage());
			e.getCause().printStackTrace(System.err);
			
			return ERROR;
		}
		
		return CONNECTED;
	}

	@Override
	public SessionState getTargetState() 
	{
		return CONNECTED;
	}
	
	private SessionChannel createChannel(Client client, final EndpointDescription endpoint) throws SessionStateChangeException
	{
		try 
		{
			SessionChannel channel = client.createSessionChannel(endpoint);
			
			if(channel == null)
			{
				throw new SessionStateChangeException("Null channel created for endpoint ["+endpoint+"]");
			}
			
			if(!channel.getSecureChannel().isOpen())
			{
				throw new SessionStateChangeException("invalid channel created for endpoint ["+endpoint+"]");
			}
			
			return channel;
		} 
		catch (ServiceResultException e) 
		{
			throw new SessionStateChangeException("ServiceResultException thrown during channel creation to endpoint ["+endpoint+"]", e);
		}
	}
	
	private void activateChannel(SessionChannel channel) throws SessionStateChangeException
	{
		try 
		{
			ActivateSessionResponse response = channel.activate();
			
			if(response.getResponseHeader().getServiceResult().isNotGood())
			{
				throw new SessionStateChangeException("Server response for channel activate was bad, result:\n"+response);
			}
		} 
		catch (ServiceResultException e) 
		{
			throw new SessionStateChangeException("Failed to activate channel", e);
		}
	}
	
}
