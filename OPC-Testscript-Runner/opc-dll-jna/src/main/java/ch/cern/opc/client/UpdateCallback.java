package ch.cern.opc.client;

import com.sun.jna.Callback;

/**
 * Interface detailing the view that the C++ code has of the Java as regards
 * callback functionality.
 * 
 * @author bfarnham
 *
 */
public interface UpdateCallback extends Callback
{
	// int onUpdate(Update.ByReference update);	
	public int onUpdate(String path, String value, int quality, int type, String timestamp);
}
