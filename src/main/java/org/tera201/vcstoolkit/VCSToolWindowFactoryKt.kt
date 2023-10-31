package org.tera201.vcstoolkit

import org.tera201.vcstoolkit.action.ShowSettingsAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.content.ContentFactory
import org.tera201.vcstoolkit.tabs.FXCirclePanel
import org.tera201.vcstoolkit.tabs.FXCityPanel
import org.tera201.vcstoolkit.tabs.FXGraphPanel
import org.tera201.vcstoolkit.tabs.GitPanel
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.awt.Dimension


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
        val jtp = JBTabbedPane()
        jtp.autoscrolls = true
        jtp.add("FX Graph", FXGraphPanel())
        jtp.add("FX City", FXCityPanel())
        jtp.add("FX Circle", FXCirclePanel())
        jtp.add("Git", GitPanel())
        jtp.preferredSize = Dimension(500, 400)
        val content = contentFactory.createContent(jtp, "", false)
        toolWindow.contentManager.addContent(content)
        toolWindow.setTitleActions(listOf(ShowSettingsAction()))
    }
}