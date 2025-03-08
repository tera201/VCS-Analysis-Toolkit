package org.tera201.vcstoolkit.tabs.git

import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.ui.ColoredTreeCellRenderer
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
import org.tera201.vcstoolkit.tabs.FXCircleTab
import org.tera201.vcstoolkit.tabs.TabEnum
import org.tera201.vcstoolkit.tabs.TabManager
import java.io.File
import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import kotlin.concurrent.thread

class GitTabController(
    private val gitTabUI: GitTabUI,
    private val cache: VCSToolkitCache,
    private val settings: VCSToolkitSettings,
    private val tabManager: TabManager
) {

    val buildModel = BuildModel()
    var myRepo: SCMRepository? = null
    var isClearingSelection = false
    private val dateBaseURL: String = "${settings.modelPath}/model.db"
    val dataBaseUtil:DataBaseUtil
    var models = ArrayList<Int>()
    var modelsIdMap = HashMap<String, Int>()
    val pathJTree = Tree()
    private val listeners = GitTabListeners(gitTabUI, cache, settings, this)
    val menuItemDeleteFromDB = JMenuItem("Delete From DB")
    var analyzing = false


    init {
        initializeUI()
        dataBaseUtil = DataBaseUtil(dateBaseURL)
        listeners.setupListeners()
        createPopupMenu()
        setupTree()

        ApplicationManager.getApplication().messageBus.connect()
            .subscribe(VCSToolkitSettings.SettingsChangedListener.TOPIC, object :
                VCSToolkitSettings.SettingsChangedListener {
                override fun onSettingsChange(settings: VCSToolkitSettings) {
                    gitTabUI.logsJBScrollPane.isVisible = settings.showGitLogs
                    gitTabUI.clearLogButton.isVisible = settings.showGitLogs
                    GitTabUtils.createDirectoryIfNotExists(settings.repoPath)
                    GitTabUtils.createDirectoryIfNotExists(settings.modelPath)
                    if (settings.externalProjectMode == 1 &&  cache.projectPathMap[gitTabUI.projectComboBox.selectedItem]!!.isExternal) {
                        gitTabUI.branchListModel.clear()
                        gitTabUI.tagListModel.clear()
                    } else {
                        gitTabUI.projectComboBox.selectedItem = cache.lastProject
                    }
                    buildCircle()
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
            GitTabUtils.createExceptionNotification(e)
        }
        updatePathPanel()
    }

    fun analyzeAction() {

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

    fun getRepoByUrl(url: String) {
        val repoName = buildModel.getRepoNameByUrl(url)
        GitTabUtils.createDirectoryIfNotExists(settings.repoPath)
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
            GitTabUtils.createExceptionNotification(e)
        }
        cache.projectPathMap[repoName] = ProjectPath(false, directoryPath, directoryPath)
        gitTabUI.projectComboBox.selectedItem = repoName
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
        listeners.listenerForProjectComboBox()
        listeners.listenerForOpenProjectButton()
    }

    private fun externalWarningNotification() {
        GitTabUtils.createNotification(
            "warning", "VCS for external projects was disabled. " +
                    "Please use the built-in VCS in the IDE manually.", NotificationType.WARNING
        )
    }

    fun updatePathPanelAndGitLists(projectName: String, projectPath: String) {
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
                GitTabUtils.createNotification("Opps", "The project doesn't have git repo.", NotificationType.WARNING)
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

    private fun createPopupMenu() {
        gitTabUI.popupMenu.add(menuItemDeleteFromDB)
        listeners.modelListRightClickListener()
        listeners.popupMenuDeleteItemListener()
    }

    fun analyze(isGit: Boolean, name: String, projectPath: String) {
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
            GitTabUtils.createExceptionNotification(e)
        }
    }

    private fun buildCircle() {
        try {
            (tabManager.getTabMap()[TabEnum.CIRCLE] as FXCircleTab).renderByModel(models, dataBaseUtil)
        } catch (e:Exception) {
            GitTabUtils.createExceptionNotification(e)
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
        listeners.pathTreeListener()
    }

    fun onTreeNodeDoubleClicked(node: DefaultMutableTreeNode?) {
        val filePath = node?.userObject.toString()
        val virtualFile = LocalFileSystem.getInstance().findFileByPath(filePath)
        if (virtualFile != null)
            FileEditorManager.getInstance(tabManager.getCurrentProject()).openFile(virtualFile, true)
    }
}