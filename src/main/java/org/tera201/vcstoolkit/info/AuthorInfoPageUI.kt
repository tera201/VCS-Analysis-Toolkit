package org.tera201.vcstoolkit.info

import com.formdev.flatlaf.FlatClientProperties
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.*
import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridLayoutManager
import org.repodriller.scm.entities.CommitSize
import org.repodriller.scm.entities.DeveloperInfo
import org.tera201.swing.chart.bar.HorizontalBarChart
import org.tera201.swing.chart.data.pie.DefaultPieDataset
import org.tera201.vcstoolkit.helpers.addComponentPairRow
import org.tera201.vcstoolkit.panels.CommitPanelSplitter
import org.tera201.vcstoolkit.tabs.TabManager
import org.tera201.vcstoolkit.utils.DateUtils.Companion.getDayOfMouth
import org.tera201.vcstoolkit.utils.DateUtils.Companion.getDayOfWeek
import org.tera201.vcstoolkit.utils.DateUtils.Companion.getHourOfDay
import org.tera201.vcstoolkit.utils.DateUtils.Companion.getMonthOfYear
import org.tera201.vcstoolkit.utils.DateUtils.Companion.getStringDate
import org.tera201.vcstoolkit.utils.DateUtils.Companion.timestampToLocalDate
import java.awt.Color
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import javax.swing.*

class AuthorInfoPageUI(val tabManager: TabManager) {
    val panel = JBPanel<JBPanel<*>>(GridLayoutManager(5, 1))
    private val authorNameLabel = JBLabel()
    private val emailComboBox = ComboBox<String>()
    private val commitCountLabel = JBLabel()
    private val commitFrequencyLabel = JBLabel()
    private val avgCommitTimeLabel = JBLabel()
    private val createdBranchesLabel = JBLabel()
    private val ownerPercentageLabel = JBLabel()
    private val lastActivityLabel = JBLabel()
    private val labelPanel = JBPanel<JBPanel<*>>(GridLayoutManager(7, 2)).apply {
        var i = 0
        addComponentPairRow(i++, JBLabel("Email"), emailComboBox)
        addComponentPairRow(i++, JBLabel("Commit count"), commitCountLabel)
        addComponentPairRow(i++, JBLabel("Commit frequency"), commitFrequencyLabel)
        addComponentPairRow(i++, JBLabel("Avg commit time"), avgCommitTimeLabel)
        addComponentPairRow(i++, JBLabel("Created branches"), createdBranchesLabel)
        addComponentPairRow(i++, JBLabel("Owner, %"), ownerPercentageLabel)
        addComponentPairRow(i, JBLabel("Last activity"), lastActivityLabel)
    }
    private val stableCommitPanel = JBPanel<JBPanel<*>>()
    private val mainInfoPanel = JBPanel<JBPanel<*>>(GridLayoutManager(2, 2)).apply {
        add(authorNameLabel, GridConstraints().apply { row = 0; column = 0 })
        add(labelPanel, GridConstraints().apply { row = 1; column = 0; anchor = GridConstraints.ANCHOR_NORTHWEST })
        add(stableCommitPanel, GridConstraints().apply { row = 1; column = 1 })
    }
    private val filePiePanel = JBPanel<JBPanel<*>>()
    private val linesPiePanel = JBPanel<JBPanel<*>>()
    private val piePanels = JBPanel<JBPanel<*>>(GridLayoutManager(1, 2)).apply {
        add(filePiePanel, GridConstraints().apply { row = 0; column = 0 })
        add(linesPiePanel, GridConstraints().apply { row = 0; column = 1 })
    }
    private val filePieChart = initPieChart(filePiePanel, "File actions", false)
    private val linesPieChart = initPieChart(linesPiePanel, "Line actions", false)
    private val stableCommitChart = initPieChart(stableCommitPanel, "Stable commits", false)
    private val commitPanelSplitter = CommitPanelSplitter()
    private val commitScrollPanel = JBScrollPane().apply { setViewportView(commitPanelSplitter) }
    private val barChartButtonGroup = ButtonGroup()
    private val timeButtonsPanel = JBPanel<JBPanel<*>>().apply { add(JBLabel("BarChart by:")) }
    private val timeButtons = mapOf(
        "Hours" to ::createBarDataByHours, "Week" to ::createBarDataByDay,
        "Month" to ::createBarDataByMonth, "Year" to ::createBarDataByYear
    )
        .map { (label, dataFunction) -> JBRadioButton(label) to dataFunction }
        .onEach { (button, _) -> barChartButtonGroup.add(button); timeButtonsPanel.add(button) }
    private val commitBarChart = HorizontalBarChart().apply {
        setValuesFormat(DecimalFormat("#,##0"))
        barColor = Color.decode("#f97316")
        header = JBLabel("").apply { putClientProperty(FlatClientProperties.STYLE, ("font:+1;border:0,0,5,0")) }
    }
    private val barChartPanel = JBPanel<JBPanel<*>>(GridLayoutManager(2, 1)).apply {
        add(timeButtonsPanel, GridConstraints().apply { row = 0; column = 0 })
        add(commitBarChart, GridConstraints().apply { row = 1; column = 0; fill = GridConstraints.FILL_HORIZONTAL })
    }
    private var spinnerPanel = createSpinnerPanel()
    private var email: String? = null

