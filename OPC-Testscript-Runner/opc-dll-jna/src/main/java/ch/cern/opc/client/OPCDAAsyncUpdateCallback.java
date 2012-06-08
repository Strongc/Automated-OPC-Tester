package ch.cern.opc.client;

import com.sun.jna.Callback;

public interface OPCDAAsyncUpdateCallback extends Callback 
{
	int onUpdate(String itemPath, String value, int quality, int type, String timestamp);
}
