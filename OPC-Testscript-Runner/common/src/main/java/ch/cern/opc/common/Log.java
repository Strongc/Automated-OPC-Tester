package ch.cern.opc.common;

import java.io.IOException;

import javax.swing.JTextArea;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class Log 
{
	public static enum LogLevel {
		TRACE("trace"), DEBUG("debug"), INFO("info"), WARN("warn");
		
		private final String name;
		
		LogLevel(final String name)
		{
			this.name = name;
		}
		
		boolean nameMatches(final String name)
		{
			if(this.name.equalsIgnoreCase(name))
			{
				return true;
			}
			
			return false;
		}
		
		Level toLog4jLevel()
		{
			if(this == LogLevel.TRACE) return Level.TRACE;
			if(this == LogLevel.DEBUG) return Level.DEBUG;
			if(this == LogLevel.INFO) return Level.INFO;
			if(this == LogLevel.WARN) return Level.WARN;
			
			// default
			return Level.DEBUG;
		}
	}
	
	private final static String LOG_CATEGORY_NM = "script_runner";
	private final static LogLevel DEFAULT_LOG_LEVEL = LogLevel.INFO;
	
	protected static LimitedSizeTextLogger textArea;
	
	private static LogLevel logLevel = DEFAULT_LOG_LEVEL;
	private static final int MAX_OUTPUT_TEXT_AREA_CHAR_COUNT = 100000;

	static 
	{
		// console appender
		BasicConfigurator.configure();
		
		// create file appender
		try 
		{
			final String fmt = "%d{dd/MM/yyyy HH:mm:ss.SSS} %-5p: %m%n";
			final String logfile = "script_runner.log";
			
			BasicConfigurator.configure(new FileAppender(new PatternLayout(fmt), logfile));
		} 
		catch (IOException e) 
		{
			throw new RuntimeException("Failed to start script runner logfile");
		}
		
		// initialize to default level
		logLevel(DEFAULT_LOG_LEVEL.name());
	}
	
	public static void logLevel(final String levelString)
	{
		if(levelString == null) return;
		
		for(LogLevel level : LogLevel.values())
		{
			if(level.nameMatches(levelString))
			{
				logLevel = level;
			}
		}
		
		Logger.getLogger(LOG_CATEGORY_NM).setLevel(logLevel.toLog4jLevel());
	}

	public static void logError(Object msg)
	{
		updateTextComponent(msg);
		Logger.getLogger(LOG_CATEGORY_NM).error(msg);
	}

	public static void logWarning(Object msg)
	{
		updateTextComponent(msg);
		Logger.getLogger(LOG_CATEGORY_NM).warn(msg);
	}

	public static void logInfo(Object msg)
	{
		updateTextComponent(msg, LogLevel.INFO);
		Logger.getLogger(LOG_CATEGORY_NM).info(msg);
	}

	public static void logDebug(String msg)
	{
		updateTextComponent(msg, LogLevel.DEBUG);
		Logger.getLogger(LOG_CATEGORY_NM).debug(msg);
	}
	
	public static void logTrace(String msg) 
	{
		updateTextComponent(msg, LogLevel.TRACE);
		Logger.getLogger(LOG_CATEGORY_NM).trace(msg);
	}

	public static void setTextComponent(JTextArea textArea) 
	{
		Log.textArea = new LimitedSizeTextLogger(textArea, MAX_OUTPUT_TEXT_AREA_CHAR_COUNT);
	}
	
	private static void updateTextComponent(final Object msg, final LogLevel level)
	{
		if(level.compareTo(logLevel) >= 0)
		{
			updateTextComponent(msg);
		}
	}

	private static void updateTextComponent(final Object msg)
	{
		if(textArea != null)
		{
			textArea.publish(msg.toString());
		}
	}
}
