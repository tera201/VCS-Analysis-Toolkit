package org.tera201.vcstoolkit

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import org.tera201.vcstoolkit.action.ActionManager
import org.tera201.vcstoolkit.tabs.*
import javax.swing.SwingUtilities


class VCSToolWindowFactoryKt : ToolWindowFactory {

    /**
     * Create the tool window content.
     *
     * @param project    current project
     * @param toolWindow current tool windowA
     */
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentFactory = ContentFactory.getInstance()
        SwingUtilities.invokeLater {
            val tabManager = TabManager(project)
            val jtp = tabManager.getJBTabbedPane()
            val actionManager = ActionManager(jtp, toolWindow, tabManager)

            jtp.addChangeListener {
                when (val selectedTab = jtp.getTitleAt(jtp.selectedIndex)) {
                    TabEnum.GRAPH.value -> actionManager.setToolBarForInfoPage()
                    in arrayOf(TabEnum.CITY.value, TabEnum.CIRCLE.value) -> {
                        if (actionManager.openedFxTabs[selectedTab] == null) actionManager.setToolBarWithExpand()
                        else actionManager.setToolBarWithCollapse()
                    }
                    else -> actionManager.setDefaultToolBar()
                }
            }

            ApplicationManager.getApplication().messageBus.connect()
                .subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, object : FileEditorManagerListener {
                    override fun fileClosed(source: FileEditorManager, file: VirtualFile) {
                        if (file.extension == null && (file.name == TabEnum.CITY.value || file.name == TabEnum.CIRCLE.value)) {
                            val fullScreenTabInfo = actionManager.openedFxTabs[file.name]
                            if (fullScreenTabInfo != null) {
                                val fxTab = jtp.getComponentAt(fullScreenTabInfo.index) as FXTab
                                fxTab.setJFXPanel(fullScreenTabInfo.jfxPanel)
                                actionManager.openedFxTabs.remove(file.name)
                            }
                        }
                    }
                })


            val content = contentFactory.createContent(jtp, "", false)
            toolWindow.contentManager.addContent(content)
        }
    }
}