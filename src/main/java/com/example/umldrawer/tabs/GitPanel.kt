package com.example.umldrawer.tabs

import com.intellij.openapi.observable.util.whenSizeChanged
import com.intellij.ui.components.JBScrollPane
import java20.console.JavaParserRunner
import java20.console.saveModel
import model.console.BuildModel
import org.eclipse.uml2.uml.Model
import org.repodriller.scm.GitRemoteRepository
import java.awt.Dimension
import java.awt.FlowLayout
import java.io.File
import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode
import kotlin.concurrent.thread


var myRepo:GitRemoteRepository? = null
val jTextArea = JTextArea()
var model:Model? = null
public fun createGit(): JPanel?{
    val gitPanel: JPanel = object : JPanel() {}
    initGit(gitPanel)
    return gitPanel
}

private fun initGit(gitJPanel: JPanel) {
    gitJPanel.layout = FlowLayout(FlowLayout.LEFT)
    val getButton = JButton("Get")
    val removeButton = JButton("Clear cache")
    val analyzeButton = JButton("Analyze")
    val clearLogButton = JButton("Clear log")
    val saveUmlFileButton = JButton("Save UML model")
    val getUmlFileButton = JButton("Get UML model")
    val urlField = JTextField()
    val filesTreeJBScrollPane = JBScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER)
    val logsJBScrollPane = JBScrollPane(jTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED)

    //TODO: create cache file for old values
    urlField.text = "https://github.com/arnohaase/a-foundation.git"

    gitJPanel.whenSizeChanged { urlField.preferredSize = (Dimension(gitJPanel.width - getButton.width - 40, getButton.height))
        filesTreeJBScrollPane.preferredSize = Dimension(gitJPanel.width - 20, (gitJPanel.height * 2f/3f).toInt())
        logsJBScrollPane.preferredSize = Dimension(gitJPanel.width - removeButton.width - 40,
            (gitJPanel.height * 1f/6f).toInt()
        )
    }

    getButton.addActionListener {
        clickGet(urlField, filesTreeJBScrollPane)
    }

    removeButton.addActionListener {
        filesTreeJBScrollPane.setViewportView(null)
        filesTreeJBScrollPane.updateUI()
        myRepo?.delete()
        jTextArea.append("Removed.\n")
    }

    clearLogButton.addActionListener {
        jTextArea.text = null
    }

    analyzeButton.addActionListener {
        val javaParserRunner = JavaParserRunner()
        try {
            if (myRepo != null) {
                thread {
                    val javaFiles = javaParserRunner.collectFiles(myRepo!!.repositoryPath)
                    jTextArea.append("Start analyzing.\n")
                    model = javaParserRunner.buildModel("JavaSampleModel", javaFiles, jTextArea)
                    jTextArea.append("End analyzing.\n")
                }
            } else jTextArea.append("Get some repo for analyzing.\n")
        } catch (e:Exception) {
            println(e.toString())
        }
    }

    saveUmlFileButton.addActionListener {
        if (model != null) {
            val fileChooser = JFileChooser()
            fileChooser.setDialogTitle("Save UML-model");
            val userSelection = fileChooser.showSaveDialog(gitJPanel)
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                val fileToSave = fileChooser.selectedFile
                jTextArea.append("Save as file: ${fileToSave.absolutePath}\n")
                model!!.saveModel(fileToSave.absolutePath)
            }
        } else jTextArea.append("There isn't model for getting file.\n")
    }

    //TODO: add listener for getUmlFileButton

    gitJPanel.add(urlField)
    gitJPanel.add(getButton)
    gitJPanel.add(filesTreeJBScrollPane)
    gitJPanel.add(removeButton)
    gitJPanel.add(logsJBScrollPane)
    gitJPanel.add(analyzeButton)
    gitJPanel.add(clearLogButton)
    gitJPanel.add(saveUmlFileButton)
    gitJPanel.add(getUmlFileButton)
}

private fun clickGet(textField: JTextField, jbScrollPane: JBScrollPane) {
    myRepo?.delete()
    //TODO: add regex
    if (textField.text != "") {
        jTextArea.append("Cloning: ${textField.text}\n")
        val buildModel = BuildModel()
        myRepo = buildModel.createRepo(textField.text)
        jTextArea.append("Cloned to ${myRepo!!.repositoryPath}\n")
        println("PRINT PATH: " + myRepo!!.repositoryPath)
        val root = DefaultMutableTreeNode(myRepo!!.repositoryPath.split("/").last())
        val directories = File(myRepo!!.repositoryPath).list { dir, name -> File(dir, name).isDirectory }
        buildTree(directories, root, myRepo!!.repositoryPath)
        jbScrollPane.setViewportView(JTree(root))
        jbScrollPane.updateUI()
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