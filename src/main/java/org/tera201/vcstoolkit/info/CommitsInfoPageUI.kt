package org.tera201.vcstoolkit.info

import com.formdev.flatlaf.FlatClientProperties
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridLayoutManager
import net.miginfocom.swing.MigLayout
import org.tera201.vcstoolkit.tabs.TabManager
import java.awt.BorderLayout
import java.awt.Cursor
import java.awt.Dimension
import java.awt.Font
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.SwingConstants

class CommitsInfoPageUI(val tabManager: TabManager) {
    val panel = JBPanel<JBPanel<*>>().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
    }

    // Filter components
    private val authorComboBox = ComboBox<String>().apply {
        addItem("All Authors")
        // Add more authors dynamically
    }

    private val branchComboBox = ComboBox<String>().apply {
        addItem("All Branches")
        // Add more branches dynamically
    }

    private val commitsComboBox = ComboBox<String>().apply {
        addItem("All Commits")
        addItem("Last 10")
        addItem("Last 50")
        addItem("Last 100")
    }

    private val filterConfigButton = JButton("⚙").apply {
        toolTipText = "Configure Filters"
        preferredSize = Dimension(40, 30)
        cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
    }

    // Filter line panel
    private val filterLine = JBPanel<JBPanel<*>>().apply {
        layout = MigLayout("insets 10", "[grow][grow][grow][]", "[]")
        add(createFilterSection("Author:", authorComboBox), "grow")
        add(createFilterSection("Branch:", branchComboBox), "grow")
        add(createFilterSection("Commits:", commitsComboBox), "grow")
        add(filterConfigButton, "")
        border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
    }

    // Tiles for stable/unstable commits
    private val stableCommitsTile = createCommitTile("Stable Commits", "0", JBColor.GREEN.darker() as JBColor)
    private val unstableCommitsTile = createCommitTile("Unstable Commits", "0", JBColor.RED.darker() as JBColor)

    private val tilesPanel = JBPanel<JBPanel<*>>().apply {
        layout = MigLayout("insets 10, gap 15", "[grow][grow]", "[]")
        add(stableCommitsTile, "grow")
        add(unstableCommitsTile, "grow")
        border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
    }

    // All commits tile (full width)
    private val allCommitsTile = createAllCommitsTile("All Matching Commits", "0")

    private val allCommitsPanel = JBPanel<JBPanel<*>>().apply {
        layout = MigLayout("insets 10", "[grow]", "[]")
        add(allCommitsTile, "grow")
        border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
    }

    // Grouping section
    private val addGroupButton = JButton("+ Add Group").apply {
        cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
    }

    private val groupingPanel = JBPanel<JBPanel<*>>().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        border = BorderFactory.createTitledBorder("Group Commits By")
        add(Box.createVerticalStrut(10))
        add(createGroupButtonPanel())
    }

    init {
        panel.apply {
            add(filterLine)
            add(Box.createVerticalStrut(10))
            add(tilesPanel)
            add(Box.createVerticalStrut(10))
            add(allCommitsPanel)
            add(Box.createVerticalStrut(15))
            add(groupingPanel)
            add(Box.createVerticalGlue())
        }

        // Setup listeners
        setupListeners()
    }

    private fun createFilterSection(label: String, comboBox: ComboBox<String>): JPanel {
        return JBPanel<JBPanel<*>>().apply {
            layout = MigLayout("insets 0", "[]5[]", "[]")
            add(JBLabel(label).apply {
                font = font.deriveFont(Font.PLAIN, 12f)
            })
            add(comboBox, "grow")
        }
    }

    private fun createCommitTile(title: String, count: String, accentColor: JBColor): JPanel {
        return JBPanel<JBPanel<*>>().apply {
            layout = BorderLayout(10, 10)
            border = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(JBColor.border(), 1, true),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
            )
            cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)

            putClientProperty(FlatClientProperties.STYLE, "arc:12")

            val titleLabel = JBLabel(title).apply {
                font = font.deriveFont(Font.BOLD, 14f)
                foreground = JBColor.foreground()
                horizontalAlignment = SwingConstants.CENTER
            }

            val countLabel = JBLabel(count).apply {
                font = font.deriveFont(Font.BOLD, 32f)
                foreground = accentColor
                horizontalAlignment = SwingConstants.CENTER
            }

            val infoLabel = JBLabel("Click to view details").apply {
                font = font.deriveFont(Font.ITALIC, 11f)
                foreground = JBColor.GRAY
                horizontalAlignment = SwingConstants.CENTER
            }

            val contentPanel = JBPanel<JBPanel<*>>().apply {
                layout = BoxLayout(this, BoxLayout.Y_AXIS)
                isOpaque = false
                add(titleLabel)
                add(Box.createVerticalStrut(10))
                add(countLabel)
                add(Box.createVerticalStrut(5))
                add(infoLabel)
            }

            add(contentPanel, BorderLayout.CENTER)

            // Store count label for updates
            putClientProperty("countLabel", countLabel)
        }
    }

    private fun createAllCommitsTile(title: String, count: String): JPanel {
        return JBPanel<JBPanel<*>>().apply {
            layout = MigLayout("insets 20", "[grow]", "[]10[]5[]")
            border = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(JBColor.border(), 1, true),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
            )
            cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)

            putClientProperty(FlatClientProperties.STYLE, "arc:12")

            val titleLabel = JBLabel(title).apply {
                font = font.deriveFont(Font.BOLD, 16f)
                foreground = JBColor.foreground()
            }

            val countLabel = JBLabel(count).apply {
                font = font.deriveFont(Font.BOLD, 40f)
                foreground = JBColor.BLUE.darker()
            }

            val descLabel = JBLabel("commits matching current filters").apply {
                font = font.deriveFont(Font.PLAIN, 12f)
                foreground = JBColor.GRAY
            }

            add(titleLabel, "wrap")
            add(countLabel, "wrap")
            add(descLabel, "")

            // Store count label for updates
            putClientProperty("countLabel", countLabel)
        }
    }

    private fun createGroupButtonPanel(): JPanel {
        return JBPanel<JBPanel<*>>().apply {
            layout = MigLayout("insets 10", "[]", "[]")
            add(addGroupButton, "align center")
            border = BorderFactory.createEmptyBorder(5, 5, 15, 5)
        }
    }

    private fun createGroupTile(groupName: String, groupBy: String, count: String): JPanel {
        return JBPanel<JBPanel<*>>().apply {
            layout = MigLayout("insets 15", "[grow][]", "[]5[]")
            border = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(JBColor.border(), 1, true),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
            )
            cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)

            putClientProperty(FlatClientProperties.STYLE, "arc:8")

            val titleLabel = JBLabel(groupName).apply {
                font = font.deriveFont(Font.BOLD, 13f)
            }

            val groupByLabel = JBLabel("Grouped by: $groupBy").apply {
                font = font.deriveFont(Font.PLAIN, 11f)
                foreground = JBColor.GRAY
            }

            val countLabel = JBLabel(count).apply {
                font = font.deriveFont(Font.BOLD, 24f)
                foreground = JBColor.BLUE
            }

            val removeButton = JButton("×").apply {
                toolTipText = "Remove group"
                preferredSize = Dimension(30, 30)
                cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                foreground = JBColor.RED
            }

            add(titleLabel, "wrap")
            add(groupByLabel, "span 2, wrap")
            add(countLabel, "")
            add(removeButton, "align right top")
        }
    }

    private fun setupListeners() {
        // Stable commits tile click
        stableCommitsTile.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                onStableCommitsClicked()
            }

            override fun mouseEntered(e: MouseEvent) {
                stableCommitsTile.border = BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(JBColor.GREEN.darker(), 2, true),
                    BorderFactory.createEmptyBorder(20, 20, 20, 20)
                )
            }

            override fun mouseExited(e: MouseEvent) {
                stableCommitsTile.border = BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(JBColor.border(), 1, true),
                    BorderFactory.createEmptyBorder(20, 20, 20, 20)
                )
            }
        })

        // Unstable commits tile click
        unstableCommitsTile.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                onUnstableCommitsClicked()
            }

            override fun mouseEntered(e: MouseEvent) {
                unstableCommitsTile.border = BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(JBColor.RED.darker(), 2, true),
                    BorderFactory.createEmptyBorder(20, 20, 20, 20)
                )
            }

            override fun mouseExited(e: MouseEvent) {
                unstableCommitsTile.border = BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(JBColor.border(), 1, true),
                    BorderFactory.createEmptyBorder(20, 20, 20, 20)
                )
            }
        })

        // All commits tile click
        allCommitsTile.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                onAllCommitsClicked()
            }

            override fun mouseEntered(e: MouseEvent) {
                allCommitsTile.border = BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(JBColor.BLUE.darker(), 2, true),
                    BorderFactory.createEmptyBorder(15, 20, 15, 20)
                )
            }

            override fun mouseExited(e: MouseEvent) {
                allCommitsTile.border = BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(JBColor.border(), 1, true),
                    BorderFactory.createEmptyBorder(15, 20, 15, 20)
                )
            }
        })

        // Filter config button
        filterConfigButton.addActionListener {
            onFilterConfigClicked()
        }

        // Add group button
        addGroupButton.addActionListener {
            onAddGroupClicked()
        }

        // Filter change listeners
        authorComboBox.addActionListener {
            onFiltersChanged()
        }

        branchComboBox.addActionListener {
            onFiltersChanged()
        }

        commitsComboBox.addActionListener {
            onFiltersChanged()
        }
    }

    // Public methods for updating data
    fun updateStableCommitsCount(count: Int) {
        val countLabel = stableCommitsTile.getClientProperty("countLabel") as? JBLabel
        countLabel?.text = count.toString()
    }

    fun updateUnstableCommitsCount(count: Int) {
        val countLabel = unstableCommitsTile.getClientProperty("countLabel") as? JBLabel
        countLabel?.text = count.toString()
    }

    fun updateAllCommitsCount(count: Int) {
        val countLabel = allCommitsTile.getClientProperty("countLabel") as? JBLabel
        countLabel?.text = count.toString()
    }

    fun addGroupTile(groupName: String, groupBy: String, count: String) {
        val groupTile = createGroupTile(groupName, groupBy, count)
        groupingPanel.add(groupTile, groupingPanel.componentCount - 1)
        groupingPanel.revalidate()
        groupingPanel.repaint()
    }

    // Callback methods (to be implemented)
    private fun onStableCommitsClicked() {
        // Handle stable commits tile click
        println("Stable commits clicked")
    }

    private fun onUnstableCommitsClicked() {
        // Handle unstable commits tile click
        println("Unstable commits clicked")
    }

    private fun onAllCommitsClicked() {
        // Handle all commits tile click
        println("All commits clicked")
    }

    private fun onFilterConfigClicked() {
        // Open filter configuration dialog
        println("Filter config clicked")
    }

    private fun onAddGroupClicked() {
        // Open dialog to add new grouping
        // Example: addGroupTile("Feature Commits", "commit message pattern", "15")
        println("Add group clicked")
    }

    private fun onFiltersChanged() {
        // Update data based on selected filters
        println("Filters changed")
    }
}