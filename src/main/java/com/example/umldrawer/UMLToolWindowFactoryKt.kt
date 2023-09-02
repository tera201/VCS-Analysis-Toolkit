package com.example.umldrawer

import com.example.umldrawer.tabs.createFXCity
import com.example.umldrawer.tabs.createFXGraph
import com.example.umldrawer.tabs.createGit
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBTabbedPane
import model.console.BuildModel
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JPanel


class UMLToolWindowFactoryKt : ToolWindowFactory {

    private val log: Logger = LogManager.getLogger(UMLToolWindowFactoryKt::class.java)

    /**
     * Create the tool window content.
     *
     * @param project    current project
     * @param toolWindow current tool windowA
     */
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindowContent = JPanel()
        myToolWindowContent.autoscrolls = true
        val component = toolWindow.component

        val jtp = JBTabbedPane()
        jtp.autoscrolls = true
        jtp.add("FX Graph", createFXGraph())
        jtp.add("FX City", createFXCity())
        jtp.add("Git", createGit())
        jtp.preferredSize = Dimension(500, 400)


        val cppParserRunner = CppParserRunner()
        val cppFiles = cppParserRunner.collectFiles("C:\\Users\\rnaryshkin\\IdeaProjects\\cpp2uml/CppToUMLSamples/src")
        val model = cppParserRunner.buildModel("CppParserRunnerSampleModel", cppFiles)

//        repo.scm.delete()

        myToolWindowContent.add(jtp, BorderLayout.CENTER)
        myToolWindowContent.isVisible = true
        component.parent.add(myToolWindowContent)
    }
}