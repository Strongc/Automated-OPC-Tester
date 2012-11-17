package ch.cern.opc.client;

import ch.cern.opc.common.Quality;


public class ItemValue 
{
	public final String value;
	public final Quality quality;
	public final String timestamp;
	public final int datatype;

	public static final ItemValue ERROR_VALUE = new ItemValue("ERROR", 0, "ERROR", -1);
	
	public ItemValue(final String value, final int quality, final String timestamp, final int datatype)
	{
		if(value == null) throw new IllegalArgumentException("value cannot be null (but empty string is acceptable)");
		if(timestamp == null) throw new IllegalArgumentException("timestamp cannot be null");
		this.value = value;
		this.quality = new Quality(quality);
		this.timestamp = timestamp;
		this.datatype = datatype;
	}
	
}
