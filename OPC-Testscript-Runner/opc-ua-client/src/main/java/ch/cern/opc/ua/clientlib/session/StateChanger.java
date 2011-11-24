package ch.cern.opc.ua.clientlib.session;

interface StateChanger 
{
	public SessionState changeState(final Session session);
	public SessionState getTargetState();
}
