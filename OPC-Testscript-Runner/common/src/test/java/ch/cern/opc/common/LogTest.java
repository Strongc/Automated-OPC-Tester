package ch.cern.opc.common;

import static org.junit.Assert.*;

import javax.swing.JTextArea;
import javax.swing.text.JTextComponent;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LogTest 
{
	private JTextComponent textComponent;
	
	@Before
	public void setup()
	{
		textComponent = new JTextArea();
		Log.setTextComponent(textComponent);
	}
	
	@After
	public void teardown()
	{
		Log.setTextComponent(null);
	}
	
	
	@Test
	public void testTextComponentIsUpdated()
	{
		assertTrue(textComponent.getText().isEmpty());
		
		Log.logError("error");
		assertEquals("error\n", textComponent.getText());
		
		Log.logWarning("warning");
		Log.logInfo("info");
		Log.logDebug("debug");
		assertEquals("error\nwarning\ninfo\ndebug\n", textComponent.getText());
	}
}
