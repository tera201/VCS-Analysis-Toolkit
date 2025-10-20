package org.tera201.vcstoolkit.info

import com.formdev.flatlaf.FlatClientProperties
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.JBColor
import com.intellij.ui.components.*
import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridLayoutManager
import com.intellij.util.ui.JBUI
import org.tera201.swing.chart.bar.HorizontalBarChart
import org.tera201.swing.chart.data.pie.DefaultPieDataset
import org.tera201.vcsmanager.db.entities.CommitSize
import org.tera201.vcsmanager.db.entities.DeveloperInfo
import org.tera201.vcstoolkit.helpers.addComponentPairRow
import org.tera201.vcstoolkit.panels.CommitPanelSplitter
import org.tera201.vcstoolkit.panels.TilePanel
import org.tera201.vcstoolkit.tabs.TabManager
import org.tera201.vcstoolkit.utils.DateUtils.Companion.getDayOfMouth
import org.tera201.vcstoolkit.utils.DateUtils.Companion.getDayOfWeek
import org.tera201.vcstoolkit.utils.DateUtils.Companion.getHourOfDay
import org.tera201.vcstoolkit.utils.DateUtils.Companion.getMonthOfYear
import org.tera201.vcstoolkit.utils.DateUtils.Companion.getStringDate
import org.tera201.vcstoolkit.utils.DateUtils.Companion.timestampToLocalDate
import java.awt.Color
import java.awt.Font
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import javax.swing.*

class AuthorInfoPageUI(val tabManager: TabManager) {
    val panel = JBPanel<JBPanel<*>>(GridLayoutManager(5, 1))
    
    // Header section
    private val authorNameLabel = JBLabel().apply {
        font = font.deriveFont(Font.BOLD, 16f)
        putClientProperty(FlatClientProperties.STYLE, "font:bold +2")
    }
    private val emailComboBox = ComboBox<String>()
    
    // Statistics section
    private val commitCountLabel = JBLabel()
    private val commitFrequencyLabel = JBLabel()
    private val avgCommitTimeLabel = JBLabel()
    private val createdBranchesLabel = JBLabel()
    private val ownerPercentageLabel = JBLabel()
    private val lastActivityLabel = JBLabel()
    
    // Create compact header panel without border
    private val headerPanel = JBPanel<JBPanel<*>>().apply {
        layout = GridLayoutManager(3, 2)
        border = JBUI.Borders.empty(10)
        add(createInfoLabel("Developer:").apply { 
            font = font.deriveFont(Font.BOLD, 13f)
        }, GridConstraints().apply { 
            row = 0; column = 0 
            anchor = GridConstraints.ANCHOR_WEST
        })
        add(authorNameLabel, GridConstraints().apply { 
            row = 0; column = 1 
            anchor = GridConstraints.ANCHOR_WEST
            fill = GridConstraints.FILL_HORIZONTAL
        })
        add(createInfoLabel("Email:"), GridConstraints().apply { 
            row = 1; column = 0 
            anchor = GridConstraints.ANCHOR_WEST
        })
        add(emailComboBox, GridConstraints().apply { 
            row = 1; column = 1 
            fill = GridConstraints.FILL_HORIZONTAL
        })
    }
    
    private val statisticsPanel = TilePanel("Statistics").apply {
        layout = GridLayoutManager(6, 2)
        var i = 0
        addComponentPairRow(i++, createInfoLabel("Total Commits:"), commitCountLabel)
        addComponentPairRow(i++, createInfoLabel("Commit Frequency:"), commitFrequencyLabel)
        addComponentPairRow(i++, createInfoLabel("Avg. Commit Time:"), avgCommitTimeLabel)
        addComponentPairRow(i++, createInfoLabel("Created Branches:"), createdBranchesLabel)
        addComponentPairRow(i++, createInfoLabel("Code Ownership:"), ownerPercentageLabel)
        addComponentPairRow(i, createInfoLabel("Last Activity:"), lastActivityLabel)
    }
    
