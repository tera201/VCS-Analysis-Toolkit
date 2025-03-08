package org.tera201.vcstoolkit.tabs

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.updateSettings.impl.pluginsAdvertisement.notificationGroup
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.components.JBList
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.PlatformIcons
import model.console.BuildModel
import org.repodriller.scm.SCMRepository
import org.tera201.code2uml.AnalyzerBuilder
import org.tera201.code2uml.Language
import org.tera201.code2uml.util.messages.DataBaseUtil
import org.tera201.vcstoolkit.helpers.ProjectPath
import org.tera201.vcstoolkit.services.VCSToolkitCache
import org.tera201.vcstoolkit.services.settings.VCSToolkitSettings
import java.awt.event.*
import java.io.File
import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import kotlin.concurrent.thread

class GitTabController(
    private val gitTabUI: GitTabUI,
    private val cache: VCSToolkitCache,
    private val settings: VCSToolkitSettings,
    private val tabManager: TabManager) {

    private val buildModel = BuildModel()
    var myRepo: SCMRepository? = null
    private var isClearingSelection = false
    private val dateBaseURL: String = "${settings.modelPath}/model.db"
    val dataBaseUtil:DataBaseUtil
    var models = ArrayList<Int>()
    var modelsIdMap = HashMap<String, Int>()
    val pathJTree = Tree()
    private var analyzing = false

    init {
        initializeUI()
        dataBaseUtil = DataBaseUtil(dateBaseURL)
        createPopupMenu()
        setupListeners()
        setupGitListListeners()
        setupListSelectionListeners()
    }

    private fun setupListeners() {
        gitTabUI.getButton.addActionListener { handleGetRepo() }
        listenerForAnalyzeButton()

        setupTree()
    }

    private fun handleGetRepo() {
        thread {
            //TODO: add regex
            val url = gitTabUI.urlField.text
            if (url.isNotEmpty()) {
                cache.urlField = url
                getRepoByUrl(url)
            }
        }
    }

    private fun setupGitListListeners() {
        val branchMouseEvent = mouseClickListenerForGitList(gitTabUI.branchList)
        val tagMouseEvent = mouseClickListenerForGitList(gitTabUI.tagList)
        gitTabUI.branchList.addMouseListener(branchMouseEvent)
        gitTabUI.tagList.addMouseListener(tagMouseEvent)
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
                        models.removeAt(i)
                    }
                }
            }
        })
    }

    private fun initializeUI() {
        gitTabUI.logsJBScrollPane.isVisible = settings.showGitLogs
        gitTabUI.clearLogButton.isVisible = settings.showGitLogs
        configureProjectPane()
    }

    private fun populateJBList(targetListModel: DefaultListModel<String>, stringList: List<String>) {
        targetListModel.clear()
        targetListModel.addAll(stringList)
    }


    private fun mouseClickListenerForGitList(jbList: JBList<String>):MouseAdapter {
        return object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                if (e!!.clickCount == 2 && !analyzing) {
                    val index = jbList.locationToIndex(e.point)
                    if (index >= 0) {
                        val selectedItem = jbList.model.getElementAt(index)
                        checkoutTo(selectedItem)
                    }
                } else if (analyzing) {
                    createNotification("Checkout freeze",
                        "You cannot checkout while the analyzer is running", NotificationType.WARNING)
                }
            }
        }
    }

    fun checkoutTo(item:String) {
        try {
            if (settings.externalProjectMode == 2)
                myRepo?.scm?.createCommit("VCSToolkit: save message")
            myRepo!!.scm.checkoutTo(item)
            if (myRepo?.scm?.currentBranchOrTagName != null)
                gitTabUI.currentBranchOrTagLabel.text = myRepo?.scm?.currentBranchOrTagName
            if (settings.externalProjectMode == 2)
                myRepo?.scm?.resetLastCommitsWithMessage("VCSToolkit: save message")
        } catch (e:Exception) {
            createExceptionNotification(e)
        }
//        val fileSystem = LocalFileSystem.getInstance()
//        val virtualFile: VirtualFile? = fileSystem.findFileByPath(myRepo!!.path)
//        val virtualFileGit: VirtualFile? = fileSystem.findFileByPath("${myRepo!!.path}/.git}")
//        virtualFile?.refresh(false, true)
//        virtualFileGit?.refresh(false, true)
        updatePathPanel()
    }

    private fun getRepoByUrl(url: String) {
        val repoName = buildModel.getRepoNameByUrl(url)
        createDirectoryIfNotExists(settings.repoPath)
        val directoryPath = "${settings.repoPath}/$repoName"
        val directory = File(directoryPath)
        try {
            if (directory.exists() && directory.isDirectory) {
                gitTabUI.logsJTextArea.append("Already exist!\n")
                myRepo = buildModel.getRepository(url, settings.repoPath, settings.modelPath)
                if ((0 until gitTabUI.projectComboBox.model.size).none { i -> gitTabUI.projectComboBox.model.getElementAt(i) == repoName }) {
                    gitTabUI.projectComboBox.addItem(repoName)
                }
            } else {
                gitTabUI.logsJTextArea.append("Cloning: ${url}\n")
                myRepo = if (settings.username.equals("")) buildModel.createClone(url, settings.repoPath, settings.modelPath)
                else buildModel.createClone(url, settings.repoPath, settings.username, settings.password, settings.modelPath)
                gitTabUI.logsJTextArea.append("Cloned to ${myRepo!!.path}\n")
                gitTabUI.projectComboBox.addItem(repoName)
            }
        } catch (e: Exception) {
            createExceptionNotification(e)
        }
        cache.projectPathMap[repoName] = ProjectPath(false, directoryPath, directoryPath)
        gitTabUI.projectComboBox.selectedItem = repoName
    }

    private fun createDirectoryIfNotExists(path: String) {
        val directory = File(path)
        if (!directory.exists()) {
            directory.mkdirs()
        }
    }

    private fun createExceptionNotification(e:Exception) {
        val content = "${e.message}"
        System.err.println("message: ${e.message}\nstackTrace: ${e.stackTrace.joinToString("\n")}")
        createNotification(e.javaClass.simpleName, content, NotificationType.ERROR)
    }

    private fun configureProjectPane() {
        cache.projectPathMap["Current project"] = ProjectPath(true, tabManager.project.basePath.toString(),
            "${settings.repoPath}/${tabManager.project.name}")
        if (cache.projectPathMap.isNotEmpty()) {
            cache.projectPathMap.keys.forEach {
                gitTabUI.projectComboBox.addItem(it)
            }
            if (cache.lastProject != "") {
                gitTabUI.projectComboBox.selectedItem = cache.lastProject
                cache.projectPathMap[cache.lastProject]?.let { updatePathPanelAndGitLists(cache.lastProject, it.path) }
            }
        }

        File(settings.repoPath).list { dir, name -> File(dir, name).isDirectory.and(name != "Models") }?.forEach {
            if ((0 until gitTabUI.projectComboBox.model.size).none { i -> gitTabUI.projectComboBox.model.getElementAt(i) == it }) {
                cache.projectPathMap[it] = ProjectPath(false, "${settings.repoPath}/$it",
                    "${settings.repoPath}/$it")
                gitTabUI.projectComboBox.addItem(it)
            }
        }
        listenerForProjectComboBox()
        listenerForOpenProjectButton()
    }

    private fun listenerForProjectComboBox() {
        gitTabUI.projectComboBox.addActionListener {
            val selectedProject = gitTabUI.projectComboBox.selectedItem as String
            val projectPath = cache.projectPathMap[selectedProject]
            val directory = File(projectPath?.path)
            if (directory.exists() && directory.isDirectory) {
                projectPath?.let { updatePathPanelAndGitLists(selectedProject, it.path) }
            } else {
                gitTabUI.projectComboBox.removeItem(selectedProject)
                cache.projectPathMap.remove(selectedProject)
                gitTabUI.projectComboBox.selectedItem = cache.lastProject
                createNotification("Oops", "The project with the specified path was not found.", NotificationType.ERROR)
            }
            cache.lastProject = selectedProject
        }
    }

    private fun listenerForOpenProjectButton() {
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

    private fun createNotification(title:String, message:String, notificationType: NotificationType) {
        val notification: Notification =
            notificationGroup.createNotification("VCS Analysis Toolkit - $title", message, notificationType)
        Notifications.Bus.notify(notification, null)
    }

    private fun externalWarningNotification() {
        createNotification("warning", "VCS for external projects was disabled. " +
                "Please use the built-in VCS in the IDE manually.", NotificationType.WARNING)
    }

    private fun updatePathPanelAndGitLists(projectName: String, projectPath: String) {
        thread {
            val isRepo = File("$projectPath/.git").exists()
            if (myRepo?.repoName != projectName && isRepo) myRepo = buildModel.getRepository(projectPath, settings.modelPath)
            updatePathPanel()
            if (isRepo && !(cache.projectPathMap[projectName]!!.isExternal && settings.externalProjectMode == 1)) {
                populateJBList(gitTabUI.branchListModel, buildModel.getBranches(myRepo).filter { it != "HEAD" })
                populateJBList(gitTabUI.tagListModel, buildModel.getTags(myRepo))
                if (myRepo?.scm?.currentBranchOrTagName != null)
                    gitTabUI.currentBranchOrTagLabel.text = myRepo?.scm?.currentBranchOrTagName
            } else if (cache.projectPathMap[projectName]!!.isExternal && settings.externalProjectMode == 1) {
                gitTabUI.currentBranchOrTagLabel.text = getProjectNameOrCurrentBranchOrTag(projectPath, isRepo)
                gitTabUI.branchListModel.clear()
                gitTabUI.tagListModel.clear()
                externalWarningNotification()
            } else {
                createNotification("Opps", "The project doesn't have git repo.", NotificationType.WARNING)
                gitTabUI.currentBranchOrTagLabel.text = getProjectNameOrCurrentBranchOrTag(projectPath, isRepo)
                gitTabUI.branchListModel.clear()
                gitTabUI.tagListModel.clear()
            }
        }
    }

    private fun getProjectNameOrCurrentBranchOrTag(projectPath: String, isRepo:Boolean):String {
        val splitPath = projectPath.split("/")
        val lastDirectory = splitPath[splitPath.size - 1]
        var name = lastDirectory
        if (isRepo) {
            myRepo = buildModel.getRepository(projectPath, settings.modelPath)
            name = myRepo!!.scm.currentBranchOrTagName
        }
        return name
    }

    fun JBList<String>.addClearSelectorByAnotherList(anotherList: JBList<String>) {
        this.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                if ((e != null) &&!(e.isMetaDown || e.isShiftDown)
                    && (!isClearingSelection && anotherList.selectedValue != null)) {
                    isClearingSelection = true
                    anotherList.clearSelection()
                    isClearingSelection = false
                }
                val selected = this@addClearSelectorByAnotherList.selectedValuesList.size + anotherList.selectedValuesList.size
                gitTabUI.analyzeButton.text = if (selected == 1) "Analyze" else "AnalyzeAll"
            }
        })
    }

    private fun createPopupMenu() {
        val menuItemDeleteFromDB = JMenuItem("Delete From DB")
        gitTabUI.popupMenu.add(menuItemDeleteFromDB)
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

        menuItemDeleteFromDB.addActionListener { e: ActionEvent? ->
            val selectedIndex: Int = gitTabUI.modelList.getSelectedIndex()
            val modelId = modelsIdMap[gitTabUI.modelListContent.getElementAt(selectedIndex)]
            if (selectedIndex != -1) {
                gitTabUI.modelList.remove(selectedIndex)
                gitTabUI.modelListContent.removeAt(selectedIndex)
                if (modelId != null) {
                    dataBaseUtil.deleteModel(modelId)
                }
            }
        }
        menuItemDeleteFromDB.addActionListener { e: ActionEvent? ->

        }
    }

    private fun listenerForAnalyzeButton() {
        gitTabUI.analyzeButton.addActionListener {
            thread {
                if (cache.projectPathMap[cache.lastProject] != null) {
                    val projectPath = cache.projectPathMap[cache.lastProject]?.path
                    analyzing = true
                    gitTabUI.analyzerProgressBar.isVisible = true
                    val startTime = System.currentTimeMillis()
                    val allList = gitTabUI.branchList.selectedValuesList + gitTabUI.tagList.selectedValuesList
                    models.clear()
                    modelsIdMap.clear()
                    gitTabUI.modelListContent.clear()
                    gitTabUI.logsJTextArea.append("Start analyzing.\n")
                    allList.forEach { analyze(true, it, myRepo!!.path) }
                    if (allList.isEmpty() && projectPath != null) analyze(false, gitTabUI.currentBranchOrTagLabel.text, projectPath)
                    val executionTime = (System.currentTimeMillis() - startTime) / 1000.0
                    gitTabUI.logsJTextArea.append("End analyzing. Execution time: $executionTime sec.\n")
                    analyzing = false
                    gitTabUI.analyzerProgressBar.isVisible = false
                    gitTabUI.modelListContent.addAll(modelsIdMap.keys)
                    buildCircle()
                } else gitTabUI.logsJTextArea.append("Get some repo or project for analyzing.\n")
            }
        }
    }

    private fun analyze(isGit: Boolean, name: String, projectPath: String) {
        gitTabUI.logsJTextArea.append("\t*modeling: ${gitTabUI.currentBranchOrTagLabel.text}\n")
        gitTabUI.logsJTextArea.caret.dot = gitTabUI.logsJTextArea.text.length
        val selectedProject = if (gitTabUI.projectComboBox.selectedItem == "Current project")  tabManager.project.name else gitTabUI.projectComboBox.selectedItem as String
        try {
            if (isGit) checkoutTo(name)
            val analyzerBuilder =
                AnalyzerBuilder(Language.Java, selectedProject, name, projectPath, dateBaseURL)
                    .textArea(gitTabUI.logsJTextArea).progressBar(gitTabUI.analyzerProgressBar).threads(4)
            val modelId = analyzerBuilder.buildDB(dataBaseUtil)
            models.add(modelId)
            modelsIdMap.put(name, modelId)
        } catch (e:Exception) {
            createExceptionNotification(e)
        }
    }

    private fun buildCircle() {
        try {
            (tabManager.getTabMap()[TabEnum.CIRCLE] as FXCircleTab).renderByModel(models, dataBaseUtil)
        } catch (e:Exception) {
            createExceptionNotification(e)
        }
    }

    private fun updatePathPanel() {
        val root = DefaultMutableTreeNode(cache.projectPathMap[cache.lastProject]?.path)
        buildTree(File(cache.projectPathMap[cache.lastProject]?.path), root)
        pathJTree.model = DefaultTreeModel(root)
        SwingUtilities.invokeLater {
            gitTabUI.filesTreeJBScrollPane.setViewportView(pathJTree)
            gitTabUI.filesTreeJBScrollPane.updateUI()
        }
    }

    private fun buildTree(file: File, rootTree: DefaultMutableTreeNode) {
        file.list { dir, name -> File(dir, name).isDirectory }?.forEach {
            val path = "${file.absolutePath}/$it"
            val node = DefaultMutableTreeNode(path)
            rootTree.add(node)
            buildTree(File(path), node)
        }
        file.list { dir, name -> File(dir, name).isFile }?.forEach {
            val node = DefaultMutableTreeNode("${file.absolutePath}/$it")
            rootTree.add(node)
        }
    }

    private fun setupTree() {
        pathJTree.cellRenderer = object : ColoredTreeCellRenderer() {
            override fun customizeCellRenderer(
                tree: JTree,
                value: Any?,
                selected: Boolean,
                expanded: Boolean,
                leaf: Boolean,
                row: Int,
                hasFocus: Boolean
            ) {
                val node = value as? DefaultMutableTreeNode ?: return
                val file = File(node.userObject.toString())
                icon = if (file.isDirectory) PlatformIcons.FOLDER_ICON
                else FileTypeManager.getInstance().getFileTypeByFileName(file.name).icon
                append(file.name)
            }
        }

        pathJTree.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                if (e!!.clickCount == 2) {
                    val selRow = pathJTree.getRowForLocation(e.x, e.y)
                    if (selRow != -1) {
                        val selPath = pathJTree.getPathForLocation(e.x, e.y)
                        val selectedNode = selPath?.lastPathComponent as? DefaultMutableTreeNode
                        onTreeNodeDoubleClicked(selectedNode)
                    }
                }
            }
        })
    }

    private fun onTreeNodeDoubleClicked(node: DefaultMutableTreeNode?) {
        val filePath = node?.userObject.toString()
        val virtualFile = LocalFileSystem.getInstance().findFileByPath(filePath)
        if (virtualFile != null)
            FileEditorManager.getInstance(tabManager.getCurrentProject()).openFile(virtualFile, true)
    }
}