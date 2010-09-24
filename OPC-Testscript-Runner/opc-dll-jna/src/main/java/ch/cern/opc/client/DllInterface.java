package ch.cern.opc.client;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;

interface DllInterface extends Library 
{
	DllInterface INSTANCE = (DllInterface)
    Native.loadLibrary("AutomatedOpcTester.dll", DllInterface.class);

    public void init(String host, String server);
    public void getItemNames();
    public NativeLong createGroup(String groupName, NativeLong requestedRefreshRate);
    public boolean addItem(String groupName, String itemPath);
    public boolean readItemSync(String groupName, String itemPath, byte buffer[], int bufferSz);
}
