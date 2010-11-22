package ch.cern.opc.client;

import com.sun.jna.Callback;

public interface AsyncUpdateCallback extends Callback 
{
	int onUpdate(String itemPath, String value);
}
