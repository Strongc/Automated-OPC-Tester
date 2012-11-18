package ch.cern.opc.common;

import org.apache.commons.lang.builder.EqualsBuilder;



public class ItemValue 
{
	public final String value;
	public final Quality quality;
	public final String timestamp;
	public final int datatype;

	public static final ItemValue ERROR_VALUE = new ItemValue("ERROR", 0, "ERROR", -1);

	public ItemValue(final String value, final int quality, final String timestamp, final int datatype)
	{
		this.value = (value == null?"":value);
		this.quality = new Quality(quality);
		this.timestamp = (timestamp == null?"":timestamp);
		this.datatype = datatype;
	}
	
	public ItemValue(final String value, final Quality.State quality, final String timestamp, final int datatype)
	{
		this(value, quality.toInt(), timestamp, datatype);
	}

	@Override
	public boolean equals(Object obj) 
	{
		if (obj instanceof ItemValue == false) 
		{
			return false;
		}

		if (this == obj) 
		{
			return true;
		}		
		
		
		ItemValue rhs = (ItemValue) obj;
		return new EqualsBuilder().
				append(value, rhs.value).
				append(quality, rhs.quality).
				append(timestamp, rhs.timestamp).
				append(datatype, rhs.datatype).
				isEquals();
	}
	
	@Override
	public String toString() 
	{
		return "value ["+value+"] quality ["+quality+"] timestamp ["+timestamp+"] datatype ["+datatype+"]";
	}

}
