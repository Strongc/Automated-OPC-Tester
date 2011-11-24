package ch.cern.opc.ua.clientlib.session;

public enum SessionState 
{
	ERROR(new ToInitialState()), 
	INITIAL(new ToConnectedState()),
	CONNECTED(new ToReadyState()),
	READY(new ToClosedState()), 
	CLOSED(new ToInitialState());
	
	private final StateChanger stateChanger;
	
	SessionState(StateChanger changer)
	{
		this.stateChanger = changer;
	}
	
	public SessionState moveToNext(Session session)
	{
		try
		{
			return stateChanger.changeState(session);
		}
		catch(IllegalStateException e)
		{
			return ERROR;
		}
	}
}
