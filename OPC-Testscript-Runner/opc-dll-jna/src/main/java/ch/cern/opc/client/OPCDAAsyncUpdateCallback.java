package ch.cern.opc.client;

import java.util.concurrent.BlockingDeque;
import static ch.cern.opc.common.Log.*;

/**
 * Class handles the immediate callback from the C++ into the Java layer.
 * Essentially the job of this class is simply to clone the data and let
 * the native thread return as soon as possible. Handling the update data
 * is handled by some other thread (which processes the updateQueue)
 * 
 * @author bfarnham
 *
 */
public class OPCDAAsyncUpdateCallback implements UpdateCallback 
{
	private final static int QUEUE_SZ_WARNING_THRESHOLD = 100;
	private final static int MAX_UPDATE_QUEUE_RETRIES = 5;
	private final static int QUEUE_RETRY_SLEEP_MS = 5;
	
	private final BlockingDeque<UpdateValue> updateQueue;
	
	public OPCDAAsyncUpdateCallback(BlockingDeque<UpdateValue> updateQueue)
	{
		this.updateQueue = updateQueue;
	}
	
	@Override
	public int onUpdate(String path, String value, int quality, int type, String timestamp)
	{
		logTrace("update arrived, current queue size ["+updateQueue.size()+"]");
		
		for(int retry = 0; retry < MAX_UPDATE_QUEUE_RETRIES; retry++)
		{
			UpdateValue update = new UpdateValue(path, "", value, quality, type, timestamp);
			
			if(updateQueue.offerLast(update))
			{
				if(updateQueue.size() > QUEUE_SZ_WARNING_THRESHOLD)
				{
					logWarning("Update queue has ["+updateQueue.size()+"] entries, (warning threshold is ["+QUEUE_SZ_WARNING_THRESHOLD+"]");
				}
				
				return 1;
			}
			else
			{
				try 
				{
					Thread.sleep(QUEUE_RETRY_SLEEP_MS);
				} 
				catch (InterruptedException e){}
			}
		}
		
		logError("Updates arriving from the OPC client dll are not being queued within ["+QUEUE_RETRY_SLEEP_MS*MAX_UPDATE_QUEUE_RETRIES+"ms], current queue size ["+updateQueue.size()+"]");
		return 0;
	}
	
}