package ch.cern.opc.client;

import java.util.List;

import ch.cern.opc.common.ItemAccessRight;
import ch.cern.opc.common.ItemValue;
import ch.cern.opc.common.Datatype;

public interface OPCDAClientApi 
{
	public enum State {UNINITIALISED, CREATED, DESTROYED}
	
	public boolean init(String host, String server);
	public void end();
	public List<String> getItemNames();
	
	public boolean createGroup(String groupName, long refreshRateMs);
	public boolean destroyGroup(String groupName);
	public boolean addItem(String groupName, String itemPath);
	public ItemValue readItemSync(String groupName, String itemPath);
	public boolean readItemAsync(String groupName, String itemPath);
	public boolean writeItemSync(String groupName, String itemPath, String value);
	public boolean writeItemAsync(String groupName, String itemPath, String value);
	public void registerAsyncUpdate(OPCDAAsyncUpdateCallback callback);
	public ItemAccessRight getItemAccessRights(String groupName, String itemPath);
	public Datatype getItemDatatype(String groupName, String itemPath);
	
	public String getLastError();
	
	public State getState();
}
