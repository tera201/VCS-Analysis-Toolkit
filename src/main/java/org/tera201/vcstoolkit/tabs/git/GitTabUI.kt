package org.tera201.vcstoolkit.tabs.git

import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.JBPopupMenu
import com.intellij.ui.JBSplitter
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import org.tera201.vcstoolkit.helpers.SharedModel
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.*

class GitTabUI (val modelListContent: SharedModel) {
    val urlField = JBTextField().apply { toolTipText = "<html>Enter your repository path.<br>Example: https://github.com/dummy/project.git</html>" }
    val getButton = JButton("Get")
    val analyzeButton = JButton("Analyze")
    // TODO: problems with JBTextArea (IDEA freeze) when analyzing one branch
    val logsJTextArea = JTextArea().apply { this.isEditable = false }
    val clearLogButton = JButton("Clear log")
    val logsJBScrollPane = JBScrollPane(
        logsJTextArea,
        JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        JBScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
    )
    val branchListModel = DefaultListModel<String>()
    val tagListModel = DefaultListModel<String>()
    val branchList = JBList(branchListModel)
    val tagList = JBList(tagListModel)
    val branchPane = JBPanel<JBPanel<*>>(BorderLayout()).apply {
        this.add(JBLabel("Branches"), BorderLayout.NORTH)
        this.add(JBScrollPane(branchList), BorderLayout.CENTER)
    }
    val tagPane = JBPanel<JBPanel<*>>(BorderLayout()).apply {
        this.add(JBLabel("Tags"), BorderLayout.NORTH)
        this.add(JBScrollPane(tagList), BorderLayout.CENTER)
    }
    val modelList = JBList(modelListContent)
    val projectComboBox = ComboBox<String>()
    val openProjectButton = JButton("Open project")
    val vcSplitPane = JBSplitter(true, 0.5f).apply {
        this.firstComponent = branchPane
        this.secondComponent = tagPane
        this.dividerWidth = 1
    }
    val filesTreeJBScrollPane =
        JBScrollPane(JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JBScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED)
    val currentBranchOrTagLabel = JBLabel("Current")
    val filesTreePane = JBPanel<JBPanel<*>>(BorderLayout()).apply {
        this.add(currentBranchOrTagLabel, BorderLayout.NORTH)
        this.add(filesTreeJBScrollPane, BorderLayout.CENTER)
    }
    val showSplitPane = JBSplitter(false, 0.5f).apply {
        this.firstComponent = filesTreePane
        this.secondComponent = vcSplitPane
        this.dividerWidth = 1
    }
    val logModelSplitPane = JBSplitter(false, 0.5f).apply {
        this.firstComponent = logsJBScrollPane
        this.secondComponent = JBScrollPane(modelList)
        this.dividerWidth = 1
    }
    val analyzerProgressBar = JProgressBar().apply { isVisible = false }
    val logModelPane = JBPanel<JBPanel<*>>(BorderLayout()).apply {
        add(logModelSplitPane, BorderLayout.CENTER)
        add(analyzerProgressBar, BorderLayout.SOUTH)
    }
    val logButtonsPanel = JBPanel<JBPanel<*>>().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        add(clearLogButton)
    }
    val modelControlPanel = JBPanel<JBPanel<*>>().apply { add(analyzeButton) }
    val popupMenu = JBPopupMenu()

    fun createUI(panel: JPanel) {
        panel.addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent?) {
                e?.let {
                    val newWidth = it.component.width
                    val newHeight = it.component.height
                    urlField.preferredSize = Dimension(newWidth - getButton.width - 40, getButton.height)
                    showSplitPane.preferredSize = Dimension(newWidth - 20, (newHeight - 130) * 2 / 3)
                    logModelSplitPane.preferredSize = Dimension(newWidth - 140, (newHeight - 130) / 3)
                }
            }
        })
        panel.add(urlField)
        panel.add(getButton)
        panel.add(showSplitPane)
        panel.add(logButtonsPanel)
        panel.add(logModelPane)
        panel.add(modelControlPanel)
        panel.add(projectComboBox)
        panel.add(openProjectButton)
    }
}