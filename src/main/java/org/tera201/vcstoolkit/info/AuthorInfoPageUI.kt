package org.tera201.vcstoolkit.info

import com.formdev.flatlaf.FlatClientProperties
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.*
import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridLayoutManager
import net.miginfocom.swing.MigLayout
import org.repodriller.scm.entities.CommitSize
import org.repodriller.scm.entities.DeveloperInfo
import org.tera201.swing.chart.bar.HorizontalBarChart
import org.tera201.swing.chart.data.pie.DefaultPieDataset
import org.tera201.swing.chart.pie.PieChart
import org.tera201.swing.spinner.SpinnerProgress
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
import java.time.DayOfWeek
import java.time.Month
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import java.util.stream.IntStream
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
    private val filePieChart = PieChart().apply { initPieChart(this, filePiePanel, "File actions") }
    private val linesPieChart = PieChart().apply { initPieChart(this, linesPiePanel, "Line actions") }
    private val stableCommitChart = PieChart().apply { initPieChart(this, stableCommitPanel, "Stable commits") }
    private val commitPanelSplitter = CommitPanelSplitter()
    private val commitScrollPanel = JBScrollPane().apply {
        setViewportView(commitPanelSplitter)
    }
    private val hoursButton = JBRadioButton("Hours")
    private val weekButton = JBRadioButton("Week")
    private val monthButton = JBRadioButton("Month")
    private val yearButton = JBRadioButton("Year")
    private val barChartButtonGroup = ButtonGroup().apply {
        add(hoursButton)
        add(weekButton)
        add(monthButton)
        add(yearButton)
    }
    private val commitBarChartHeader = JBLabel("").apply {
        putClientProperty(FlatClientProperties.STYLE, ("font:+1;border:0,0,5,0"))
    }
    private val commitBarChart = HorizontalBarChart().apply {
        setValuesFormat(DecimalFormat("#,##0"))
        barColor = Color.decode("#f97316")
        setHeader(commitBarChartHeader)
    }
    private val barChartPanel = JBPanel<JBPanel<*>>(GridLayoutManager(2, 1)).apply {
        add(JBPanel<JBPanel<*>>().apply {
            add(JBLabel("BarChart by:"))
            add(hoursButton)
            add(weekButton)
            add(monthButton)
            add(yearButton)
        }, GridConstraints().apply { row = 0; column = 0 })
        add(commitBarChart, GridConstraints().apply { row = 1; column = 0; fill = GridConstraints.FILL_HORIZONTAL })
    }

    private var spinner = SpinnerProgress(100, 10)
    private var spinnerPanel = JBPanel<JBPanel<*>>(MigLayout("fill, insets 0, align center center", "[center]")).apply {
        add(spinner)
        spinner.isIndeterminate = true
    }

    private var email: String? = null

    init {
        panel.apply {
            add(mainInfoPanel, GridConstraints().apply { row = 0; column = 0 })
            add(piePanels, GridConstraints().apply { row = 1; column = 0; fill = GridConstraints.FILL_HORIZONTAL })
            add(
                commitScrollPanel,
                GridConstraints().apply { row = 2; column = 0; fill = GridConstraints.FILL_HORIZONTAL })
            add(barChartPanel, GridConstraints().apply { row = 3; column = 0; fill = GridConstraints.FILL_HORIZONTAL})
            add(spinnerPanel, GridConstraints().apply { row = 4; column = 0 })
        }
        spinnerView(true)
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
        for (listener in hoursButton.actionListeners) {
            hoursButton.removeActionListener(listener)
        }
    }

    private fun openSelected(commitSizeMap: Map<String, CommitSize>, developerInfoMap: Map<String, DeveloperInfo>) {
        this.email = emailComboBox.getSelectedItem().toString()
        clear()
        updateLabels(developerInfoMap, commitSizeMap)
        updatePieChart(createPieDataFile(developerInfoMap[email]!!), filePieChart)
        updatePieChart(createPieDataLines(developerInfoMap[email]!!), linesPieChart)
        updatePieChart(createPieDataStable(commitSizeMap), stableCommitChart)
        commitPanelSplitter.updatePanel(commitSizeMap.filter { it.value.authorEmail == email })
        val dateCollection = commitSizeMap.values.filter { it.authorEmail == email }
            .map { it.date }
        hoursButton.addActionListener {
            updateBarChart(createBarDataByHours(dateCollection), "Commit by hours")
            weekButton
        }
        weekButton.addActionListener {
            updateBarChart(createBarDataByDay(dateCollection), "Commit by day of week")
        }
        monthButton.addActionListener {
            updateBarChart(createBarDataByMonth(dateCollection), "Commit by day of month")
        }
        yearButton.addActionListener {
            updateBarChart(createBarDataByYear(dateCollection), "Commit by month")
        }
        spinnerView(false)
        if (barChartButtonGroup.selection == null) hoursButton.doClick()
        else barChartButtonGroup.elements.toList().find { it.model == barChartButtonGroup.selection }?.doClick()
    }

    private fun spinnerView(isVisible: Boolean) {
        mainInfoPanel.isVisible = !isVisible
        commitScrollPanel.isVisible = !isVisible
        piePanels.isVisible = !isVisible
        barChartPanel.isVisible = !isVisible
        spinnerPanel.isVisible = isVisible

    }

    private fun updateLabels(developerInfoMap: Map<String, DeveloperInfo>, commitSizeMap: Map<String, CommitSize>) {
        authorNameLabel.text = developerInfoMap[email]!!.name
        commitCountLabel.text = developerInfoMap[email]!!.commits.size.toString()
        createdBranchesLabel.text = ""
        val lines = developerInfoMap.values.sumOf { it.getActualLinesOwner().toDouble() }
        ownerPercentageLabel.text =
            String.format("%.2f", ((developerInfoMap[email]!!.getActualLinesOwner()) / lines) * 100)
        val commitDates = commitSizeMap.values.filter { it.authorEmail == email }
            .map { it.date }.sorted().toList()
        val differences: MutableList<Int> = ArrayList()
        for (i in 1..<commitDates.size) {
            differences.add(commitDates[i] - commitDates[i - 1])
        }
        val date = commitDates.maxOrNull()
        lastActivityLabel.text = if (date != null) getStringDate(date) else ""
        val daysCount = commitDates.map { timestampToLocalDate(it) }.size.toDouble()
        commitFrequencyLabel.text = String.format("%.2f", differences.size / daysCount) + " per day"
        avgCommitTimeLabel.text =
            String.format(
                "%.2f",
                differences.sum() / (24 * 3600.0 * differences.size)
            ) + " day"
    }

    private fun initPieChart(
        pieChart1: PieChart,
        panel: JPanel,
        name: String
    ) {
        panel.putClientProperty(FlatClientProperties.STYLE, ("border:5,5,5,5;background:null"))
        panel.layout = MigLayout("wrap,fill,gap 10", "fill")
        val header1 = JLabel(name)
        header1.putClientProperty(FlatClientProperties.STYLE, "font:+1")
        pieChart1.setHeader(header1)
        pieChart1.chartColor.addColor(Color.decode("#a3e635"), Color.decode("#f87171"), Color.decode("#fb923c"))
        pieChart1.putClientProperty(FlatClientProperties.STYLE, "border:5,5,5,5,\$Component.borderColor,,20")
        panel.add(pieChart1, "split 5,height 360")
        pieChart1.visibleLegend(false)
        pieChart1.startAnimation()
    }

    private fun updatePieChart(defaultPieDataset: DefaultPieDataset<String>, pieChart: PieChart) {
        pieChart.setDataset(defaultPieDataset)
        pieChart.startAnimation()
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
        commitSizeMap.values.filter { it.authorEmail == email }.forEach {
            if (it.stability <= 0.2) unStableCommitSize.getAndSet(unStableCommitSize.get() + 1)
            else stableCommitCount.getAndSet(stableCommitCount.get() + 1)
        }
        val dataset = DefaultPieDataset<String>()
        dataset.addValue("stable commit count", stableCommitCount.get())
        dataset.addValue("unstable commit count", unStableCommitSize.get())
        return dataset
    }

    private fun updateBarChart(dataset: DefaultPieDataset<String>, title: String) {
        commitBarChartHeader.text = title
        commitBarChart.setDataset(dataset)
    }

    private fun createBarDataByDay(dateCollection: Collection<Int>): DefaultPieDataset<String> {
        val dataset = DefaultPieDataset<String>()
        val commitCountMap: MutableMap<DayOfWeek, Int> = EnumMap(java.time.DayOfWeek::class.java)
        dateCollection.forEach {
            val day = getDayOfWeek(it)
            commitCountMap[day] = commitCountMap.getOrDefault(day, 0) + 1
        }
        commitCountMap.entries.stream().sorted(java.util.Map.Entry.comparingByKey())
            .forEach { it: Map.Entry<DayOfWeek, Int> -> dataset.addValue(it.key.toString(), it.value) }
        return dataset
    }

    private fun createBarDataByYear(dateCollection: Collection<Int>): DefaultPieDataset<String> {
        val dataset = DefaultPieDataset<String>()
        val commitCountMap: MutableMap<Month, Int> = EnumMap(java.time.Month::class.java)
        dateCollection.forEach {
            val month = getMonthOfYear(it)
            commitCountMap[month] = commitCountMap.getOrDefault(month, 0) + 1
        }
        commitCountMap.entries.sortedBy { it.key }.forEach { dataset.addValue(it.key.toString(), it.value) }
        return dataset
    }

    private fun createBarDataByHours(dateCollection: Collection<Int>): DefaultPieDataset<String> {
        val dataset = DefaultPieDataset<String>()
        val commitCount = IntArray(24)
        dateCollection.forEach { commitCount[getHourOfDay(it)]++ }
        IntStream.range(0, commitCount.size).forEach { index: Int ->
            if (commitCount[index] > 0) dataset.addValue(
                "$index:00", commitCount[index]
            )
        }
        return dataset
    }

    private fun createBarDataByMonth(dateCollection: Collection<Int>): DefaultPieDataset<String> {
        val dataset = DefaultPieDataset<String>()
        val commitCount = IntArray(32)
        dateCollection.forEach { commitCount[getDayOfMouth(it)]++ }
        IntStream.range(0, commitCount.size).forEach { index: Int ->
            if (commitCount[index] > 0) dataset.addValue(
                (index + 1).toString(),
                commitCount[index]
            )
        }
        return dataset
    }
}