package org.tera201.vcstoolkit.info

import com.formdev.flatlaf.FlatClientProperties
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import net.miginfocom.swing.MigLayout
import org.tera201.vcsmanager.db.entities.CommitEntity
import org.tera201.vcsmanager.scm.SCM
import org.tera201.vcstoolkit.services.FilterCache
import org.tera201.vcstoolkit.services.VCSToolkitCache
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
    private var vcsToolkitCache: VCSToolkitCache = VCSToolkitCache.getInstance(tabManager.getCurrentProject())
    private val project: String get() = vcsToolkitCache.lastProject
    private var filterCache: FilterCache = FilterCache.getInstance(tabManager.getCurrentProject())
    val commitFilters: MutableList<CommitFilterConfig> get() = filterCache.commitFilterCache.getOrPut(project){ mutableListOf<CommitFilterConfig>() }
    val groupDataList: MutableList<GroupData> get()= filterCache.groupDataCache.getOrPut(project){ mutableListOf<GroupData>() }
    val commits: MutableList<CommitEntity> = mutableListOf()

    // Filter components
    private val authorComboBox = ComboBox<String>().apply {
        addItem("All Authors")
    }

    private val branchComboBox = ComboBox<String>().apply {
        addItem("All Branches")
    }

    private val commitsComboBox = ComboBox<String>()

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
        return GroupTile(groupData).also {
            it.addEditButtonAction(this::onEditGroupClicked )
            it.addRemoveButtonAction( this::removeGroupTile )
            it.addDragHandler(groupTilesContainer)
            it.addMouseClickAction(this::onGroupTileClicked)
        }
    }

    fun open(scm: SCM) {
        this.scm = scm
        scm.developerInfo.keys.forEach(authorComboBox::addItem)
        scm.allBranchesName.forEach(branchComboBox::addItem)
        if (commitFilters.isEmpty()) {
            commitFilters.add(CommitFilterConfig("All Commits"))
        }
        groupDataList.forEach { addGroupTile(it) }
        commitFilters.forEach { commitsComboBox.addItem(it.name) }
        onFiltersChanged()
    }

    private fun filterCommits() {
        this.commits.clear()
        val draftCommits = scm!!.getCommitInfo(authorComboBox.selectedItem as String, branchComboBox.selectedItem as String)
        val selectedCommitFilter: CommitFilterConfig =
            (commitFilters.find { it.name == commitsComboBox.selectedItem } ?: {commitsComboBox.selectedItem = commitFilters[0].name; commitFilters[0]}) as CommitFilterConfig
        val filteredCommits = draftCommits.filter { commitEntity ->
            selectedCommitFilter.startDate?.let { start ->
                commitEntity.date >= start
            } ?: true
        }.filter { commitEntity ->
            selectedCommitFilter.endDate?.let { start ->
                commitEntity.date <= start
            } ?: true
        }
        val sortedFilteredCommits = if (selectedCommitFilter.isLastCommits) {
            filteredCommits.sortedByDescending { it.date }
        } else {
            filteredCommits.sortedBy { it.date }
        }
        val filteredCommitCount = if (selectedCommitFilter.commitAmount != null) {
            sortedFilteredCommits.take(selectedCommitFilter.commitAmount!!)
        } else sortedFilteredCommits
        this.commits.addAll(filteredCommitCount)
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

    fun removeGroupTile(tile: JPanel, existingData: GroupData) {
        groupTilesContainer.remove(tile)
        groupDataList.remove(existingData)
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
        val stableCommits = commits.filter { it.stability > 0.2 }
        showCommitDetailsDialog(stableCommits, "Stable Commits")
    }

    private fun onUnstableCommitsClicked() {
        val unstableCommits = commits.filter { it.stability <= 0.2 }
        showCommitDetailsDialog(unstableCommits, "Unstable Commits")
    }

    private fun onAllCommitsClicked() {
        showCommitDetailsDialog(commits, "All Matching Commits")
    }

    private fun onFilterConfigClicked() {
        val dialog = FilterConfigDialog(panel, commitFilters)
        if (dialog.showAndGet()) {
            commitsComboBox.removeAllItems()
            commitFilters.forEach { commitsComboBox.addItem(it.name) }
        }
    }

    private fun onAddGroupClicked() {
        val dialog = AddGroupDialog(panel, editMode = false)
        if (dialog.showAndGet()) {
            val result = dialog.getGroupData()
            if (result.commitMessageRegex != null && scm != null) {
                val hashes = scm!!.getCommitsByMessageRegex(result.commitMessageRegex!!)
                result.count = hashes.size.toString()
            }
            groupDataList.add(result)
            addGroupTile(result)
        }
    }

    private fun onEditGroupClicked(tile: JPanel, existingData: GroupData) {
        val dialog = AddGroupDialog(panel, editMode = true, existingData = existingData)
        if (dialog.showAndGet()) {
            val updatedData = dialog.getGroupData()
            if (updatedData.commitMessageRegex != null && scm != null) {
                groupDataList.remove(existingData)
                val hashes = scm!!.getCommitsByMessageRegex(updatedData.commitMessageRegex!!)
                updatedData.count = hashes.size.toString()
                groupDataList.add(updatedData)
            }
            updateGroupTile(tile, updatedData)
        }
    }

    private fun onFiltersChanged() {
        filterCommits()

        updateAllCommitsCount(commits.size)
        commits.map { it.stability }.filter { it > 0.2 }.size.let {
            updateStableCommitsCount(it)
        }
        commits.map { it.stability }.filter { it <= 0.2 }.size.let {
            updateUnstableCommitsCount(it)
        }
    }

    private fun onGroupTileClicked(groupName: String, groupBy: String) {
        println("Group tile clicked: $groupName (grouped by: $groupBy)")
    }

    private fun showCommitDetailsDialog(commits: List<CommitEntity>, title: String) {
        val dialog = CustomCommitDetailsDialog(tabManager.project, commits, title, scm!!)
        dialog.show()
    }
}