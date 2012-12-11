package ch.cern.opc.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.builder.EqualsBuilder;

public class Timestamp 
{
	public final static String INVALID_TIMESTAMP = "INVALID TIMESTAMP";
	public final static String DATE_FORMAT_STRING = "yyyy/M/d-H:m:s.SSS";
	
	private String timeString;
	private Date date;
	
	private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_STRING);
	
	public Timestamp(final String timeString) 
	{
		this.timeString = timeString;
	}
	
	public Timestamp(final Date date)
	{
		if(date != null)
		{
			timeString = DATE_FORMAT.format(date);
			this.date = date;
		}
		else
		{
			timeString = INVALID_TIMESTAMP;
		}
	}
	
	@Override
	public boolean equals(Object obj) 
	{
		if (obj instanceof Timestamp == false) 
		{
			return false;
		}

		if (this == obj) 
		{
			return true;
		}
		
		Timestamp rhs = (Timestamp) obj;
		return new EqualsBuilder().append(timeString, rhs.timeString).isEquals();
	}
	
	@Override
	public String toString() 
	{
		if(timeString == null) return INVALID_TIMESTAMP;
		return timeString;
	}

	public Date toDate() 
	{
		if(date == null)
		{
			date = toDate(timeString);
			if(date == null)
			{
				timeString = INVALID_TIMESTAMP;
			}
		}
		
		return date;
	}
	
	private Date toDate(final String timeString)
	{
		try 
		{
			return DATE_FORMAT.parse(timeString);
		} 
		catch (ParseException e) 
		{
			Log.logWarning("Failed to parse timestamp string ["+timeString+"] to date object");
		}
		catch(NullPointerException e)
		{
			Log.logWarning("Failed to parse timestamp string ["+timeString+"] to date object");
		}
		
		return null;
	}

	public boolean isAfter(Date time) 
	{
		if(time == null) return false;
		return toDate().compareTo(time) > 0;
	}

	public boolean isAfter(String time) 
	{
		return isAfter(toDate(time));
	}
	
	public boolean isAfter(Timestamp time) 
	{
		return isAfter(time != null?time.toDate(): null);
	}
	
	public boolean isBefore(Date time) 
	{
		if(time == null) return false;
		return toDate().compareTo(time) < 0;
	}

	public boolean isBefore(String time) 
	{
		return isBefore(toDate(time));
	}

	public boolean isBefore(Timestamp time) 
	{
		return isBefore(time != null?time.toDate(): null);
	}
}
