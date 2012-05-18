package ch.cern.opc

import groovy.swing.SwingBuilder
import javax.swing.*
import java.awt.*

import ch.cern.opc.common.Log
import ch.cern.opc.scriptRunner.ScriptRunner
import ch.cern.opc.scriptRunner.OnCompleteCallback


import javax.swing.tree.DefaultMutableTreeNode as TreeNode

import static javax.swing.JSplitPane.VERTICAL_SPLIT
import static javax.swing.JSplitPane.HORIZONTAL_SPLIT
import static javax.swing.JFileChooser.FILES_ONLY
import static javax.swing.JFileChooser.APPROVE_OPTION
import static java.awt.Color.*
import static ch.cern.opc.common.Log.*

class ScriptRunnerGui implements OnCompleteCallback
{
	private def builder = new SwingBuilder()
	private def mainFrame
	private def scriptFile
	private def scriptTextArea
	private def outputTextArea
	private def scriptIsRunning = false
	private def resultsTree = new ResultsTree()
	
	private def exportScriptResultXmlFile = null
	private def scriptResultXml = null
	
	def ScriptRunnerGui(scriptFilePath)
	{
		println("ScriptRunnerGui created with [${scriptFilePath}]")
		scriptFile = new File(scriptFilePath + '\\dummy.opc.test')
	}
	
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
			menu(text: 'About', mnemonic:'A')
			{
				menuItem(text:'v1.1')
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
		if(scriptIsRunning)
		{
			logError("Cannot open new script, script [${scriptFile.path}] is running - wait until it completes")
			return
		}

		def currentDirectory = (scriptFile != null? new File(scriptFile.parent): null) 
		println("opening at directory [${currentDirectory}] parent of file [${scriptFile.path}]")
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
		if(scriptIsRunning)
		{
			logError("Cannot start new script run, script [${scriptFile.path}] is running - wait until it completes")
			return
		}
		scriptIsRunning = true
		
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
				isOPCUAScript(scriptFile.path),
				this)
			println(scriptResultXml)
		}
	}
	
	protected def isOPCUAScript(def scriptFilePath)
	{
		return (scriptFilePath == null? false: scriptFilePath.contains('opcua.test'))
	}
	
	private def exportScriptResultXml()
	{
		if(scriptIsRunning)
		{
			logError("Cannot export script results, script [${scriptFile.path}] is still running - wait until it completes")
			return
		}

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
	
	@Override
	void onComplete()
	{
		logDebug("GUI - onComplete called")
		this.scriptIsRunning = false
	}
}