package ch.cern.opc.common;

import org.apache.commons.lang.StringUtils;

public enum Datatype 
{
	VT_EMPTY(0, "VT_EMPTY"), 
	VT_I2(2, "VT_I2"), 
	VT_I4(3, "VT_I4"),
	VT_R4(4, "VT_R4"), 
	VT_R8(5, "VT_R8"), 
	VT_CY(6, "VT_CY"), 
	VT_DATE(7, "VT_DATE"), 
	VT_BSTR(8, "VT_BSTR"), 
	VT_BOOL(11, "VT_BOOL"), 
	VT_I1(16, "VT_I1"),
	VT_UI1(17, "VT_UI1"),
	VT_UI2(18, "VT_UI2"),
	VT_UI4(19, "VT_UI4"),
	VT_I8(20, "VT_I8"),
	VT_UI8(21, "VT_UI8"),
	VT_INT(22, "VT_INT"),
	VT_UNRECOGNISED(999999, "VT_UNRECOGNISED");
	
	public final int datatypeId;
	public final String name;
	
	private Datatype(final int id, final String nm) 
	{
		datatypeId = id;
		name = nm;
	}
	
	@Override
	public String toString() 
	{
		return name;
	}
	
	public static Datatype getDatatypeById(final int datatypeId)
	{
		for(Datatype type: Datatype.values())
		{
			if(datatypeId == type.datatypeId) return type;
		}
		
		return VT_UNRECOGNISED;
	}
	
	public static Datatype getDatatypeByName(final String datatypeName)
	{
		for(Datatype type: Datatype.values())
		{
			if(StringUtils.equals(datatypeName, type.name)) return type;
		}
		
		return VT_UNRECOGNISED;
	}
	
	
}
