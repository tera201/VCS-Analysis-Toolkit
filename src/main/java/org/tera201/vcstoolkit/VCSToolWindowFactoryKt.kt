package org.tera201.vcstoolkit

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.content.ContentFactory
import io.ktor.util.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.tera201.vcstoolkit.action.FullScreenAction
import org.tera201.vcstoolkit.action.ShowSettingsAction
import org.tera201.vcstoolkit.tabs.*
import java.awt.Dimension
import javax.swing.SwingUtilities
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener


class VCSToolWindowFactoryKt : ToolWindowFactory {

    private val log: Logger = LogManager.getLogger(VCSToolWindowFactoryKt::class.java)

    /**
     * Create the tool window content.
     *
     * @param project    current project
     * @param toolWindow current tool windowA
     */
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentFactory = ContentFactory.getInstance()
        SwingUtilities.invokeLater {
            val jtp = JBTabbedPane()
            val circleTab = FXCircleTab()
            jtp.autoscrolls = true
            jtp.add(TabEnum.GRAPH.value, FXGraphTab())
            jtp.add(TabEnum.CITY.value, FXCityTab())
            jtp.add(TabEnum.CIRCLE.value,  circleTab)
            jtp.add(TabEnum.GIT.value, GitPanel(circleTab))
            jtp.preferredSize = Dimension(500, 400)

            val fullScreenAction = FullScreenAction(jtp)
            val showSettingsAction = ShowSettingsAction()

            val listButtonsFx = listOf(fullScreenAction, showSettingsAction)
            val listButtons = listOf(showSettingsAction)

            jtp.addChangeListener(object : ChangeListener {
                override fun stateChanged(e: ChangeEvent?) {
                    val selectedTab = jtp.getTitleAt(jtp.selectedIndex)
                    if (selectedTab == TabEnum.CITY.value || selectedTab == TabEnum.CIRCLE.value)
                        toolWindow.setTitleActions(listButtonsFx)
                    else
                        toolWindow.setTitleActions(listButtons)
                }

            })

            ApplicationManager.getApplication().messageBus.connect()
                .subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, object : FileEditorManagerListener {
                    override fun fileClosed(source: FileEditorManager, file: VirtualFile) {
                        if (file.extension == null && (file.name == TabEnum.CITY.value || file.name == TabEnum.CIRCLE.value)) {
                            val fullScreenTabInfo = fullScreenAction.openedFxTabs[file.name]
                        if (fullScreenTabInfo != null) {
                                val fxTab = jtp.getComponentAt(fullScreenTabInfo.index) as FXTab
                                fxTab.setJFXPanel(fullScreenTabInfo.jfxPanel)
                                fullScreenAction.openedFxTabs.remove(file.name)
                            }
                        }
                    }
                })


            val content = contentFactory.createContent(jtp, "", false)
            toolWindow.contentManager.addContent(content)
        }
    }
}