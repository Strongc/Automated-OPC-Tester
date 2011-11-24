package ch.cern.opc.dsl.common.async

protected class AsyncTicker
{
	public final static def TIMER_PERIOD_SEC = 1
	private def timer
	
	def start(AsyncConditionManager asyncAssertManager)
	{
		if(timer != null)
		{
			throw new IllegalStateException('AssertAsynchTicker timer already started - illegal attempt to start another')
		}
		
		def timerTask = new TimerTask(){
			public void run()
			{
				asyncAssertManager.onTick()
			}
		}	
		
		timer = new Timer()
		timer.scheduleAtFixedRate(timerTask, new Date(), TIMER_PERIOD_SEC*1000)
	}
	
	def stop()
	{
		if(timer != null)
		{
			timer.cancel()
		}	
		timer = null
	}
}
