package ch.cern.opc

import groovy.swing.SwingBuilder
import javax.swing.*
import java.awt.*

import ch.cern.opc.common.Log
import ch.cern.opc.scriptRunner.ScriptRunner


import javax.swing.tree.DefaultMutableTreeNode as TreeNode

import static javax.swing.JSplitPane.VERTICAL_SPLIT
import static javax.swing.JSplitPane.HORIZONTAL_SPLIT
import static javax.swing.JFileChooser.FILES_ONLY
import static javax.swing.JFileChooser.APPROVE_OPTION
import static java.awt.Color.*
import static ch.cern.opc.common.Log.*

class ScriptRunnerGui 
{
	private static final def SCRIPT_TEXT_AREA = 1
	private static final def OUTPUT_TEXT_AREA = 2
	
	private def builder = new SwingBuilder()
	private def mainFrame
	private def scriptFile
	private def textAreas = [:]
	private def resultsTree = new ResultsTree()
	
	def show()
	{
		mainFrame = builder.frame(title:'OPC Script Runner', defaultCloseOperation:JFrame.EXIT_ON_CLOSE, pack:true, show:true, JMenuBar:menu()) {
				splitPane(orientation:HORIZONTAL_SPLIT,
				leftComponent:labelledPanel('xml results tree', GREEN),
				rightComponent:splitPane(orientation: VERTICAL_SPLIT,
				topComponent:textAreaPanel(SCRIPT_TEXT_AREA, 'no script loaded'),
				bottomComponent:textAreaPanel(OUTPUT_TEXT_AREA, 'no script output'))
				)
			}

		builder.edt {mainFrame}
	}
	
	private def menu()
	{
		builder.menuBar()
		{
			menu(text: 'File', mnemonic:'F')
			{
				menuItem(text:'Open Script', mnemonic:'O', actionPerformed:{openScript()})
				menuItem(text:'Run Script', mnemonic:'R', actionPerformed:{runScript()})
			}
		}
	}
	
	private def textAreaPanel(id, initialText)
	{
		def textArea = builder.textArea(text:initialText, editable:false)
		textAreas[id] = textArea
		return new JScrollPane(textArea)
	}
	
	private def labelledPanel(text, bgColour)
	{
		return resultsTree.tree
	}
	
	private def openScript()
	{
		def scriptChooser = builder.fileChooser(id:'fileChooser', fileSelectionMode: FILES_ONLY)
		
		if(scriptChooser.showOpenDialog(mainFrame) == APPROVE_OPTION)
		{
			scriptFile = scriptChooser.selectedFile
			showScript(scriptFile)
		}
	}
	
	private def showScript(file)
	{
		textAreas[SCRIPT_TEXT_AREA].text = file.text
	}
	
	private def runScript()
	{
		textAreas[OUTPUT_TEXT_AREA].text = ''
		
		if(scriptFile == null)
		{
			textAreas[SCRIPT_TEXT_AREA].text = 'Choose a script...'
			return
		}
		
		showScript(scriptFile)
		Log.setTextComponent(textAreas[OUTPUT_TEXT_AREA])
		
		def thread = Thread.start{
			resultsTree.clearResults()
			def results = new ScriptRunner().runScript(scriptFile)
			resultsTree.addResults(results)
		}
	}
}