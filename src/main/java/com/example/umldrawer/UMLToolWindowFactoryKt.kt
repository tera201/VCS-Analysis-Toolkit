package com.example.umldrawer

import com.example.umldrawer.action.ShowSettingsAction
import com.example.umldrawer.tabs.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.content.ContentFactory
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.awt.Dimension


class UMLToolWindowFactoryKt : ToolWindowFactory {

    private val log: Logger = LogManager.getLogger(UMLToolWindowFactoryKt::class.java)

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