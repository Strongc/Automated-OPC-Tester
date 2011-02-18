package ch.cern.opc.common;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;

class LimitedSizeTextLogger 
{
	private final static int PRUNING_PERCENTAGE = 10;
	
	private final int maxChars;
	private final JTextArea textArea;

	public LimitedSizeTextLogger(JTextArea textArea, int maxChars)
	{
		this.textArea = textArea;		
		this.maxChars = maxChars;
	}

	public int getMaxChars() 
	{
		return maxChars;
	}

	public String getText() 
	{
		return textArea.getText();
	}
	
	public void publish(final String msg) 
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				textArea.append(msg + "\n");
				
				if(docLength() > maxChars)
				{
					pruneDoc();
				}
				
				textArea.setCaretPosition(textArea.getText().length() - 1);
			}
			
			private int docLength()
			{
				return textArea.getDocument().getLength();
			}
			
			private void pruneDoc()
			{
				final int overflowCharCount = docLength() - maxChars;
				final int pruningCharCount = (maxChars * PRUNING_PERCENTAGE)/100;
				final int numCharsToRemove = overflowCharCount + pruningCharCount;
				
				try 
				{
					textArea.getDocument().remove(0, numCharsToRemove);
				} 
				catch (BadLocationException e)
				{
					throw new RuntimeException("Failed to prune text output field, size was ["+docLength()+"] and failed to prune [] chars");
				}
			}
		});
	}
}
