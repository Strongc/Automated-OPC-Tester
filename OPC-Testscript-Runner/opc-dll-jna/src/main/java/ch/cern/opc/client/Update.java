package ch.cern.opc.client;

import com.sun.jna.Structure;

/**
 * Class maps to the update structure passed to the java from the OPC client layer.
 * 
 * @author bfarnham
 */
public class Update extends Structure
{
	public String itemPath;
	public String attributeId;
	public String value;
	public int quality;
	public int type;
	public String timestamp;
}
