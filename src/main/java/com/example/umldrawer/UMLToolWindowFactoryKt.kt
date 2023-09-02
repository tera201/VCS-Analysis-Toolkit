package com.example.umldrawer

import com.example.umldrawer.tabs.createFXCity
import com.example.umldrawer.tabs.createFXGraph
import com.example.umldrawer.tabs.createGit
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBTabbedPane
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
        val component = toolWindow.component

        val jtp = JBTabbedPane()
        jtp.autoscrolls = true
        jtp.add("FX Graph", createFXGraph())
        jtp.add("FX City", createFXCity())
        jtp.add("Git", createGit())
        jtp.preferredSize = Dimension(500, 400)

        component.parent.add(jtp)
    }
}