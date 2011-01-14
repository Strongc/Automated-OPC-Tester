package ch.cern.opc.common;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class Log 
{
	private static Logger log;
	private static JTextArea textArea;

	static 
	{
		log = Logger.getLogger("OPC_Test_Script_Runner");
		log.setLevel(Level.DEBUG);
		BasicConfigurator.configure();
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
		updateTextComponent(msg);
		log.info(msg);
	}

	public static void logDebug(String msg)
	{
		updateTextComponent(msg);
		log.debug(msg);
	}

	public static void setTextComponent(JTextArea textArea) 
	{
		Log.textArea = textArea;
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