    private val stableCommitPanel = JBPanel<JBPanel<*>>()
    private val stableCommitTile = TilePanel("Commit Stability").apply {
        layout = GridLayoutManager(1, 1)
        add(stableCommitPanel, GridConstraints().apply { 
            row = 0; column = 0
            fill = GridConstraints.FILL_BOTH
        })
    }
    
    // Reorganized top section: Statistics | Commit Stability (larger)
    private val topSection = JBPanel<JBPanel<*>>(GridLayoutManager(2, 5)).apply {
        border = JBUI.Borders.empty(5)
        // Header spans both columns
        add(headerPanel, GridConstraints().apply { 
            row = 0; column = 0
            colSpan = 5
            fill = GridConstraints.FILL_HORIZONTAL
        })
        // Statistics takes 1 column
        add(statisticsPanel, GridConstraints().apply { 
            row = 1; column = 0
            colSpan = 2
            fill = GridConstraints.FILL_BOTH
        })
        // Stability takes 1 column with more space
        add(stableCommitTile, GridConstraints().apply { 
            row = 1; column = 2
            colSpan = 3
            fill = GridConstraints.FILL_BOTH
        })
    }
    
    // Charts section
    private val filePiePanel = JBPanel<JBPanel<*>>()
    private val linesPiePanel = JBPanel<JBPanel<*>>()
    private val filePieTile = TilePanel("File Actions").apply {
        layout = GridLayoutManager(1, 1)
        add(filePiePanel, GridConstraints().apply { 
            row = 0; column = 0
            fill = GridConstraints.FILL_BOTH
        })
    }
    private val linesPieTile = TilePanel("Line Actions").apply {
        layout = GridLayoutManager(1, 1)
        add(linesPiePanel, GridConstraints().apply { 
            row = 0; column = 0
            fill = GridConstraints.FILL_BOTH
        })
    }
    
    private val chartsPanel = JBPanel<JBPanel<*>>(GridLayoutManager(1, 2)).apply {
        border = JBUI.Borders.empty(5)
        add(filePieTile, GridConstraints().apply { 
            row = 0; column = 0
            fill = GridConstraints.FILL_BOTH
        })
        add(linesPieTile, GridConstraints().apply { 
            row = 0; column = 1
            fill = GridConstraints.FILL_BOTH
        })
    }
    
    private val filePieChart = initPieChart(filePiePanel, "", false)
    private val linesPieChart = initPieChart(linesPiePanel, "", false)
    private val stableCommitChart = initPieChart(stableCommitPanel, "", false)
    
    // Commit history section
    private val commitPanelSplitter = CommitPanelSplitter()
    private val commitTile = TilePanel("Commit History").apply {
        layout = GridLayoutManager(1, 1)
        add(commitPanelSplitter, GridConstraints().apply { 
            row = 0; column = 0
            fill = GridConstraints.FILL_BOTH
        })
    }
    
    // Bar chart section
    private val barChartButtonGroup = ButtonGroup()
    private val timeButtonsPanel = JBPanel<JBPanel<*>>().apply { 
        border = JBUI.Borders.empty(5, 10)
        add(JBLabel("Group by: ").apply { 
            foreground = JBColor.GRAY
        }) 
    }
    private val timeButtons = mapOf(
        "Hours" to ::createBarDataByHours, 
        "Week Days" to ::createBarDataByDay,
        "Days of Month" to ::createBarDataByMonth, 
        "Months" to ::createBarDataByYear
    )
        .map { (label, dataFunction) -> JBRadioButton(label) to dataFunction }
        .onEach { (button, _) -> 
            barChartButtonGroup.add(button)
            timeButtonsPanel.add(button) 
        }
    
    private val commitBarChart = HorizontalBarChart().apply {
        setValuesFormat(DecimalFormat("#,##0"))
        barColor = JBColor(Color.decode("#3574F0"), Color.decode("#5A9CFF"))
        header = JBLabel("").apply { 
            font = font.deriveFont(Font.BOLD, 13f)
            putClientProperty(FlatClientProperties.STYLE, "font:bold +1")
            border = JBUI.Borders.empty(5, 10)
        }
    }
    
