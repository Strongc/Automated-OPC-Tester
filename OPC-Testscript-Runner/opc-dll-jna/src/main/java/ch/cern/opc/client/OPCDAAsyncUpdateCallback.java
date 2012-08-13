package ch.cern.opc.client;

import com.sun.jna.Callback;

public interface OPCDAAsyncUpdateCallback extends Callback 
{
	int onUpdate(Update update);
}
