package ch.cern.opc.common;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

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
	}
	
	private final static LogLevel DEFAULT_LOG_LEVEL = LogLevel.INFO;
	
	private static Logger log;
	protected static JTextArea textArea;
	private static LogLevel logLevel = DEFAULT_LOG_LEVEL;

	static 
	{
		log = Logger.getLogger("OPC_Test_Script_Runner");
		log.setLevel(Level.DEBUG);
		BasicConfigurator.configure();
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
	}

	public static void logError(Object msg)
	{
		updateTextComponent(msg);
		log.error(msg);
	}

	public static void logWarning(Object msg)
	{
		updateTextComponent(msg);
		log.warn(msg);
	}

	public static void logInfo(Object msg)
	{
		updateTextComponent(msg, LogLevel.INFO);
		log.info(msg);
	}

	public static void logDebug(String msg)
	{
		updateTextComponent(msg, LogLevel.DEBUG);
		log.debug(msg);
	}
	
	public static void logTrace(String msg) 
	{
		updateTextComponent(msg, LogLevel.TRACE);
		log.trace(msg);
	}

	public static void setTextComponent(JTextArea textArea) 
	{
		Log.textArea = textArea;
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
		if(textArea == null) return;
		
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				textArea.append(msg + "\n");
				textArea.setCaretPosition(textArea.getText().length() - 1);
			}
		});
	}
}