    private val barChartTile = TilePanel("Commit Activity Patterns").apply {
        layout = GridLayoutManager(2, 1)
        add(timeButtonsPanel, GridConstraints().apply { 
            row = 0; column = 0
            fill = GridConstraints.FILL_HORIZONTAL
        })
        add(commitBarChart, GridConstraints().apply { 
            row = 1; column = 0
            fill = GridConstraints.FILL_BOTH
        })
    }
    
    private var spinnerPanel = createSpinnerPanel()
    private var email: String? = null

    init {
        panel.apply {
            border = JBUI.Borders.empty(10)
            add(topSection, GridConstraints().apply { 
                row = 0; column = 0
                fill = GridConstraints.FILL_HORIZONTAL
            })
            add(chartsPanel, GridConstraints().apply { 
                row = 1; column = 0
                fill = GridConstraints.FILL_HORIZONTAL
            })
            add(commitTile, GridConstraints().apply { 
                row = 2; column = 0
                fill = GridConstraints.FILL_HORIZONTAL
            })
            add(barChartTile, GridConstraints().apply { 
                row = 3; column = 0
                fill = GridConstraints.FILL_HORIZONTAL
            })
            add(spinnerPanel, GridConstraints().apply { 
                row = 4; column = 0 
            })
        }
        setContentVisibility(false)
    }
    
    private fun createInfoLabel(text: String): JBLabel {
        return JBLabel(text).apply {
            foreground = JBColor.GRAY
        }
    }

    fun open(commitSizeMap: Map<String, CommitSize>, developerInfoMap: Map<String, DeveloperInfo>) {
        developerInfoMap.keys.forEach(emailComboBox::addItem)
        emailComboBox.addActionListener({
            openSelected(commitSizeMap, developerInfoMap)
        })
        openSelected(commitSizeMap, developerInfoMap)
    }

    private fun clear() {
        commitPanelSplitter.clear()
        timeButtons.forEach { (button, _) -> button.actionListeners.forEach { button.removeActionListener(it) } }
    }

    private fun openSelected(commitSizeMap: Map<String, CommitSize>, developerInfoMap: Map<String, DeveloperInfo>) {
        clear()
        this.email = emailComboBox.selectedItem!!.toString()
        val filteredCommitSize = commitSizeMap.filter { it.value.authorEmail == email }
        updateLabels(developerInfoMap, filteredCommitSize)
        updatePieCharts(filteredCommitSize, developerInfoMap[email]!!)
        commitPanelSplitter.updatePanel(filteredCommitSize)
        val dateCollection = filteredCommitSize.values.map { it.date }
        timeButtons.forEach { (button, dataFunction) ->
            button.apply { addActionListener { updateBarChart(dataFunction(dateCollection), "Commits by ${button.text}") } }
        }
        setContentVisibility(true)
        if (barChartButtonGroup.selection == null) barChartButtonGroup.elements.nextElement().doClick()
        else barChartButtonGroup.elements.toList().find { it.model == barChartButtonGroup.selection }?.doClick()
    }

    private fun setContentVisibility(showContent: Boolean) {
        topSection.isVisible = showContent
        chartsPanel.isVisible = showContent
        commitTile.isVisible = showContent
        barChartTile.isVisible = showContent
        spinnerPanel.isVisible = !showContent
    }

