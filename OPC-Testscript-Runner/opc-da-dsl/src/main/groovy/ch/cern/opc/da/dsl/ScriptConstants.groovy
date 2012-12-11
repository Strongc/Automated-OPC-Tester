package ch.cern.opc.da.dsl

import ch.cern.opc.common.Quality
import ch.cern.opc.common.Datatype

class ScriptConstants 
{
	/*
	 * Slightly cheesy repetition - in order that scripts
	 * run in a context which knows about Quality enumerated
	 * states.
	 * 
	 * If there is a better way, go for it.
	 */
	public final static GOOD = Quality.State.GOOD
	public final static BAD = Quality.State.BAD
	public final static UNCERTAIN = Quality.State.UNCERTAIN
	public final static NA = Quality.State.NA
	
	/*
	 * And more cheesy repetition of datatype identifiers of OPC
	 */
	public final static VT_EMPTY = Datatype.VT_EMPTY
	public final static VT_I2 = Datatype.VT_I2
	public final static VT_I4 = Datatype.VT_I4
	public final static VT_R4 = Datatype.VT_R4
	public final static VT_R8 = Datatype.VT_R8
	public final static VT_CY = Datatype.VT_CY
	public final static VT_DATE = Datatype.VT_DATE
	public final static VT_BSTR = Datatype.VT_BSTR
	public final static VT_BOOL = Datatype.VT_BOOL
	public final static VT_I1 = Datatype.VT_I1
	public final static VT_UI1 = Datatype.VT_UI1
	public final static VT_UI2 = Datatype.VT_UI2
	public final static VT_UI4 = Datatype.VT_UI4
	public final static VT_I8 = Datatype.VT_I8
	public final static VT_UI8 = Datatype.VT_UI8
	public final static VT_INT = Datatype.VT_INT 

	public final static VT_UNRECOGNISED = Datatype.VT_UNRECOGNISED
}
