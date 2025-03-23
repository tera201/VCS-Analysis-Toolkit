package org.tera201.vcstoolkit.info

import com.formdev.flatlaf.FlatClientProperties
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridLayoutManager
import org.repodriller.scm.entities.CommitSize
import org.repodriller.scm.entities.DeveloperInfo
import org.tera201.swing.chart.ChartLegendRenderer
import org.tera201.swing.chart.data.category.DefaultCategoryDataset
import org.tera201.swing.chart.data.pie.DefaultPieDataset
import org.tera201.swing.chart.line.LineChart
import org.tera201.vcstoolkit.helpers.addComponentPairRow
import org.tera201.vcstoolkit.helpers.addNComponentsRow
import org.tera201.vcstoolkit.helpers.setTextWithShortener
import org.tera201.vcstoolkit.panels.CommitPanelSplitter
import org.tera201.vcstoolkit.tabs.TabManager
import java.awt.Color
import java.awt.Component
import java.text.DecimalFormat
import java.util.*
import java.util.function.Consumer
import javax.swing.*
import kotlin.math.ceil

class InfoPageUI(val tabManager: TabManager) {
    val panel = JBPanel<JBPanel<*>>(GridLayoutManager(4, 1))
    private val mainPathLabel = JBLabel()
    private val authorLabel = JBLabel()
    private val curAuthorLabel = JBLabel()
    private val rowsLabel = JBLabel()
    private val rowSizeLabel = JBLabel()
    private val sizeLabel = JBLabel()
    private val revisionLabel = JBLabel()
    private val labelPanel = JBPanel<JBPanel<*>>(GridLayoutManager(6, 2)).apply {
        var i = 0
        addComponentPairRow(i++, JBLabel("Author"), authorLabel)
        addComponentPairRow(i++, JBLabel("Current author"), curAuthorLabel)
        addComponentPairRow(i++, JBLabel("Rows"), rowsLabel)
        addComponentPairRow(i++, JBLabel("Rows size"), rowSizeLabel)
        addComponentPairRow(i++, JBLabel("Size"), sizeLabel)
        addComponentPairRow(i, JBLabel("Revision"), revisionLabel)
    }
    private val chartPanel = JBPanel<JBPanel<*>>()
    private val authorImpactPieChart = initPieChart(chartPanel, "Authors impact", true)
    private val mainInfoPanel = JBPanel<JBPanel<*>>(GridLayoutManager(2, 2)).apply {
        add(mainPathLabel, GridConstraints().apply { row = 0; column = 0 })
        add(labelPanel, GridConstraints().apply { row = 1; column = 0; anchor = GridConstraints.ANCHOR_NORTHWEST })
        add(chartPanel, GridConstraints().apply { row = 1; column = 1 })
    }
    private val commitPanelSplitter = CommitPanelSplitter()
    private val commitScrollPanel = JBScrollPane().apply { setViewportView(commitPanelSplitter)  }
    private val lineChart: LineChart = LineChart().apply {
        this.chartType = LineChart.ChartType.CURVE
        this.putClientProperty(FlatClientProperties.STYLE, "border:5,5,5,5,\$Component.borderColor,,20")
        this.setValuesFormat(DecimalFormat("#,##0.## B"))
        this.categoryDataset = categoryDataset
        this.chartColor.addColor(Color.decode("#38bdf8"), Color.decode("#fb7185"), Color.decode("#34d399"))
        val header = JLabel("Project Size Evolution")
        header.putClientProperty(FlatClientProperties.STYLE, "font:+1;border:0,0,5,0")
        this.setHeader(header)
    }
    private val lineChartScrollPanel =
        JBScrollPane(JBScrollPane.VERTICAL_SCROLLBAR_NEVER, JBScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED).apply {
            setViewportView(lineChart)
        }
    private var spinnerPanel = createSpinnerPanel()
    private var lastPathNode: String? = null

    init {
        panel.apply {
            addNComponentsRow(0, mainInfoPanel to false)
            addNComponentsRow(1, commitScrollPanel to true)
            addNComponentsRow(2, lineChartScrollPanel to true)
            addNComponentsRow(3, spinnerPanel to false)
        }
        setContentVisibility(false)
    }

    @Throws(InterruptedException::class)
    fun open(commitSizeMap: Map<String, CommitSize>, developerInfoMap: Map<String, DeveloperInfo>) {
        lastPathNode = getPathByTab(tabManager)?.substringAfterLast("/")
        updateLabels(developerInfoMap, commitSizeMap)
        authorImpactPieChart.updateData(createPieData(developerInfoMap))
        commitPanelSplitter.updatePanel(commitSizeMap)
        updateLineChart(commitSizeMap)
        setContentVisibility(true)
    }

    private fun setContentVisibility(showContent: Boolean) {
        mainInfoPanel.isVisible = showContent
        commitScrollPanel.isVisible = showContent
        lineChartScrollPanel.isVisible = showContent
        spinnerPanel.isVisible = !showContent

    }

    private fun updateLabels(developerInfoMap: Map<String, DeveloperInfo>, commitSizeMap: Map<String, CommitSize>) {
        mainPathLabel.text = lastPathNode
        authorLabel.text = commitSizeMap.values.minByOrNull { it.date }?.authorName
        sizeLabel.text = commitSizeMap.values.maxByOrNull { it.date }?.projectSize.toString()
        curAuthorLabel.text = developerInfoMap.values.maxByOrNull { it.getActualLinesOwner() }?.name
        rowsLabel.text = developerInfoMap.values.sumOf { it.getActualLinesOwner() }.toString()
        rowSizeLabel.text = developerInfoMap.values.sumOf { it.getActualLinesSize() }.toString()
        revisionLabel.setTextWithShortener(commitSizeMap.values.maxByOrNull { it.date }?.name ?: "", 6)
    }

    private fun updateLineChart(commitSizeMap: Map<String, CommitSize>) {
        createLineChartData(commitSizeMap.values)
        lineChart.startAnimation()
    }

    private fun createPieData(developerInfoMap: Map<String, DeveloperInfo>): DefaultPieDataset<String> {
        val dataset = DefaultPieDataset<String>()
        developerInfoMap.values.forEach(Consumer { developerInfo: DeveloperInfo ->
            dataset.addValue(developerInfo.name, developerInfo.changes)
        })
        return dataset
    }

    private fun createLineChartData(commitSizes: Collection<CommitSize>) {
        val categoryDataset = DefaultCategoryDataset<String, String>()
        commitSizes.sortedBy { it.date }.groupBy { formatDate(it.date) }.mapNotNull { it.value.maxByOrNull { it.date } }
            .forEach { categoryDataset.addValue(it.projectSize, "Project ", formatDate(it.date)) }
        createLineChartLegend(categoryDataset.columnKeys.size)
        lineChart.categoryDataset = categoryDataset
    }

    private fun createLineChartLegend(categoryDatasetColumnCount: Int) {
        val step = ceil(categoryDatasetColumnCount / 20f)
        lineChart.legendRenderer = object : ChartLegendRenderer() {
            override fun getLegendComponent(legend: Any, index: Int): Component? =
                if (index % step == 0.0f) super.getLegendComponent(legend, index) else null
        }
    }
}