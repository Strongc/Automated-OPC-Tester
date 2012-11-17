package ch.cern.opc.da.dsl

import ch.cern.opc.common.Quality;

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
}
