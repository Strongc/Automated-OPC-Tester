package ch.cern.opc.client;

import java.nio.ByteBuffer;

import com.sun.jna.Library;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.IntByReference;

interface DllInterface extends Library 
{
    public void init(String host, String server);
    public void end();
    
    public boolean getItemNames(String[] itemsBuffer, int elementLength, int numElements, int offset);
    public NativeLong createGroup(String groupName, NativeLong requestedRefreshRate);
    public boolean destroyGroup(String groupName);
    public boolean addItem(String groupName, String itemPath);
    public boolean readItemSync(String groupName, String itemPath, int charBuffSz, ByteBuffer value, IntByReference quality, IntByReference type, ByteBuffer timestamp);
    public boolean readItemAsync(String groupName, String itemPath);
    public boolean writeItemSync(String groupName, String itemPath, String value);
    public boolean writeItemAsync(String groupName, String itemPath, String value);
    public void getLastError(byte buffer[], int bufferSz);
    public void registerAsyncUpdate(OPCDAAsyncUpdateCallback cb);
    public int getItemAccessRights(String groupName, String itemPath);
}
