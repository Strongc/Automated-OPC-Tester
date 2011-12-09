package ch.cern.opc.ua.clientlib.session;

import static ch.cern.opc.ua.clientlib.session.SessionState.INITIAL;

class ToInitialState implements StateChanger 
{
	@Override
	public SessionState changeState(Session session) 
	{
		session.setChannel(null);
		
		return INITIAL;
	}

	@Override
	public SessionState getTargetState() 
	{
		return INITIAL;
	}
}
