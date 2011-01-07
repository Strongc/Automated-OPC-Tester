package ch.cern.opc.client;

import java.util.List;

public interface ClientApi 
{
	public enum State {UNINITIALISED, CREATED, DESTROYED}
	
	public boolean init(String host, String server);
	public List<String> getItemNames();
	public boolean destroy();
	
	public boolean createGroup(String groupName, long refreshRateMs);
	public boolean destroyGroup(String groupName);
	public boolean addItem(String groupName, String itemPath);
	public String readItemSync(String groupName, String itemPath);
	public boolean readItemAsync(String groupName, String itemPath);
	public boolean writeItemSync(String groupName, String itemPath, String value);
	public boolean writeItemAsync(String groupName, String itemPath, String value);
	public void registerAsyncUpdate(AsyncUpdateCallback callback);
	public String getLastError();
	
	public State getState();
}
