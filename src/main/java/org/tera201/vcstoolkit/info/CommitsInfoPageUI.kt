package org.tera201.vcstoolkit.info

import com.formdev.flatlaf.FlatClientProperties
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import net.miginfocom.swing.MigLayout
import org.tera201.vcsmanager.scm.SCM
import org.tera201.vcstoolkit.tabs.TabManager
import java.awt.BorderLayout
import java.awt.Cursor
import java.awt.Dimension
import java.awt.Font
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.SwingConstants

class CommitsInfoPageUI(val tabManager: TabManager) {
    var scm: SCM? = null
    val panel = JBPanel<JBPanel<*>>().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
    }

    // Filter components
    private val authorComboBox = ComboBox<String>().apply {
        addItem("All Authors")
    }

    private val branchComboBox = ComboBox<String>().apply {
        addItem("All Branches")
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
        alignmentX = java.awt.Component.LEFT_ALIGNMENT
        maximumSize = Dimension(Int.MAX_VALUE, preferredSize.height)
    }

    // Tiles for stable/unstable commits
    private val stableCommitsTile = createCommitTile("Stable Commits", "0", JBColor.GREEN.darker() as JBColor)
    private val unstableCommitsTile = createCommitTile("Unstable Commits", "0", JBColor.RED.darker() as JBColor)

    private val tilesPanel = JBPanel<JBPanel<*>>().apply {
        layout = MigLayout("insets 10, gap 15", "[grow][grow]", "[]")
        add(stableCommitsTile, "grow")
        add(unstableCommitsTile, "grow")
        border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
        alignmentX = java.awt.Component.LEFT_ALIGNMENT
        maximumSize = Dimension(Int.MAX_VALUE, preferredSize.height)
    }

    // All commits tile (full width)
    private val allCommitsTile = createAllCommitsTile("All Matching Commits", "0")

    private val allCommitsPanel = JBPanel<JBPanel<*>>().apply {
        layout = MigLayout("insets 10", "[grow]", "[]")
        add(allCommitsTile, "grow")
        border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
        alignmentX = java.awt.Component.LEFT_ALIGNMENT
        maximumSize = Dimension(Int.MAX_VALUE, preferredSize.height)
    }

    // Grouping section with tiles container
    private val groupTilesContainer = JBPanel<JBPanel<*>>().apply {
        layout = ResponsiveGridLayout()
        border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
    }

    private val addGroupButton = JButton("+ Add Group").apply {
        cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
    }

    private val groupingPanel = JBPanel<JBPanel<*>>().apply {
        layout = BorderLayout()
        border = BorderFactory.createTitledBorder("Group Commits By")
        alignmentX = java.awt.Component.LEFT_ALIGNMENT
        maximumSize = Dimension(Int.MAX_VALUE, Int.MAX_VALUE)

        // Create a scroll pane for tiles
        val scrollPane = JScrollPane(groupTilesContainer).apply {
            border = null
            horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
            verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        }

        add(scrollPane, BorderLayout.CENTER)
        add(createGroupButtonPanel(), BorderLayout.SOUTH)

        // Add component listener to trigger relayout when panel resizes
        addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent?) {
                groupTilesContainer.revalidate()
                groupTilesContainer.repaint()
            }
        })
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

    private fun createGroupTile(groupData: GroupData): JPanel {
        return JBPanel<JBPanel<*>>().apply {
            layout = BorderLayout(5, 5)
            border = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(JBColor.border(), 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
            )
            cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            preferredSize = Dimension(200, 120)
            minimumSize = Dimension(150, 100)

            putClientProperty(FlatClientProperties.STYLE, "arc:8")
            // Store the group data for editing
            putClientProperty("groupData", groupData)

            fun data() = this.getClientProperty("groupData") as? GroupData

            // Top section with title and buttons
            val topPanel = JBPanel<JBPanel<*>>().apply {
                layout = BorderLayout()
                isOpaque = false
            }. also {

                val titleLabel = JBLabel(groupData.name).apply {
                    font = font.deriveFont(Font.BOLD, 13f)
                }

                val buttonsPanel = JBPanel<JBPanel<*>>().apply {
                    layout = BoxLayout(this, BoxLayout.X_AXIS)
                    isOpaque = false
                }.also { panel ->

                    // Edit button
                    val editButton = JButton("✎").apply {
                        toolTipText = "Edit group"
                        preferredSize = Dimension(24, 24)
                        cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                        foreground = JBColor.BLUE
                        putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_BORDERLESS)

                    }.also {
                        it.addActionListener {
                            data()?.let { data -> onEditGroupClicked(this, data) }
                        }
                    }

                    // Remove button
                    val removeButton = JButton("×").apply {
                        toolTipText = "Remove group"
                        preferredSize = Dimension(24, 24)
                        cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                        foreground = JBColor.RED
                        putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_BORDERLESS)

                    }.also {
                        it.addActionListener {
                            removeGroupTile(this)
                        }
                    }

                    panel.add(editButton)
                    panel.add(Box.createHorizontalStrut(2))
                    panel.add(removeButton)
                }

                it.add(titleLabel, BorderLayout.CENTER)
                it.add(buttonsPanel, BorderLayout.EAST)
            }

            // Center section with count
            val countLabel = JBLabel(groupData.count).apply {
                font = font.deriveFont(Font.BOLD, 28f)
                foreground = JBColor.BLUE
                horizontalAlignment = SwingConstants.CENTER
            }

            // Bottom section with group by info
            val groupByLabel = JBLabel("Grouped by: ${groupData.getGroupByDescription()}").apply {
                font = font.deriveFont(Font.PLAIN, 10f)
                foreground = JBColor.GRAY
                horizontalAlignment = SwingConstants.CENTER
            }

            add(topPanel, BorderLayout.NORTH)
            add(countLabel, BorderLayout.CENTER)
            add(groupByLabel, BorderLayout.SOUTH)

            // Store count label for updates
            putClientProperty("countLabel", countLabel)
            putClientProperty("groupByLabel", groupByLabel)

            // Add hover effect
            addMouseListener(object : MouseAdapter() {
                override fun mouseEntered(e: MouseEvent) {
                    border = BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(JBColor.BLUE, 2, true),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                    )
                }

                override fun mouseExited(e: MouseEvent) {
                    border = BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(JBColor.border(), 1, true),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                    )
                }

                override fun mouseClicked(e: MouseEvent) {
                    data()?.let { data -> onGroupTileClicked(data.name, data.getGroupByDescription()) }
                }
            })
        }
    }

    fun open(scm: SCM) {
        this.scm = scm
        scm.developerInfo.keys.forEach(authorComboBox::addItem)
        scm.allBranchesMap.keys.forEach(branchComboBox::addItem)

        // Example: Add some test tiles with realistic filter descriptions
        addGroupTile(GroupData("Feature Commits", commitMessageRegex = "^feat:.*", count = "15"))
        addGroupTile(GroupData("Bug Fixes", commitMessageRegex = "^fix:.*", fileType = ".java", count = "8"))
        addGroupTile(GroupData("Documentation", fileType = ".md", filePath = "/docs", count = "5"))
        addGroupTile(GroupData("Refactoring", changesRegex = ".*refactor.*", count = "12"))
        addGroupTile(GroupData("Tests", filePath = "/test", fileType = ".java, .kt", count = "20"))
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

    fun addGroupTile(groupData: GroupData) {
        val groupTile = createGroupTile(groupData)
        groupTilesContainer.add(groupTile)
        groupTilesContainer.revalidate()
        groupTilesContainer.repaint()
    }

    fun removeGroupTile(tile: JPanel) {
        groupTilesContainer.remove(tile)
        groupTilesContainer.revalidate()
        groupTilesContainer.repaint()
    }

    fun clearGroupTiles() {
        groupTilesContainer.removeAll()
        groupTilesContainer.revalidate()
        groupTilesContainer.repaint()
    }

    private fun updateGroupTile(tile: JPanel, newData: GroupData) {
        // Update stored data
        tile.putClientProperty("groupData", newData)

        // Update title label
        val topPanel = tile.getComponent(0) as JPanel
        val titleLabel = (topPanel.getComponent(0) as JBLabel)
        titleLabel.text = newData.name

        // Update count label
        val countLabel = tile.getClientProperty("countLabel") as? JBLabel
        countLabel?.text = newData.count

        // Update group by description
        val groupByLabel = tile.getClientProperty("groupByLabel") as? JBLabel
        groupByLabel?.text = "Grouped by: ${newData.getGroupByDescription()}"

        tile.revalidate()
        tile.repaint()
    }

    // Callback methods (to be implemented)
    private fun onStableCommitsClicked() {
        println("Stable commits clicked")
    }

    private fun onUnstableCommitsClicked() {
        println("Unstable commits clicked")
    }

    private fun onAllCommitsClicked() {
        println("All commits clicked")
    }

    private fun onFilterConfigClicked() {
        FilterConfigDialog(panel).show()
    }

    private fun onAddGroupClicked() {
        val dialog = AddGroupDialog(panel, editMode = false)
        if (dialog.showAndGet()) {
            val result = dialog.getGroupData()
            addGroupTile(result)
        }
    }

    private fun onEditGroupClicked(tile: JPanel, existingData: GroupData) {
        val dialog = AddGroupDialog(panel, editMode = true, existingData = existingData)
        if (dialog.showAndGet()) {
            val updatedData = dialog.getGroupData()
            updateGroupTile(tile, updatedData)
        }
    }

    private fun onFiltersChanged() {
        println("Filters changed")
    }

    private fun onGroupTileClicked(groupName: String, groupBy: String) {
        println("Group tile clicked: $groupName (grouped by: $groupBy)")
    }
}