package org.tera201.vcstoolkit.tabs

import com.intellij.notification.Notification
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.JBSplitter
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.PlatformIcons
import model.console.BuildModel
import org.repodriller.scm.SCMRepository
import org.tera201.code2uml.AnalyzerBuilder
import org.tera201.code2uml.Language
import org.tera201.code2uml.util.messages.DataBaseUtil
import org.tera201.vcstoolkit.helpers.ProjectPath
import org.tera201.vcstoolkit.helpers.SharedModel
import org.tera201.vcstoolkit.services.VCSToolkitCache
import org.tera201.vcstoolkit.services.settings.VCSToolkitSettings
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.event.*
import java.io.File
import java.io.IOException
import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import kotlin.concurrent.thread

class GitTab(private val tabManager: TabManager, val modelListContent:SharedModel) : JPanel() {
    private var settings: VCSToolkitSettings = VCSToolkitSettings.getInstance()
    private var cache: VCSToolkitCache = VCSToolkitCache.getInstance(tabManager.getCurrentProject())
    var myRepo: SCMRepository? = null
    private val dateBaseURL: String = "${settings.modelPath}/model.db"
    val dataBaseUtil:DataBaseUtil
    private val getButton = JButton("Get")
    private val urlField = JTextField()
    private val analyzeButton = JButton("Analyze")
    private val buildModel = BuildModel()
    // TODO: problems with JBTextArea (IDEA freeze) when analyzing one branch
    private val logsJTextArea = JTextArea().apply {
        this.isEditable = false
    }
    private val clearLogButton = JButton("Clear log")
    val logsJBScrollPane = JBScrollPane(
        logsJTextArea,
        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
    )
    var models = ArrayList<Int>()
    var modelsIdMap = HashMap<String, Int>()

