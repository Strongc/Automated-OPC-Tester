package ch.cern.opc.client;

import ch.cern.opc.common.ItemValue;

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
	public final ItemValue value;
	
	public UpdateValue(final String itemPath, final String attributeId, final ItemValue value)
	{
		this.itemPath = itemPath;
		this.attributeId = attributeId;
		this.value = value;
	}
}
