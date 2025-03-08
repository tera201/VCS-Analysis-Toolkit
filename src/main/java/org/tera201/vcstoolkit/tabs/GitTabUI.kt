package org.tera201.vcstoolkit.tabs

import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.JBSplitter
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import org.tera201.vcstoolkit.helpers.SharedModel
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.*

class GitTabUI (val modelListContent: SharedModel) {
    val urlField = JTextField().apply { toolTipText = "<html>Enter your repository path.<br>Example: https://github.com/dummy/project.git</html>" }
    val getButton = JButton("Get")
    val analyzeButton = JButton("Analyze")
    // TODO: problems with JBTextArea (IDEA freeze) when analyzing one branch
    val logsJTextArea = JTextArea().apply {
        this.isEditable = false
    }
    val clearLogButton = JButton("Clear log")
    val logsJBScrollPane = JBScrollPane(
        logsJTextArea,
        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
    )
    val branchListModel = DefaultListModel<String>()
    val tagListModel = DefaultListModel<String>()
    val branchList = JBList(branchListModel)
    val tagList = JBList(tagListModel)
    val branchListScrollPane = JBScrollPane(branchList)
    val branchPane = JPanel().apply {
        val label = JLabel("Branches")
        this.layout = BorderLayout()
        this.add(label, BorderLayout.NORTH)
        this.add(branchListScrollPane, BorderLayout.CENTER)
    }
    val tagListScrollPane = JBScrollPane(tagList)
    val tagPane = JPanel().apply {
        val label = JLabel("Tags")
        this.layout = BorderLayout()
        this.add(label, BorderLayout.NORTH)
        this.add(tagListScrollPane, BorderLayout.CENTER)
    }
    val modelList = JBList(modelListContent)
    val modelListScrollPane = JBScrollPane(modelList)
    val projectComboBox = ComboBox<String>()
    val openProjectButton = JButton("Open project")
    val vcSplitPane = JBSplitter(true, 0.5f).apply {
        this.firstComponent = branchPane
        this.secondComponent = tagPane
        this.dividerWidth = 1
    }
    val filesTreeJBScrollPane =
        JBScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED)
    val currentBranchOrTagLabel = JLabel("Current")
    val filesTreePane = JPanel().apply {
        this.layout = BorderLayout()
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
        this.secondComponent = modelListScrollPane
        this.dividerWidth = 1
    }
    val analyzerProgressBar = JProgressBar().apply { isVisible = false }
    val logModelPane = JPanel().apply {
        layout = BorderLayout()
        add(logModelSplitPane, BorderLayout.CENTER)
        add(analyzerProgressBar, BorderLayout.SOUTH)
    }
    val popupMenu = JPopupMenu()

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
        addLogPanelButtons(panel)
        panel.add(logModelPane)
        addModelControlPanel(panel)
        panel.add(projectComboBox)
        panel.add(openProjectButton)
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

    private fun addModelControlPanel(mainJPanel: JPanel) {
        val modelControlPanel = JPanel()
        modelControlPanel.add(analyzeButton)
        mainJPanel.add(modelControlPanel)
    }
}