    private var isClearingSelection = false
    private val branchListModel = DefaultListModel<String>()
    private val tagListModel = DefaultListModel<String>()
    private val branchList = JBList(branchListModel)
    private val tagList = JBList(tagListModel)
    val modelList = JBList(modelListContent)
    private val branchListScrollPane = JBScrollPane(branchList)
    private val branchPane = JPanel().apply {
        val label = JLabel("Branches")
        this.layout = BorderLayout()
        this.add(label, BorderLayout.NORTH)
        this.add(branchListScrollPane, BorderLayout.CENTER)
    }
    private val tagListScrollPane = JBScrollPane(tagList)
    private val tagPane = JPanel().apply {
        val label = JLabel("Tags")
        this.layout = BorderLayout()
        this.add(label, BorderLayout.NORTH)
        this.add(tagListScrollPane, BorderLayout.CENTER)
    }
    private val modelListScrollPane = JBScrollPane(modelList)
    val pathJTree = Tree()
    private val projectComboBox = ComboBox<String>()
    private val openProjectButton = JButton("Open project")
    private val vcSplitPane = JBSplitter(true, 0.5f).apply {
        this.firstComponent = branchPane
        this.secondComponent = tagPane
        this.dividerWidth = 1
    }
    private val filesTreeJBScrollPane =
        JBScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED)
    private val currentBranchOrTagLabel = JLabel("Current")
    private val filesTreePane = JPanel().apply {
        this.layout = BorderLayout()
        this.add(currentBranchOrTagLabel, BorderLayout.NORTH)
        this.add(filesTreeJBScrollPane, BorderLayout.CENTER)
    }
    private val showSplitPane = JBSplitter(false, 0.5f).apply {
        this.firstComponent = filesTreePane
        this.secondComponent = vcSplitPane
        this.dividerWidth = 1
    }
    private val logModelSplitPane = JBSplitter(false, 0.5f).apply {
        this.firstComponent = logsJBScrollPane
        this.secondComponent = modelListScrollPane
    this.dividerWidth = 1
    }
    private val analyzerProgressBar = JProgressBar().apply { isVisible = false };
    private val logModelPane = JPanel().apply {
        layout = BorderLayout()
        add(logModelSplitPane, BorderLayout.CENTER);
        add(analyzerProgressBar, BorderLayout.SOUTH);
    }
    private var analyzing = false
    private val notificationGroup = NotificationGroupManager.getInstance().getNotificationGroup("VCSToolkitNotify")
    
    init {
        try {
            File(settings.repoPath).mkdirs()
            File(settings.modelPath).mkdirs()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        dataBaseUtil = DataBaseUtil(dateBaseURL)

        this.layout = FlowLayout(FlowLayout.LEFT)
        logsJBScrollPane.isVisible = settings.showGitLogs
        clearLogButton.isVisible = settings.showGitLogs

        ApplicationManager.getApplication().messageBus.connect()
            .subscribe(VCSToolkitSettings.SettingsChangedListener.TOPIC, object :
                VCSToolkitSettings.SettingsChangedListener {
                override fun onSettingsChange(settings: VCSToolkitSettings) {
                    logsJBScrollPane.isVisible = settings.showGitLogs
                    clearLogButton.isVisible = settings.showGitLogs
                    createDirectoryIfNotExists(settings.repoPath)
                    createDirectoryIfNotExists(settings.modelPath)
                    if (settings.externalProjectMode == 1 &&  cache.projectPathMap[projectComboBox.selectedItem]!!.isExternal) {
                        branchListModel.clear()
                        tagListModel.clear()
                    } else {
                        projectComboBox.selectedItem = cache.lastProject
                    }
                    buildCircle()
                }
            })

        urlField.text = cache.urlField
        urlField.toolTipText = "<html>Enter your repository path.<br>Example: https://github.com/dummy/project.git</html>"
        this.minimumSize = Dimension(0, 200)
        this.addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent?) {
                super.componentResized(e)
                val newWidth = e!!.component.width
                val newHeight = e!!.component.height
                urlField.preferredSize = (Dimension(newWidth - getButton.width - 40, getButton.height))
                showSplitPane.preferredSize =
                    Dimension(newWidth - 20, ((newHeight - 130 ) * 2f / 3f).toInt())
                logModelSplitPane.preferredSize = Dimension(
                    newWidth - 140,
                    ((newHeight - 130 ) * 1f / 3f).toInt()
                )

            }
        })

        getButton.addActionListener {
            thread {
                //TODO: add regex
                if (urlField.text != "") {
                    cache.urlField = urlField.text
                    getRepoByUrl(urlField.text)
                }
            }
        }
        setupTree()
        configureSplitPanes()
        this.add(urlField)
        this.add(getButton)
        this.add(showSplitPane)
        addLogPanelButtons(this)
        this.add(logModelPane)
        addModelControlPanel()
        addProjectPane()
        createPopupMenu()
    }

    private fun addProjectPane() {
        cache.projectPathMap["Current project"] = ProjectPath(true, tabManager.project.basePath.toString(),
            "${settings.repoPath}/${tabManager.project.name}")
        if (cache.projectPathMap.isNotEmpty()) {
            cache.projectPathMap.keys.forEach {
                projectComboBox.addItem(it)
            }
            if (cache.lastProject != "") {
                projectComboBox.selectedItem = cache.lastProject
                cache.projectPathMap[cache.lastProject]?.let { updatePathPanelAndGitLists(cache.lastProject, it.path) }
            }
        }

        File(settings.repoPath).list { dir, name -> File(dir, name).isDirectory.and(name != "Models") }?.forEach {
            if ((0 until projectComboBox.model.size).none { i -> projectComboBox.model.getElementAt(i) == it }) {
                cache.projectPathMap[it] = ProjectPath(false, "${settings.repoPath}/$it",
                    "${settings.repoPath}/$it")
                projectComboBox.addItem(it)
            }
        }
        listenerForProjectComboBox()
        listenerForOpenProjectButton()
        this.add(projectComboBox)
        this.add(openProjectButton)
    }

    private fun listenerForProjectComboBox() {
        projectComboBox.addActionListener {
            val selectedProject = projectComboBox.selectedItem as String
            val projectPath = cache.projectPathMap[selectedProject]
            val directory = File(projectPath?.path)
            if (directory.exists() && directory.isDirectory) {
                projectPath?.let { updatePathPanelAndGitLists(selectedProject, it.path) }
            } else {
                projectComboBox.removeItem(selectedProject)
                cache.projectPathMap.remove(selectedProject)
                projectComboBox.selectedItem = cache.lastProject
                createNotification("Oops", "The project with the specified path was not found.", NotificationType.ERROR)
            }
            cache.lastProject = selectedProject
        }
    }

    private fun listenerForOpenProjectButton() {
        openProjectButton.addActionListener {
            val descriptor = FileChooserDescriptor(false, true, false,
                false, false, false)
            val toSelect = if (settings.repoPath.isEmpty()) null else LocalFileSystem.getInstance()
                .findFileByPath(settings.repoPath)
            val selectedDirectory = FileChooser.chooseFile(descriptor, null, toSelect)
            if (selectedDirectory != null) {
                val newProjectName = selectedDirectory.name
                cache.projectPathMap[newProjectName] = ProjectPath(true, selectedDirectory.path,
                    "${settings.repoPath}/${selectedDirectory.name}")
                projectComboBox.addItem(newProjectName)
                projectComboBox.selectedItem = newProjectName
            }
        }
    }

    private fun createNotification(title:String, message:String, notificationType: NotificationType) {
        val notification: Notification =
            notificationGroup.createNotification("VCS Analysis Toolkit - $title", message, notificationType)
        Notifications.Bus.notify(notification, null)
    }

    private fun createExceptionNotification(e:Exception) {
        val content = "${e.message}"
        System.err.println("message: ${e.message}\nstackTrace: ${e.stackTrace.joinToString("\n")}")
        createNotification(e.javaClass.simpleName, content, NotificationType.ERROR)
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
                populateJBList(branchListModel, buildModel.getBranches(myRepo).filter { it != "HEAD" })
                populateJBList(tagListModel, buildModel.getTags(myRepo))
                if (myRepo?.scm?.currentBranchOrTagName != null)
                    currentBranchOrTagLabel.text = myRepo?.scm?.currentBranchOrTagName
            } else if (cache.projectPathMap[projectName]!!.isExternal && settings.externalProjectMode == 1) {
                currentBranchOrTagLabel.text = getProjectNameOrCurrentBranchOrTag(projectPath, isRepo)
                branchListModel.clear()
                tagListModel.clear()
                externalWarningNotification()
            } else {
                createNotification("Opps", "The project doesn't have git repo.", NotificationType.WARNING)
                currentBranchOrTagLabel.text = getProjectNameOrCurrentBranchOrTag(projectPath, isRepo)
                branchListModel.clear()
                tagListModel.clear()
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

    private fun configureSplitPanes() {
        setupListSelectionListeners()
        setupListDoubleClickAction()
    }

    private fun setupListSelectionListeners() {
        branchList.addClearSelectorByAnotherList(tagList)
        tagList.addClearSelectorByAnotherList(branchList)
        modelList.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent?) {
                if (e?.keyCode == KeyEvent.VK_BACK_SPACE) {
                    val selectedIndices = modelList.selectedIndices
                    for (i in selectedIndices.reversedArray()) {
                        modelListContent.removeAt(i)
                        models.removeAt(i)
                    }
                }
            }
        })
    }

    private fun createPopupMenu() {
        val popupMenu = JPopupMenu()
        val menuItemDeleteFromDB = JMenuItem("Delete From DB")
        popupMenu.add(menuItemDeleteFromDB)
        modelList.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    val index: Int = modelList.locationToIndex(e.point)
                    if (index != -1) {
                        modelList.setSelectedIndex(index)
                        popupMenu.show(modelList, e.x, e.y)
                    }
                }
            }
        })

        menuItemDeleteFromDB.addActionListener { e: ActionEvent? ->
            val selectedIndex: Int = modelList.getSelectedIndex()
            val modelId = modelsIdMap[modelListContent.getElementAt(selectedIndex)]
            if (selectedIndex != -1) {
                modelList.remove(selectedIndex)
                modelListContent.removeAt(selectedIndex)
                if (modelId != null) {
                    dataBaseUtil.deleteModel(modelId)
                }
            }
        }
        menuItemDeleteFromDB.addActionListener { e: ActionEvent? ->

        }
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
                analyzeButton.text = if (selected == 1) "Analyze" else "AnalyzeAll"
            }
        })
    }

    private fun setupListDoubleClickAction() {
        val branchMouseEvent = mouseClickListenerForGitList(branchList)
        val tagMouseEvent = mouseClickListenerForGitList(tagList)
        branchList.addMouseListener(branchMouseEvent)
        tagList.addMouseListener(tagMouseEvent)
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

    public fun checkoutTo(item:String) {
        try {
            if (settings.externalProjectMode == 2)
                myRepo?.scm?.createCommit("VCSToolkit: save message")
            myRepo!!.scm.checkoutTo(item)
            if (myRepo?.scm?.currentBranchOrTagName != null)
                currentBranchOrTagLabel.text = myRepo?.scm?.currentBranchOrTagName
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

    private fun populateJBList(targetListModel: DefaultListModel<String>, stringList: List<String>) {
        targetListModel.clear()
        targetListModel.addAll(stringList)
    }

    private fun addLogPanelButtons(mainJPanel: JPanel) {
        val logButtonsPanel = JPanel()
        logButtonsPanel.layout = BoxLayout(logButtonsPanel, BoxLayout.Y_AXIS)

        clearLogButton.addActionListener {
            logsJTextArea.text = null
        }
        logButtonsPanel.add(clearLogButton)
        mainJPanel.add(logButtonsPanel)
    }

    private fun addModelControlPanel() {
        val modelControlPanel = JPanel()
        listenerForAnalyzeButton()
        modelControlPanel.add(analyzeButton)
        this.add(modelControlPanel)
    }

    private fun listenerForAnalyzeButton() {
        analyzeButton.addActionListener {
            thread {
                if (cache.projectPathMap[cache.lastProject] != null) {
                    val projectPath = cache.projectPathMap[cache.lastProject]?.path
                    analyzing = true
                    analyzerProgressBar.isVisible = true
                    val startTime = System.currentTimeMillis()
                    val allList = branchList.selectedValuesList + tagList.selectedValuesList
                    models.clear()
                    modelsIdMap.clear()
                    modelListContent.clear()
                    logsJTextArea.append("Start analyzing.\n")
                    allList.forEach { analyze(true, it, myRepo!!.path) }
                    if (allList.isEmpty() && projectPath != null) analyze(false, currentBranchOrTagLabel.text, projectPath)
                    val executionTime = (System.currentTimeMillis() - startTime) / 1000.0
                    logsJTextArea.append("End analyzing. Execution time: $executionTime sec.\n")
                    analyzing = false
                    analyzerProgressBar.isVisible = false
                    modelListContent.addAll(modelsIdMap.keys)
                    buildCircle()
                } else logsJTextArea.append("Get some repo or project for analyzing.\n")
            }
        }
    }

    private fun buildCircle() {
        try {
            (tabManager.getTabMap()[TabEnum.CIRCLE] as FXCircleTab).renderByModel(models, dataBaseUtil)
        } catch (e:Exception) {
            createExceptionNotification(e)
        }
    }

    private fun analyze(isGit: Boolean, name: String, projectPath: String) {
        logsJTextArea.append("\t*modeling: ${currentBranchOrTagLabel.text}\n")
        logsJTextArea.caret.dot = logsJTextArea.text.length
        val selectedProject = if (projectComboBox.selectedItem == "Current project")  tabManager.project.name else projectComboBox.selectedItem as String
        try {
            if (isGit) checkoutTo(name)
            val analyzerBuilder =
                AnalyzerBuilder(Language.Java, selectedProject, name, projectPath, dateBaseURL)
                    .textArea(logsJTextArea).progressBar(analyzerProgressBar).threads(4)
            val modelId = analyzerBuilder.buildDB(dataBaseUtil)
            models.add(modelId)
            modelsIdMap.put(name, modelId)
        } catch (e:Exception) {
            createExceptionNotification(e)
        }
    }

    private fun createDirectoryIfNotExists(path: String) {
        val directory = File(path)
        if (!directory.exists()) {
            directory.mkdirs()
        }
    }

    private fun getRepoByUrl(url: String) {
        val repoName = buildModel.getRepoNameByUrl(url)
        createDirectoryIfNotExists(settings.repoPath)
        val directoryPath = "${settings.repoPath}/$repoName"
        val directory = File(directoryPath)
        try {
            if (directory.exists() && directory.isDirectory) {
                logsJTextArea.append("Already exist!\n")
                myRepo = buildModel.getRepository(url, settings.repoPath, settings.modelPath)
                if ((0 until projectComboBox.model.size).none { i -> projectComboBox.model.getElementAt(i) == repoName }) {
                    projectComboBox.addItem(repoName)
                }
            } else {
                logsJTextArea.append("Cloning: ${url}\n")
                myRepo = if (settings.username.equals("")) buildModel.createClone(url, settings.repoPath, settings.modelPath)
                else buildModel.createClone(url, settings.repoPath, settings.username, settings.password, settings.modelPath)
                logsJTextArea.append("Cloned to ${myRepo!!.path}\n")
                projectComboBox.addItem(repoName)
            }
        } catch (e: Exception) {
            createExceptionNotification(e)
        }
        cache.projectPathMap[repoName] = ProjectPath(false, directoryPath, directoryPath)
        projectComboBox.selectedItem = repoName
    }

    private fun updatePathPanel() {
//        println(cache.projectPathMap[cache.lastProject]?.path)
        val root = DefaultMutableTreeNode(cache.projectPathMap[cache.lastProject]?.path)
        buildTree(File(cache.projectPathMap[cache.lastProject]?.path), root)
        pathJTree.model = DefaultTreeModel(root)
        SwingUtilities.invokeLater {
            filesTreeJBScrollPane.setViewportView(pathJTree)
            filesTreeJBScrollPane.updateUI()
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