    private fun updateLabels(developerInfoMap: Map<String, DeveloperInfo>, commitSizeMap: Map<String, CommitSize>) {
        val developer = developerInfoMap[email]!!
        authorNameLabel.text = developer.name
        commitCountLabel.text = "${developer.commits.size} commits"
        createdBranchesLabel.text = "N/A"
        val lines = developerInfoMap.values.sumOf { it.actualLinesOwner.toDouble() }
        ownerPercentageLabel.text = String.format("%.2f%%", ((developer.actualLinesOwner) / lines) * 100)
        val commitDates = commitSizeMap.values.map { it.date }.sorted().toList()
        val differences: MutableList<Int> = ArrayList()
        for (i in 1..<commitDates.size) {
            differences.add(commitDates[i] - commitDates[i - 1])
        }
        val date = commitDates.maxOrNull()
        lastActivityLabel.text = if (date != null) getStringDate(date) else "No activity"
        val daysCount = commitDates.map { timestampToLocalDate(it) }.distinct().size.toDouble()
        commitFrequencyLabel.text = String.format("%.2f commits/day", commitDates.size / daysCount)
        avgCommitTimeLabel.text = if (differences.isNotEmpty()) {
            String.format("%.2f days", differences.sum() / (24 * 3600.0 * differences.size))
        } else {
            "N/A"
        }
    }

    private fun updatePieCharts(commitSizeMap: Map<String, CommitSize>, developerInfo: DeveloperInfo) {
        stableCommitChart.updateData(createPieDataStable(commitSizeMap))
        filePieChart.updateData(createPieDataFile(developerInfo))
        linesPieChart.updateData(createPieDataLines(developerInfo))
    }

    private fun createPieDataFile(developerInfo: DeveloperInfo): DefaultPieDataset<String> {
        val dataset = DefaultPieDataset<String>()
        dataset.addValue("Added", developerInfo.fileAdded)
        dataset.addValue("Deleted", developerInfo.fileDeleted)
        dataset.addValue("Modified", developerInfo.fileModified)
        return dataset
    }

    private fun createPieDataLines(developerInfo: DeveloperInfo): DefaultPieDataset<String> {
        val dataset = DefaultPieDataset<String>()
        dataset.addValue("Added", developerInfo.linesAdded)
        dataset.addValue("Deleted", developerInfo.linesDeleted)
        dataset.addValue("Modified", developerInfo.linesModified)
        return dataset
    }

    private fun createPieDataStable(commitSizeMap: Map<String, CommitSize>): DefaultPieDataset<String> {
        val stableCommitCount = AtomicReference(0)
        val unStableCommitSize = AtomicReference(0)
        commitSizeMap.values.forEach {
            if (it.stability <= 0.2) unStableCommitSize.getAndSet(unStableCommitSize.get() + 1)
            else stableCommitCount.getAndSet(stableCommitCount.get() + 1)
        }
        val dataset = DefaultPieDataset<String>()
        dataset.addValue("Stable", stableCommitCount.get())
        dataset.addValue("Unstable", unStableCommitSize.get())
        return dataset
    }

    private fun updateBarChart(dataset: DefaultPieDataset<String>, title: String) {
        (commitBarChart.header as JLabel).text = title
        commitBarChart.setDataset(dataset)
    }

    private fun <T> createBarData(
        dateCollection: Collection<Int>,
        mapper: (Int) -> T,
        strLabel: (T) -> String
    ): DefaultPieDataset<String> where T : Comparable<T> {
        val dataset = DefaultPieDataset<String>()
        val commitCountMap = mutableMapOf<T, Int>()
        dateCollection.forEach { commitCountMap[mapper(it)] = commitCountMap.getOrDefault(mapper(it), 0) + 1 }
        commitCountMap.entries.sortedBy { it.key }.forEach { dataset.addValue(strLabel(it.key), it.value) }
        return dataset
    }

    private fun createBarDataByDay(dateCollection: Collection<Int>) =
        createBarData(dateCollection, ::getDayOfWeek) { it.toString() }

    private fun createBarDataByYear(dateCollection: Collection<Int>) =
        createBarData(dateCollection, ::getMonthOfYear) { it.toString() }

    private fun createBarDataByHours(dateCollection: Collection<Int>) =
        createBarData(dateCollection, ::getHourOfDay) { "$it:00" }

    private fun createBarDataByMonth(dateCollection: Collection<Int>) =
        createBarData(dateCollection, ::getDayOfMouth) { it.toString() }
}