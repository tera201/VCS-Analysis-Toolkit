package com.example.umldrawer.tabs

import com.intellij.openapi.observable.util.whenSizeChanged
import com.intellij.ui.components.JBScrollPane
import java20.console.JavaParserRunner
import model.console.BuildModel
import org.eclipse.uml2.uml.Model
import org.repodriller.scm.SCMRepository
import uml.util.UMLModelHandler
import java.awt.Dimension
import java.awt.FlowLayout
import java.io.File
import java.io.IOException
import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode
import kotlin.concurrent.thread


var myRepo:SCMRepository? = null
val jTextArea = JTextArea()
var model:Model? = null
val handler = UMLModelHandler()
val projectCache = "${System.getProperty("user.dir")}/UmlToolkitCache/"
val modelCache = "${System.getProperty("user.dir")}/UmlToolkitCache/Models"
public fun createGit(): JPanel?{
    val gitPanel: JPanel = object : JPanel() {}
    initGit(gitPanel)
    return gitPanel
}

private fun initGit(gitJPanel: JPanel) {

    try {
        File(projectCache).mkdirs()
        File(modelCache).mkdirs()
    } catch (e: IOException) {
        e.printStackTrace()
    }

    gitJPanel.layout = FlowLayout(FlowLayout.LEFT)
    val getButton = JButton("Get")
    val urlField = JTextField()
    val filesTreeJBScrollPane = JBScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER)
    val logsJBScrollPane = JBScrollPane(jTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED)

    //TODO: create cache file for old values
    urlField.text = "https://github.com/arnohaase/a-foundation.git"

    gitJPanel.whenSizeChanged { urlField.preferredSize = (Dimension(gitJPanel.width - getButton.width - 40, getButton.height))
        filesTreeJBScrollPane.preferredSize = Dimension(gitJPanel.width - 20, (gitJPanel.height * 2f/3f).toInt() - 10)
        logsJBScrollPane.preferredSize = Dimension(gitJPanel.width - 140,
            (gitJPanel.height * 1f/6f).toInt()
        )
    }

    getButton.addActionListener {
        clickGet(urlField, filesTreeJBScrollPane)
    }

    //TODO: add listener for getUmlFileButton

    gitJPanel.add(urlField)
    gitJPanel.add(getButton)
    gitJPanel.add(filesTreeJBScrollPane)
    addLogPanelButtons(gitJPanel, filesTreeJBScrollPane)
    gitJPanel.add(logsJBScrollPane)
    addModelControlPanel(gitJPanel)
}

private fun addLogPanelButtons(mainJPanel: JPanel, filesTreeJBScrollPane:JBScrollPane) {
    val logButtonsPanel = JPanel()
    logButtonsPanel.layout = BoxLayout(logButtonsPanel, BoxLayout.Y_AXIS)
    val removeButton = JButton("Clear cache")
    val clearLogButton = JButton("Clear log")

    removeButton.addActionListener {
        filesTreeJBScrollPane.setViewportView(null)
        filesTreeJBScrollPane.updateUI()
        myRepo?.scm?.delete()
        jTextArea.append("Removed.\n")
    }

    clearLogButton.addActionListener {
        jTextArea.text = null
    }

    logButtonsPanel.add(removeButton)
    logButtonsPanel.add(clearLogButton)
    mainJPanel.add(logButtonsPanel)
}

private fun addModelControlPanel(mainJPanel: JPanel) {
    val modelControlPanel = JPanel()

    val analyzeButton = JButton("Analyze")
    val saveUmlFileButton = JButton("Save UML model")
    val getUmlFileButton = JButton("Get UML model")

    analyzeButton.addActionListener {
        val javaParserRunner = JavaParserRunner()
        try {
            if (myRepo != null) {
                thread {
                    val javaFiles = javaParserRunner.collectFiles(myRepo!!.path)
                    jTextArea.append("Start analyzing.\n")
                    model = javaParserRunner.buildModel("JavaSampleModel", javaFiles, jTextArea)
                    jTextArea.append("End analyzing.\n")
                }
            } else jTextArea.append("Get some repo for analyzing.\n")
        } catch (e:Exception) {
            println(e.toString())
        }
    }

    getUmlFileButton.addActionListener {
        val fileChooser = JFileChooser()
        fileChooser.setDialogTitle("Get UML-model");
        fileChooser.currentDirectory = File(modelCache)
        val userSelection = fileChooser.showOpenDialog(mainJPanel)
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            val fileToSave = fileChooser.selectedFile
            jTextArea.append("Get model from file: ${fileToSave.absolutePath}\n")
            model = handler.loadModelFromFile(fileToSave.absolutePath)
        }
    }

    saveUmlFileButton.addActionListener {
        if (model != null) {
            val fileChooser = JFileChooser()
            fileChooser.setDialogTitle("Save UML-model")
            fileChooser.currentDirectory = File(modelCache)
            fileChooser.selectedFile = File("$modelCache/model.json")
            val userSelection = fileChooser.showSaveDialog(mainJPanel)
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                val fileToSave = fileChooser.selectedFile
                jTextArea.append("Save as file: ${fileToSave.absolutePath}\n")
//                model!!.saveModel(fileToSave.absolutePath)
                handler.saveModelToFile(model!!, fileToSave.absolutePath)
            }
        } else jTextArea.append("There isn't model for getting file.\n")
    }

    modelControlPanel.add(analyzeButton)
    modelControlPanel.add(saveUmlFileButton)
    modelControlPanel.add(getUmlFileButton)
    mainJPanel.add(modelControlPanel)
}

private fun clickGet(textField: JTextField, jbScrollPane: JBScrollPane) {
    myRepo?.scm?.delete()
    //TODO: add regex
    if (textField.text != "") {
        val buildModel = BuildModel()
        val directoryPath = "$projectCache/${buildModel.getRepoNameByUrl(textField.text)}"
        val directory = File(directoryPath)
        if (directory.exists() && directory.isDirectory) {
            jTextArea.append("Already exist!\n")
            myRepo = buildModel.getRepository(textField.text, projectCache)
        }
        else {
            jTextArea.append("Cloning: ${textField.text}\n")
            myRepo = buildModel.createClone(textField.text, projectCache)
            jTextArea.append("Cloned to ${myRepo!!.path}\n")

            val root = DefaultMutableTreeNode(myRepo!!.path.split("/").last())
            val directories = File(myRepo!!.path).list { dir, name -> File(dir, name).isDirectory }
            buildTree(directories, root, myRepo!!.path)
            jbScrollPane.setViewportView(JTree(root))
            jbScrollPane.updateUI()
        }
    }
}

private fun buildTree(directories: Array<String>?, rootTree:DefaultMutableTreeNode, rootPath:String) {
    if (directories != null)
    for (dir in directories) {
        val node = DefaultMutableTreeNode(dir)
        rootTree.add(node)
        val nodeRootPath = "$rootPath/$dir"
        val nodeFiles = File(nodeRootPath).list { dir, name -> File(dir, name).isFile }
        if (nodeFiles != null) for (nodeFile in nodeFiles) node.add(DefaultMutableTreeNode(nodeFile))
        val nodeDirectories = File(nodeRootPath).list { dir, name -> File(dir, name).isDirectory }
        buildTree(nodeDirectories, node, nodeRootPath)
    }
}