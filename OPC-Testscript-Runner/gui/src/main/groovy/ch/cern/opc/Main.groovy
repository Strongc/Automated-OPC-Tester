package ch.cern.opc

import groovy.swing.SwingBuilder
import javax.swing.*
import java.awt.*

import static javax.swing.JSplitPane.VERTICAL_SPLIT
import static javax.swing.JSplitPane.HORIZONTAL_SPLIT
import static java.awt.Color.*


class Main 
{
	static main(args) 
	{
		def builder = new SwingBuilder()
		
		def labelledPanel = 
		{text, bgColour ->
			builder.panel(background:bgColour)
			{
				label(text)
			}
		}
		
		def textAreaPanel = 
		{text ->
			builder.scrollPane()
			{
				textArea(text:text, editable:false)
			}
		}
		
		def menu = 
		{
			builder.menuBar()
			{
				menu(text: 'File', mnemonic:'F')
				{
					menuItem(text:'Open Script', mnemonic:'O')
				}	
			}
		}
		
		builder.edt {			
			frame(title:'OPC Script Runner', defaultCloseOperation:JFrame.EXIT_ON_CLOSE, pack:true, show:true, JMenuBar:menu()) {
				splitPane(orientation:HORIZONTAL_SPLIT, 
						leftComponent:labelledPanel('xml results tree', GREEN),
						rightComponent:splitPane(orientation: VERTICAL_SPLIT,
						topComponent:textAreaPanel('the text of the script'),
						bottomComponent:textAreaPanel('blah blah console output blah'))
				)
			}
		}
	}
}
