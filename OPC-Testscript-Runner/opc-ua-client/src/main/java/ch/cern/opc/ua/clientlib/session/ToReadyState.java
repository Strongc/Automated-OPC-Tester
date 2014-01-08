package ch.cern.opc.ua.clientlib.session;

import static ch.cern.opc.ua.clientlib.session.SessionState.ERROR;
import static ch.cern.opc.ua.clientlib.session.SessionState.READY;
import static ch.cern.opc.common.Log.logError;

import org.apache.commons.lang3.ArrayUtils;

class ToReadyState implements StateChanger
{
	@Override
	public SessionState changeState(Session session) 
	{
		if(ArrayUtils.isEmpty(session.getNamespace()))
		{
			logError("Failed to read server namespace");
			return ERROR;
		}
		
		if(!(session.getAddressspace().getNodeCount() > 1))
		{
			logError("Failed to read server addressspace");
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
