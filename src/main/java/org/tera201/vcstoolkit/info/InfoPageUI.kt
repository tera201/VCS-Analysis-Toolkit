package org.tera201.vcstoolkit.info

import com.formdev.flatlaf.FlatClientProperties
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridLayoutManager
import com.intellij.util.ui.JBUI
import org.tera201.swing.chart.ChartLegendRenderer
import org.tera201.swing.chart.data.category.DefaultCategoryDataset
import org.tera201.swing.chart.data.pie.DefaultPieDataset
import org.tera201.swing.chart.line.LineChart
import org.tera201.vcsmanager.db.entities.CommitSize
import org.tera201.vcsmanager.db.entities.DeveloperInfo
import org.tera201.vcstoolkit.helpers.addComponentPairRow
import org.tera201.vcstoolkit.helpers.setTextWithShortener
import org.tera201.vcstoolkit.panels.CommitPanelSplitter
import org.tera201.vcstoolkit.tabs.TabManager
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.Font
import java.text.DecimalFormat
import java.util.function.Consumer
import javax.swing.*
import kotlin.math.ceil

class InfoPageUI(val tabManager: TabManager) {
    val panel = JBPanel<JBPanel<*>>(GridLayoutManager(4, 1))
    
    // Header section
    private val mainPathLabel = JBLabel().apply {
        font = font.deriveFont(Font.BOLD, 16f)
        putClientProperty(FlatClientProperties.STYLE, "font:bold +2")
    }
    
    // Statistics labels
    private val authorLabel = JBLabel()
    private val curAuthorLabel = JBLabel()
    private val rowsLabel = JBLabel()
    private val rowSizeLabel = JBLabel()
    private val sizeLabel = JBLabel()
    private val revisionLabel = JBLabel()
    
    // Create compact header panel
    private val headerPanel = JBPanel<JBPanel<*>>().apply {
        layout = GridLayoutManager(2, 2)
        border = JBUI.Borders.empty(10)
        add(createInfoLabel("Project:").apply { 
            font = font.deriveFont(Font.BOLD, 13f)
        }, GridConstraints().apply { 
            row = 0; column = 0 
            anchor = GridConstraints.ANCHOR_WEST
        })
        add(mainPathLabel, GridConstraints().apply { 
            row = 0; column = 1 
            anchor = GridConstraints.ANCHOR_WEST
            fill = GridConstraints.FILL_HORIZONTAL
        })
    }
    
    // Statistics panel with tile
    private val statisticsPanel = createTilePanel("Project Statistics").apply {
        layout = GridLayoutManager(6, 2)
        var i = 0
        addComponentPairRow(i++, createInfoLabel("First Author:"), authorLabel)
        addComponentPairRow(i++, createInfoLabel("Main Contributor:"), curAuthorLabel)
        addComponentPairRow(i++, createInfoLabel("Total Lines:"), rowsLabel)
        addComponentPairRow(i++, createInfoLabel("Lines Size:"), rowSizeLabel)
        addComponentPairRow(i++, createInfoLabel("Project Size:"), sizeLabel)
        addComponentPairRow(i, createInfoLabel("Latest Revision:"), revisionLabel)
    }
    
    // Authors impact chart
    private val chartPanel = JBPanel<JBPanel<*>>()
    private val authorImpactTile = createTilePanel("Authors Impact").apply {
        layout = GridLayoutManager(1, 1)
        add(chartPanel, GridConstraints().apply { 
            row = 0; column = 0
            fill = GridConstraints.FILL_BOTH
        })
    }
    private val authorImpactPieChart = initPieChart(chartPanel, "", false)
    
    // Top section with statistics and chart
    private val topSection = JBPanel<JBPanel<*>>(GridLayoutManager(2, 5)).apply {
        border = JBUI.Borders.empty(5)
        // Header spans all columns
        add(headerPanel, GridConstraints().apply { 
            row = 0; column = 0
            colSpan = 5
            fill = GridConstraints.FILL_HORIZONTAL
        })
        // Statistics takes 2 columns (40%)
        add(statisticsPanel, GridConstraints().apply { 
            row = 1; column = 0
            colSpan = 2
            fill = GridConstraints.FILL_BOTH
        })
        // Authors Impact chart takes 3 columns (60%)
        add(authorImpactTile, GridConstraints().apply { 
            row = 1; column = 2
            colSpan = 3
            fill = GridConstraints.FILL_BOTH
        })
    }
    
    // Commit history section
    private val commitPanelSplitter = CommitPanelSplitter()
    private val commitScrollPane = JBScrollPane(commitPanelSplitter).apply {
        minimumSize = Dimension(400, 150)
        preferredSize = Dimension(600, 200)
        maximumSize = Dimension(Int.MAX_VALUE, 250)
    }
    private val commitTile = createTilePanel("Commit History").apply {
        layout = GridLayoutManager(1, 1)
        minimumSize = Dimension(400, 150)
        preferredSize = Dimension(600, 200)
        maximumSize = Dimension(Int.MAX_VALUE, 250)
        add(commitScrollPane, GridConstraints().apply { 
            row = 0; column = 0
            fill = GridConstraints.FILL_BOTH
        })
    }
    
