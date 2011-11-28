package ch.cern.opc.ua.clientlib.session;

import static ch.cern.opc.ua.clientlib.session.SessionState.*;

import org.apache.commons.lang3.ArrayUtils;

class ToReadyState implements StateChanger
{
	@Override
	public SessionState changeState(Session session) 
	{
		if(ArrayUtils.isEmpty(session.getNamespace()))
		{
			System.err.println("Failed to read server namespace");
			return ERROR;
		}
		
		if(!(session.getAddressspace().getNodeCount() > 1))
		{
			System.err.println("Failed to read server addressspace");
			return ERROR;
		}
		return READY;
	}

	@Override
	public SessionState getTargetState() 
	{
		return READY;
	}
}
