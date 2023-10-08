package com.example.umldrawer.tabs

import com.example.umldrawer.settings.UMLToolkitSettings
import com.example.umldrawer.utils.toCity
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.observable.util.whenSizeChanged
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.ui.components.JBScrollPane
import java20.console.JavaParserRunner
import javafx.application.Platform
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

class GitPanel : JPanel() {
    private var settings:UMLToolkitSettings = UMLToolkitSettings.getInstance()
    private var myRepo: SCMRepository? = null
    private val jTextArea = JTextArea()
    companion object {
        var model: Model? = null
    }
    private val handler = UMLModelHandler()
    private val projectCache = "${System.getProperty("user.dir")}/UmlToolkitCache/"
    private val modelCache = "${System.getProperty("user.dir")}/UmlToolkitCache/Models"
    
    init {
        initGit()
    }

    private fun initGit() {

        try {
            File(projectCache).mkdirs()
            File(modelCache).mkdirs()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        this.layout = FlowLayout(FlowLayout.LEFT)
        val getButton = JButton("Get")
        val urlField = JTextField()
        val filesTreeJBScrollPane =
            JBScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER)
        val logsJBScrollPane = JBScrollPane(
            jTextArea,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        )
        logsJBScrollPane.isVisible = settings.showGitLogs

        ApplicationManager.getApplication().messageBus.connect()
            .subscribe(UMLToolkitSettings.SettingsChangedListener.TOPIC, object :
                UMLToolkitSettings.SettingsChangedListener {
                override fun onSettingsChange(settings: UMLToolkitSettings) {
                    logsJBScrollPane.isVisible = settings.showGitLogs
                }
            })

        //TODO: create cache file for old values
        urlField.text = "https://github.com/arnohaase/a-foundation.git"

        this.whenSizeChanged {
            urlField.preferredSize = (Dimension(this.width - getButton.width - 40, getButton.height))
            filesTreeJBScrollPane.preferredSize =
                Dimension(this.width - 20, (this.height * 2f / 3f).toInt() - 10)
            logsJBScrollPane.preferredSize = Dimension(
                this.width - 140,
                (this.height * 1f / 6f).toInt()
            )
        }

        getButton.addActionListener {
            clickGet(urlField, filesTreeJBScrollPane)
        }

        this.add(urlField)
        this.add(getButton)
        this.add(filesTreeJBScrollPane)
        addLogPanelButtons(this, filesTreeJBScrollPane)
        this.add(logsJBScrollPane)
        addModelControlPanel(this)
    }

    private fun addLogPanelButtons(mainJPanel: JPanel, filesTreeJBScrollPane: JBScrollPane) {
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
            } catch (e: Exception) {
                println(e.toString())
            }
        }

        getUmlFileButton.addActionListener {
            val descriptor = FileChooserDescriptor(
                true, false,
                false, false, false, false
            );
            descriptor.setTitle("Get UML-model");
            val toSelect = if (modelCache == null || modelCache.isEmpty()) null else LocalFileSystem.getInstance()
                .findFileByPath(modelCache)
            val virtualFile = FileChooser.chooseFile(descriptor, null, toSelect)

            if (virtualFile != null) {
                jTextArea.append("Get model from file: ${virtualFile.path}\n")
                model = handler.loadModelFromFile(virtualFile.path)

                Platform.runLater {
                    FXCityPanel.city.clear()
                    model?.toCity(FXCityPanel.city)
                    FXCityPanel.city.updateView()
                }
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
            } else {
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

    private fun buildTree(directories: Array<String>?, rootTree: DefaultMutableTreeNode, rootPath: String) {
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
}