    // Line chart section
    private val lineChart: LineChart = LineChart().apply {
        chartType = LineChart.ChartType.CURVE
        putClientProperty(FlatClientProperties.STYLE, "border:5,5,5,5,\$Component.borderColor,,20")
        setValuesFormat(DecimalFormat("#,##0.## B"))
        minimumSize = Dimension(400, 200)
        preferredSize = Dimension(600, 300)
        chartColor.addColor(
            JBColor(Color.decode("#38bdf8"), Color.decode("#5A9CFF")),
            JBColor(Color.decode("#fb7185"), Color.decode("#ff8fa3")),
            JBColor(Color.decode("#34d399"), Color.decode("#6ee7b7"))
        )
    }
    
    private val lineChartScrollPane = JBScrollPane(
        JBScrollPane.VERTICAL_SCROLLBAR_NEVER, 
        JBScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
    ).apply {
        setViewportView(lineChart)
        minimumSize = Dimension(400, 200)
        preferredSize = Dimension(600, 300)
        maximumSize = Dimension(Int.MAX_VALUE, 400)
    }
    
    private val lineChartTile = createTilePanel("Project Size Evolution").apply {
        layout = GridLayoutManager(1, 1)
        minimumSize = Dimension(400, 200)
        preferredSize = Dimension(600, 300)
        maximumSize = Dimension(Int.MAX_VALUE, 400)
        add(lineChartScrollPane, GridConstraints().apply { 
            row = 0; column = 0
            fill = GridConstraints.FILL_BOTH
        })
    }
    
    private var spinnerPanel = createSpinnerPanel()
    private var lastPathNode: String? = null

    init {
        panel.apply {
            border = JBUI.Borders.empty(10)
            add(topSection, GridConstraints().apply { 
                row = 0; column = 0
                fill = GridConstraints.FILL_HORIZONTAL
                vSizePolicy = GridConstraints.SIZEPOLICY_FIXED
            })
            add(commitTile, GridConstraints().apply { 
                row = 1; column = 0
                fill = GridConstraints.FILL_HORIZONTAL
                vSizePolicy = GridConstraints.SIZEPOLICY_FIXED
            })
            add(lineChartTile, GridConstraints().apply { 
                row = 2; column = 0
                fill = GridConstraints.FILL_BOTH
                vSizePolicy = GridConstraints.SIZEPOLICY_CAN_GROW or GridConstraints.SIZEPOLICY_WANT_GROW
            })
            add(spinnerPanel, GridConstraints().apply { 
                row = 3; column = 0
                vSizePolicy = GridConstraints.SIZEPOLICY_FIXED
            })
        }
        setContentVisibility(false)
    }

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
    
    private fun createInfoLabel(text: String): JBLabel {
        return JBLabel(text).apply {
            foreground = JBColor.GRAY
        }
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
        topSection.isVisible = showContent
        commitTile.isVisible = showContent
        lineChartTile.isVisible = showContent
        spinnerPanel.isVisible = !showContent
    }

    private fun updateLabels(developerInfoMap: Map<String, DeveloperInfo>, commitSizeMap: Map<String, CommitSize>) {
        mainPathLabel.text = lastPathNode ?: "Project"
        authorLabel.text = commitSizeMap.values.minByOrNull { it.date }?.authorName ?: "N/A"
        sizeLabel.text = formatBytes(commitSizeMap.values.maxByOrNull { it.date }?.projectSize ?: 0)
        curAuthorLabel.text = developerInfoMap.values.maxByOrNull { it.actualLinesOwner }?.name ?: "N/A"
        val totalLines = developerInfoMap.values.sumOf { it.actualLinesOwner }
        rowsLabel.text = String.format("%,d lines", totalLines)
        rowSizeLabel.text = String.format("%,d lines", totalLines)
        revisionLabel.setTextWithShortener(commitSizeMap.values.maxByOrNull { it.date }?.name ?: "N/A", 6)
    }
    
    private fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.2f KB", bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> String.format("%.2f MB", bytes / (1024.0 * 1024))
            else -> String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024))
        }
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
        commitSizes.sortedBy { it.date }
            .groupBy { formatDate(it.date) }
            .mapNotNull { it.value.maxByOrNull { commit -> commit.date } }
            .forEach { categoryDataset.addValue(it.projectSize, "Project Size", formatDate(it.date)) }
        createLineChartLegend(categoryDataset.columnKeys.size)
        lineChart.categoryDataset = categoryDataset
    }

    private fun createLineChartLegend(categoryDatasetColumnCount: Int) {
        val step = ceil(categoryDatasetColumnCount / 20f).toInt()
        lineChart.legendRenderer = object : ChartLegendRenderer() {
            override fun getLegendComponent(legend: Any, index: Int): Component? =
                if (index % step == 0) super.getLegendComponent(legend, index) else null
        }
    }
}