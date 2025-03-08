package org.tera201.vcstoolkit.tabs.git

import com.intellij.notification.NotificationType
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.ui.components.JBList
import org.tera201.vcstoolkit.helpers.ProjectPath
import org.tera201.vcstoolkit.services.VCSToolkitCache
import org.tera201.vcstoolkit.services.settings.VCSToolkitSettings
import java.awt.event.*
import java.io.File
import javax.swing.SwingUtilities
import javax.swing.tree.DefaultMutableTreeNode
import kotlin.concurrent.thread

class GitTabListeners(
    private val gitTabUI: GitTabUI,
    private val cache: VCSToolkitCache,
    private val settings: VCSToolkitSettings,
    private val controller: GitTabController
) {

    fun setupListeners() {
        gitTabUI.getButton.addActionListener { handleGetRepo() }
        clearButtonListener()
        listenerForAnalyzeButton()
        setupGitListListeners()
        setupListSelectionListeners()
    }

    private fun handleGetRepo() {
        thread {
            //TODO: add regex
            val url = gitTabUI.urlField.text
            if (url.isNotEmpty()) {
                cache.urlField = url
                controller.getRepoByUrl(url)
            }
        }
    }


    private fun clearButtonListener() {
        gitTabUI.clearLogButton.addActionListener {
            gitTabUI.logsJTextArea.text = null
        }
    }

    private fun listenerForAnalyzeButton() {
        gitTabUI.analyzeButton.addActionListener {
            thread {
                controller.analyzeAction()
            }
        }
    }

    private fun setupGitListListeners() {
        val branchMouseEvent = mouseClickListenerForGitList(gitTabUI.branchList)
        val tagMouseEvent = mouseClickListenerForGitList(gitTabUI.tagList)
        gitTabUI.branchList.addMouseListener(branchMouseEvent)
        gitTabUI.tagList.addMouseListener(tagMouseEvent)
    }


    private fun mouseClickListenerForGitList(jbList: JBList<String>): MouseAdapter {
        return object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                if (e!!.clickCount == 2 && !controller.analyzing) {
                    val index = jbList.locationToIndex(e.point)
                    if (index >= 0) {
                        val selectedItem = jbList.model.getElementAt(index)
                        controller.checkoutTo(selectedItem)
                    }
                } else if (controller.analyzing) {
                    GitTabUtils.createNotification(
                        "Checkout freeze",
                        "You cannot checkout while the analyzer is running", NotificationType.WARNING
                    )
                }
            }
        }
    }

    private fun setupListSelectionListeners() {
        gitTabUI.branchList.addClearSelectorByAnotherList(gitTabUI.tagList)
        gitTabUI.tagList.addClearSelectorByAnotherList(gitTabUI.branchList)
        gitTabUI.modelList.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent?) {
                if (e?.keyCode == KeyEvent.VK_BACK_SPACE) {
                    val selectedIndices = gitTabUI.modelList.selectedIndices
                    for (i in selectedIndices.reversedArray()) {
                        gitTabUI.modelListContent.removeAt(i)
                        controller.models.removeAt(i)
                    }
                }
            }
        })
    }

    fun JBList<String>.addClearSelectorByAnotherList(anotherList: JBList<String>) {
        this.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                if ((e != null) &&!(e.isMetaDown || e.isShiftDown)
                    && (!controller.isClearingSelection && anotherList.selectedValue != null)) {
                    controller.isClearingSelection = true
                    anotherList.clearSelection()
                    controller.isClearingSelection = false
                }
                val selected = this@addClearSelectorByAnotherList.selectedValuesList.size + anotherList.selectedValuesList.size
                gitTabUI.analyzeButton.text = if (selected == 1) "Analyze" else "AnalyzeAll"
            }
        })
    }

    fun listenerForProjectComboBox() {
        gitTabUI.projectComboBox.addActionListener {
            val selectedProject = gitTabUI.projectComboBox.selectedItem as String
            val projectPath = cache.projectPathMap[selectedProject]
            val directory = File(projectPath?.path)
            if (directory.exists() && directory.isDirectory) {
                projectPath?.let { controller.updatePathPanelAndGitLists(selectedProject, it.path) }
            } else {
                gitTabUI.projectComboBox.removeItem(selectedProject)
                cache.projectPathMap.remove(selectedProject)
                gitTabUI.projectComboBox.selectedItem = cache.lastProject
                GitTabUtils.createNotification(
                    "Oops",
                    "The project with the specified path was not found.",
                    NotificationType.ERROR
                )
            }
            cache.lastProject = selectedProject
        }
    }

    fun modelListRightClickListener() {
        gitTabUI.modelList.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    val index: Int = gitTabUI.modelList.locationToIndex(e.point)
                    if (index != -1) {
                        gitTabUI.modelList.setSelectedIndex(index)
                        gitTabUI.popupMenu.show(gitTabUI.modelList, e.x, e.y)
                    }
                }
            }
        })
    }

    fun popupMenuDeleteItemListener() {
        controller.menuItemDeleteFromDB.addActionListener { e: ActionEvent? ->
            val selectedIndex: Int = gitTabUI.modelList.getSelectedIndex()
            val modelId = controller.modelsIdMap[gitTabUI.modelListContent.getElementAt(selectedIndex)]
            if (selectedIndex != -1) {
                gitTabUI.modelList.remove(selectedIndex)
                gitTabUI.modelListContent.removeAt(selectedIndex)
                if (modelId != null) {
                    controller.dataBaseUtil.deleteModel(modelId)
                }
            }
        }
    }

    fun pathTreeListener() {
        controller.pathJTree.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                if (e!!.clickCount == 2) {
                    val selRow = controller.pathJTree.getRowForLocation(e.x, e.y)
                    if (selRow != -1) {
                        val selPath = controller.pathJTree.getPathForLocation(e.x, e.y)
                        val selectedNode = selPath?.lastPathComponent as? DefaultMutableTreeNode
                        controller.onTreeNodeDoubleClicked(selectedNode)
                    }
                }
            }
        })
    }

    fun listenerForOpenProjectButton() {
        gitTabUI.openProjectButton.addActionListener {
            val descriptor = FileChooserDescriptor(false, true, false,
                false, false, false)
            val toSelect = if (settings.repoPath.isEmpty()) null else LocalFileSystem.getInstance()
                .findFileByPath(settings.repoPath)
            val selectedDirectory = FileChooser.chooseFile(descriptor, null, toSelect)
            if (selectedDirectory != null) {
                val newProjectName = selectedDirectory.name
                cache.projectPathMap[newProjectName] = ProjectPath(true, selectedDirectory.path,
                    "${settings.repoPath}/${selectedDirectory.name}")
                gitTabUI.projectComboBox.addItem(newProjectName)
                gitTabUI.projectComboBox.selectedItem = newProjectName
            }
        }
    }
}