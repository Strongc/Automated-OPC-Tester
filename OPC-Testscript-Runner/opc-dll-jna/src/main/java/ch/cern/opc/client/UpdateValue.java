package ch.cern.opc.client;

/**
 * Class is an read-only clone of an Update object.
 * 
 * @author bfarnham
 *
 */
public class UpdateValue 
{
	public final String itemPath;
	public final String attributeId;
	public final String value;
	public final int quality;
	public final int type;
	public final String timestamp;	
	
	public UpdateValue(final String itemPath, final String attributeId, final String value, final int quality, final int type, final String timestamp)
	{
		this.itemPath = itemPath;
		this.attributeId = attributeId;
		this.value = value;
		this.quality = quality;
		this.type = type;
		this.timestamp = timestamp;
	}
}