    init {
        panel.apply {
            add(mainInfoPanel, GridConstraints().apply { row = 0; column = 0 })
            add(piePanels, GridConstraints().apply { row = 1; column = 0; fill = GridConstraints.FILL_HORIZONTAL })
            add(
                commitScrollPanel,
                GridConstraints().apply { row = 2; column = 0; fill = GridConstraints.FILL_HORIZONTAL })
            add(barChartPanel, GridConstraints().apply { row = 3; column = 0; fill = GridConstraints.FILL_HORIZONTAL })
            add(spinnerPanel, GridConstraints().apply { row = 4; column = 0 })
        }
        setContentVisibility(false)
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
            button.apply { addActionListener { updateBarChart(dataFunction(dateCollection), "Commit by $text") } }
        }
        setContentVisibility(true)
        if (barChartButtonGroup.selection == null) barChartButtonGroup.elements.nextElement().doClick()
        else barChartButtonGroup.elements.toList().find { it.model == barChartButtonGroup.selection }?.doClick()
    }

    private fun setContentVisibility(showContent: Boolean) {
        mainInfoPanel.isVisible = showContent
        commitScrollPanel.isVisible = showContent
        piePanels.isVisible = showContent
        barChartPanel.isVisible = showContent
        spinnerPanel.isVisible = !showContent

    }

    private fun updateLabels(developerInfoMap: Map<String, DeveloperInfo>, commitSizeMap: Map<String, CommitSize>) {
        val developer = developerInfoMap[email]!!
        authorNameLabel.text = developer.name
        commitCountLabel.text = developer.commits.size.toString()
        createdBranchesLabel.text = ""
        val lines = developerInfoMap.values.sumOf { it.getActualLinesOwner().toDouble() }
        ownerPercentageLabel.text = String.format("%.2f", ((developer.getActualLinesOwner()) / lines) * 100)
        val commitDates = commitSizeMap.values.map { it.date }.sorted().toList()
        val differences: MutableList<Int> = ArrayList()
        for (i in 1..<commitDates.size) {
            differences.add(commitDates[i] - commitDates[i - 1])
        }
        val date = commitDates.maxOrNull()
        lastActivityLabel.text = if (date != null) getStringDate(date) else ""
        val daysCount = commitDates.map { timestampToLocalDate(it) }.size.toDouble()
        commitFrequencyLabel.text = String.format("%.2f  per day", differences.size / daysCount)
        avgCommitTimeLabel.text = String.format("%.2f  day", differences.sum() / (24 * 3600.0 * differences.size))
    }

    private fun updatePieCharts(commitSizeMap: Map<String, CommitSize>, developerInfo: DeveloperInfo) {
        stableCommitChart.updateData(createPieDataStable(commitSizeMap))
        filePieChart.updateData(createPieDataFile(developerInfo))
        linesPieChart.updateData(createPieDataLines(developerInfo))
    }

    private fun createPieDataFile(developerInfo: DeveloperInfo): DefaultPieDataset<String> {
        val dataset = DefaultPieDataset<String>()
        dataset.addValue("Files added", developerInfo.fileAdded)
        dataset.addValue("Files deleted", developerInfo.fileDeleted)
        dataset.addValue("Files modified", developerInfo.fileModified)
        return dataset
    }

    private fun createPieDataLines(developerInfo: DeveloperInfo): DefaultPieDataset<String> {
        val dataset = DefaultPieDataset<String>()
        dataset.addValue("Lines added", developerInfo.linesAdded)
        dataset.addValue("Lines deleted", developerInfo.linesDeleted)
        dataset.addValue("Lines modified", developerInfo.linesModified)
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
        dataset.addValue("stable commit count", stableCommitCount.get())
        dataset.addValue("unstable commit count", unStableCommitSize.get())
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