package ch.cern.opc.client;

public interface ClientApi 
{
	public enum State {UNINITIALISED, CREATED, DESTROYED}
	
	public boolean init(String host, String server);
	public boolean getItemNames();
	public boolean destroy();
	
	public boolean createGroup(String groupName, long refreshRateMs);
	public boolean addItem(String groupName, String itemPath);
	public String readItemSync(String groupName, String itemPath);
	public boolean writeItemSync(String groupName, String itemPath, String value);
	public String getLastError();
	
	public State getState();
}
