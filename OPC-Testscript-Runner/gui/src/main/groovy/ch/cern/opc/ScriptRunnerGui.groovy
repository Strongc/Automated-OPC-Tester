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
	private def builder = new SwingBuilder()
	private def mainFrame
	private def scriptFile
	private def scriptTextArea
	private def outputTextArea
	private def resultsTree = new ResultsTree()
	
	private def exportScriptResultXmlFile = null
	private def scriptResultXml = null
	
	def show()
	{
		mainFrame = builder.frame(title:'OPC Script Runner', defaultCloseOperation:JFrame.EXIT_ON_CLOSE, pack:true, show:true, JMenuBar:menu()) {
				splitPane(orientation:HORIZONTAL_SPLIT,
				leftComponent:labelledPanel('xml results tree', GREEN),
				rightComponent:splitPane(orientation: VERTICAL_SPLIT,
				topComponent:scriptTextPanel('no script loaded'),
				bottomComponent:outputTextPanel('no script output'))
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
				menuItem(text:'Export Script Result (XML)', mnemonic:'E', actionPerformed:{exportScriptResultXml()})
			}
		}
	}
	
	private def scriptTextPanel(initialText)
	{
		scriptTextArea = builder.textArea(text:initialText, editable:false)
		return new JScrollPane(scriptTextArea)
	}
	
	private def outputTextPanel(initialText)
	{
		outputTextArea = builder.textArea(text:initialText, editable:false)
		return new JScrollPane(outputTextArea)
	}
	
	private def labelledPanel(text, bgColour)
	{
		return new JScrollPane(resultsTree.tree)
	}
	
	private def openScript()
	{
		def currentDirectory = (scriptFile != null? new File(scriptFile.parent): null) 
		def scriptChooser = builder.fileChooser(id:'fileChooser', fileSelectionMode: FILES_ONLY, currentDirectory: currentDirectory)
		
		if(scriptChooser.showOpenDialog(mainFrame) == APPROVE_OPTION)
		{
			scriptFile = scriptChooser.selectedFile
			showScript(scriptFile)
		}
	}
	
	private def showScript(file)
	{
		scriptTextArea.text = file.text
	}
	
	private def runScript()
	{
		outputTextArea.text = ''
		scriptResultXml = null
		
		if(scriptFile == null)
		{
			scriptTextArea.text = 'Choose a script...'
			return
		}
		
		showScript(scriptFile)
		Log.setTextComponent(outputTextArea)
		
		def thread = Thread.start{
			resultsTree.clearResults()
			resultsTree.initTree()
			scriptResultXml = new ScriptRunner().runScript(
				scriptFile, 
				resultsTree, 
				isOPCUAScript(scriptFile.path))
			println(scriptResultXml)
		}
	}
	
	protected def isOPCUAScript(def scriptFilePath)
	{
		return (scriptFilePath == null? false: scriptFilePath.contains('opcua.test'))
	}
	
	private def exportScriptResultXml()
	{
		if(scriptResultXml == null)
		{
			scriptTextArea.text = 'no results to export...'
			return
		}
		else
		{
			def currentDirectory = (exportScriptResultXmlFile != null? new File(exportScriptResultXmlFile.parent): null)
			def scriptResultChooser = builder.fileChooser(id:'fileChooser', fileSelectionMode: FILES_ONLY, currentDirectory: currentDirectory)
			
			if(scriptResultChooser.showSaveDialog(mainFrame) == APPROVE_OPTION)
			{
				exportScriptResultXmlFile = scriptResultChooser.selectedFile
				logInfo("Exporting script results (XML) to [${exportScriptResultXmlFile.path}}]")
				
				new File(exportScriptResultXmlFile.path).with 
				{
					append("\n<!--script runner results for script [${scriptFile.path}] -->\n\n")
					append(scriptResultXml)
				}
			}
	
		}
		
	}
}