package ch.cern.opc.ua.clientlib.session;

import static ch.cern.opc.ua.clientlib.session.SessionState.*;

import org.opcfoundation.ua.common.ServiceFaultException;
import org.opcfoundation.ua.common.ServiceResultException;

class ToClosedState implements StateChanger {

	@Override
	public SessionState changeState(Session session) 
	{
		try 
		{
			session.getChannel().close();
			session.setChannel(null);
			return CLOSED;
		} 
		catch (ServiceFaultException e) 
		{
			e.printStackTrace(System.err);
		} 
		catch (ServiceResultException e) 
		{
			e.printStackTrace(System.err);
		}
		
		return ERROR;
	}

	@Override
	public SessionState getTargetState() 
	{
		return CLOSED;
	}
}
