package org.tera201.vcstoolkit.tabs.git

import com.formdev.flatlaf.FlatClientProperties
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.JBPopupMenu
import com.intellij.ui.JBColor
import com.intellij.ui.JBSplitter
import com.intellij.ui.components.*
import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridLayoutManager
import com.intellij.util.ui.JBUI
import org.tera201.vcstoolkit.helpers.SharedModel
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Font
import javax.swing.*

class GitTabUI(val modelListContent: SharedModel) {
    // Repository URL Section
    val urlField = JBTextField().apply {
        toolTipText = "<html>Enter your repository path.<br>Example: https://github.com/dummy/project.git</html>"
        putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter repository URL...")
    }
    val getButton = JButton("Clone").apply {
        putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_BORDERLESS)
    }
    
    private val urlPanel = createTilePanel("Repository").apply {
        layout = GridLayoutManager(1, 2)
        add(urlField, GridConstraints().apply {
            row = 0; column = 0
            fill = GridConstraints.FILL_HORIZONTAL
            hSizePolicy = GridConstraints.SIZEPOLICY_CAN_GROW or GridConstraints.SIZEPOLICY_WANT_GROW
        })
        add(getButton, GridConstraints().apply {
            row = 0; column = 1
            fill = GridConstraints.FILL_NONE
            hSizePolicy = GridConstraints.SIZEPOLICY_FIXED
        })
    }
    
    // Project Selection Section
    val projectComboBox = ComboBox<String>()
    val openProjectButton = JButton("Open...").apply {
        putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_BORDERLESS)
    }
    
    private val projectPanel = createTilePanel("Project").apply {
        layout = GridLayoutManager(1, 2)
        add(projectComboBox, GridConstraints().apply {
            row = 0; column = 0
            fill = GridConstraints.FILL_HORIZONTAL
            hSizePolicy = GridConstraints.SIZEPOLICY_CAN_GROW or GridConstraints.SIZEPOLICY_WANT_GROW
        })
        add(openProjectButton, GridConstraints().apply {
            row = 0; column = 1
            fill = GridConstraints.FILL_NONE
            hSizePolicy = GridConstraints.SIZEPOLICY_FIXED
        })
    }
    
    // Files Tree Section
    val filesTreeJBScrollPane = JBScrollPane(
        JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        JBScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
    )
    val currentBranchOrTagLabel = JBLabel("Current").apply {
        font = font.deriveFont(Font.BOLD, 12f)
        foreground = JBColor.namedColor("Label.infoForeground", JBColor.GRAY)
    }
    
    private val filesTreeTile = createTilePanel("Project Files").apply {
        layout = BorderLayout(5, 5)
        add(currentBranchOrTagLabel, BorderLayout.NORTH)
        add(filesTreeJBScrollPane, BorderLayout.CENTER)
        minimumSize = Dimension(150, 200)
        preferredSize = Dimension(300, 300)
    }
    
    // Branches and Tags Section
    val branchListModel = DefaultListModel<String>()
    val tagListModel = DefaultListModel<String>()
    val branchList = JBList(branchListModel)
    val tagList = JBList(tagListModel)
    
    private val branchPane = JBPanel<JBPanel<*>>(BorderLayout()).apply {
        add(JBLabel("Branches").apply {
            font = font.deriveFont(Font.BOLD, 11f)
            border = JBUI.Borders.empty(5)
        }, BorderLayout.NORTH)
        add(JBScrollPane(branchList), BorderLayout.CENTER)
    }
    
    private val tagPane = JBPanel<JBPanel<*>>(BorderLayout()).apply {
        add(JBLabel("Tags").apply {
            font = font.deriveFont(Font.BOLD, 11f)
            border = JBUI.Borders.empty(5)
        }, BorderLayout.NORTH)
        add(JBScrollPane(tagList), BorderLayout.CENTER)
    }
    
    val vcSplitPane = JBSplitter(true, 0.5f).apply {
        firstComponent = branchPane
        secondComponent = tagPane
        dividerWidth = 1
    }
    
    private val vcTile = createTilePanel("Version Control").apply {
        layout = BorderLayout()
        add(vcSplitPane, BorderLayout.CENTER)
        minimumSize = Dimension(200, 300)
        preferredSize = Dimension(300, 400)
    }
    
    // Main top split
    val showSplitPane = JBSplitter(false, 0.6f).apply {
        firstComponent = filesTreeTile
        secondComponent = vcTile
        dividerWidth = 2
        minimumSize = Dimension(400, 200)
        preferredSize = Dimension(600, 300)
    }
    
    // Analysis Section
    val analyzeButton = JButton("Analyze").apply {
        putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_BORDERLESS)
    }
    
    val analyzerProgressBar = JProgressBar().apply {
        isVisible = false
        putClientProperty(FlatClientProperties.STYLE, "arc:4")
    }
    
    private val analysisControlPanel = JBPanel<JBPanel<*>>().apply {
        layout = BoxLayout(this, BoxLayout.X_AXIS)
        border = JBUI.Borders.empty(5)
        add(analyzeButton)
        add(Box.createHorizontalGlue())
    }
    
    // Logs Section
    val logsJTextArea = JTextArea().apply {
        isEditable = false
        font = Font("Monospaced", Font.PLAIN, 11)
        lineWrap = true
        wrapStyleWord = true
    }
    val clearLogButton = JButton("Clear").apply {
        putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_BORDERLESS)
    }
    
    val logsJBScrollPane = JBScrollPane(
        logsJTextArea,
        JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        JBScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
    )
    
    private val logsTile = createTilePanel("Analysis Logs").apply {
        layout = BorderLayout()
        val logHeader = JBPanel<JBPanel<*>>().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            border = JBUI.Borders.empty(0, 0, 5, 0)
            add(Box.createHorizontalGlue())
            add(clearLogButton)
        }
        add(logHeader, BorderLayout.NORTH)
        add(logsJBScrollPane, BorderLayout.CENTER)
        minimumSize = Dimension(200, 100)
        preferredSize = Dimension(400, 150)
    }
    
    // Models Section
    val modelList = JBList(modelListContent)
    
    private val modelsTile = createTilePanel("Analysis Models").apply {
        layout = BorderLayout()
        add(JBScrollPane(modelList), BorderLayout.CENTER)
        minimumSize = Dimension(150, 100)
        preferredSize = Dimension(200, 150)
    }
    
    val logModelSplitPane = JBSplitter(false, 0.6f).apply {
        firstComponent = logsTile
        secondComponent = modelsTile
        dividerWidth = 2
        minimumSize = Dimension(400, 100)
        preferredSize = Dimension(600, 150)
    }
    
    private val analysisPanel = JBPanel<JBPanel<*>>(BorderLayout()).apply {
        add(analysisControlPanel, BorderLayout.NORTH)
        add(logModelSplitPane, BorderLayout.CENTER)
        add(analyzerProgressBar, BorderLayout.SOUTH)
        minimumSize = Dimension(400, 150)
        preferredSize = Dimension(800, 200)
    }
    
    // Main content panel (will be wrapped in scroll pane)
    private val contentPanel = JBPanel<JBPanel<*>>(GridLayoutManager(4, 1)).apply {
        border = JBUI.Borders.empty(10)
        
        // Row 0: Repository and Project panels
        val topControlsPanel = JBPanel<JBPanel<*>>(GridLayoutManager(1, 2)).apply {
            add(urlPanel, GridConstraints().apply {
                row = 0; column = 0
                fill = GridConstraints.FILL_HORIZONTAL
                hSizePolicy = GridConstraints.SIZEPOLICY_CAN_GROW or GridConstraints.SIZEPOLICY_WANT_GROW
            })
            add(projectPanel, GridConstraints().apply {
                row = 0; column = 1
                fill = GridConstraints.FILL_HORIZONTAL
                hSizePolicy = GridConstraints.SIZEPOLICY_CAN_GROW
            })
        }
        add(topControlsPanel, GridConstraints().apply {
            row = 0; column = 0
            fill = GridConstraints.FILL_HORIZONTAL
            vSizePolicy = GridConstraints.SIZEPOLICY_FIXED
        })
        
        // Row 1: Files and VCS split
        add(showSplitPane, GridConstraints().apply {
            row = 1; column = 0
            fill = GridConstraints.FILL_BOTH
            vSizePolicy = GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW
        })
        
        // Row 2: Analysis panel
        add(analysisPanel, GridConstraints().apply {
            row = 2; column = 0
            fill = GridConstraints.FILL_BOTH
            vSizePolicy = GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW
        })
    }
    
    // Main scrollable panel
    val mainPanel = JBScrollPane(contentPanel).apply {
        border = null
        horizontalScrollBarPolicy = JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        verticalScrollBarPolicy = JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        minimumSize = Dimension(300, 200)
    }
    
    val popupMenu = JBPopupMenu()

    private fun createTilePanel(title: String): JBPanel<JBPanel<*>> {
        return JBPanel<JBPanel<*>>().apply {
            border = JBUI.Borders.compound(
                JBUI.Borders.empty(5),
                JBUI.Borders.compound(
                    BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(JBColor.border(), 1, true),
                        title,
                        0,
                        2,
                        Font(Font.SANS_SERIF, Font.BOLD, 12),
                        JBColor.namedColor("Label.infoForeground", JBColor.GRAY)
                    ),
                    JBUI.Borders.empty(10)
                )
            )
        }
    }

    fun createUI(panel: JPanel) {
        panel.layout = BorderLayout()
        panel.add(mainPanel, BorderLayout.CENTER)
    }
}