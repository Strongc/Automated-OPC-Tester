package ch.cern.opc.common;

public enum ItemAccessRight 
{
	UNKNOWN_ACCESS(0),
	READ_ACCESS(1),
	WRITE_ACCESS(2),
	READ_WRITE_ACCESS(3);
	
	public final int id;
	
	private ItemAccessRight(final int id)
	{
		this.id = id;
	}
	
	public static ItemAccessRight fromString(final String name)
	{
		for(ItemAccessRight accessRight: ItemAccessRight.values())
		{
			if(accessRight.toString() == name) return accessRight;
		}
		
		return UNKNOWN_ACCESS;
	}
	
	public static ItemAccessRight fromId(final int id)
	{
		for(ItemAccessRight accessRight: ItemAccessRight.values())
		{
			if(accessRight.id == id) return accessRight;
		}
		
		return UNKNOWN_ACCESS;
	}
}
