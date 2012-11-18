package ch.cern.opc.common;

public class Quality 
{
	public enum State {
		GOOD("GOOD"), BAD("BAD"), UNCERTAIN("UNCERTAIN"), NA("NA");
		
		private final String string;
		
		State(final String s)
		{
			string = s;
		}
		
		@Override
		public String toString() 
		{
			return string;
		}
		
		public int toInt()
		{
			switch(this)
			{
			case GOOD:
				return 192;
			case BAD:
				return 0;
			case UNCERTAIN:
				return 64;
			case NA:
				return 128;
			}
			
			throw new IllegalStateException("Unrecognised quality state ["+this+"]");
		}
	};
	
	private static final int QUALITY_BIT_LOW_POS = 6;
	private static final int QUALITY_BIT_HIGH_POS = 7;

	private final int quality;
	public final State state;
	
	public Quality(final int quality)
	{
		this.quality = quality;
		
		if(qualityBitHigh() && qualityBitLow()) state = State.GOOD;
		else if(!qualityBitHigh() && qualityBitLow()) state = State.UNCERTAIN;
		else if(qualityBitHigh() && !qualityBitLow()) state = State.NA;
		else state = State.BAD;
	}
	
	@Override
	public String toString() 
	{
		return state.toString();
	}
	
	@Override
	public boolean equals(Object obj) 
	{
		if(obj == null) return false;
		
		if(this == obj) return true;
		
		if(obj instanceof Quality) return ((Quality)obj).state == this.state;
		
		if(obj instanceof Quality.State)
		{
			State objState = (State)obj;
			return state == objState;
		}
		
		return false;
	}
	
	private boolean qualityBitHigh()
	{
		return (quality & 1<<QUALITY_BIT_HIGH_POS) > 0;
	}
	
	private boolean qualityBitLow()
	{
		return (quality & 1<<QUALITY_BIT_LOW_POS) > 0; 
	}
}
