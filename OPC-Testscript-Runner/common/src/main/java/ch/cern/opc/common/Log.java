package ch.cern.opc.common;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class Log 
{
	private static Logger log;
	private static JTextComponent textComponent;
	
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

	public static void setTextComponent(JTextComponent textComponent) 
	{
		Log.textComponent = textComponent;
	}
	
	private static void updateTextComponent(Object msg)
	{
		if(textComponent != null)
		{
			Document doc = textComponent.getDocument();
			try 
			{
				doc.insertString(doc.getLength(), msg.toString() + "\n", null);
				textComponent.updateUI();
			}
			catch (BadLocationException e) 
			{
				log.error(e);
			}
		}
	}
}
