package org.tera201.vcstoolkit.info

import com.formdev.flatlaf.FlatClientProperties
import com.intellij.ui.JBSplitter
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridLayoutManager
import net.miginfocom.swing.MigLayout
import org.repodriller.scm.entities.CommitSize
import org.repodriller.scm.entities.DeveloperInfo
import org.tera201.swing.chart.ChartLegendRenderer
import org.tera201.swing.chart.data.category.DefaultCategoryDataset
import org.tera201.swing.chart.data.pie.DefaultPieDataset
import org.tera201.swing.chart.line.LineChart
import org.tera201.swing.chart.pie.PieChart
import org.tera201.swing.spinner.SpinnerProgress
import org.tera201.vcstoolkit.panels.CommitPanel
import org.tera201.vcstoolkit.tabs.TabManager
import org.tera201.vcstoolkit.utils.DateCalculator
import java.awt.Color
import java.awt.Component
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.function.Consumer
import java.util.stream.Collectors
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
        addComponentPairRow(JBLabel("Author"), authorLabel, i++)
        addComponentPairRow(JBLabel("Current author"), curAuthorLabel, i++)
        addComponentPairRow(JBLabel("Rows"), rowsLabel, i++)
        addComponentPairRow(JBLabel("Rows size"), rowSizeLabel, i++)
        addComponentPairRow(JBLabel("Size"), sizeLabel, i++)
        addComponentPairRow(JBLabel("Revision"), revisionLabel, i)
    }
    private val authorImpactPieChart = PieChart().apply {
        val header = JLabel("Authors impact")
        header.putClientProperty(FlatClientProperties.STYLE, "font:+1")
        this.setHeader(header)
        this.chartColor.addColor(
            Color.decode("#f87171"),
            Color.decode("#fb923c"),
            Color.decode("#fbbf24"),
            Color.decode("#a3e635"),
            Color.decode("#34d399"),
            Color.decode("#22d3ee"),
            Color.decode("#818cf8"),
            Color.decode("#c084fc")
        )
        this.putClientProperty(FlatClientProperties.STYLE, "border:5,5,5,5,\$Component.borderColor,,20")
    }
    private val chartPanel = JBPanel<JBPanel<*>>().apply {
        this.putClientProperty(FlatClientProperties.STYLE, ("border:5,5,5,5;background:null"))
        this.layout = MigLayout("wrap,fill,gap 10", "fill")
        this.add(authorImpactPieChart, "split 5,height 360")
    }
    private val mainInfoPanel = JBPanel<JBPanel<*>>(GridLayoutManager(2, 2)).apply {
        add(mainPathLabel, GridConstraints().apply { row = 0; column = 0 })
        add(labelPanel, GridConstraints().apply { row = 1; column = 0; anchor = GridConstraints.ANCHOR_NORTHWEST })
        add(chartPanel, GridConstraints().apply { row = 1; column = 1 })
    }
    private val listModel = DefaultListModel<String>()
    private val yearList: JList<String> = JBList(listModel).apply {
        selectionMode = ListSelectionModel.SINGLE_SELECTION
    }
    private val commitPanel = CommitPanel()
    private val splitter = JBSplitter(false, 0.95f).apply {
        dividerWidth = 1
        firstComponent = commitPanel
        secondComponent = yearList

    }
    private val commitScrollPanel = JBScrollPane().apply {
        setViewportView(splitter)
    }
    private val lineChart: LineChart = LineChart().apply {
        this.chartType = LineChart.ChartType.CURVE
        this.putClientProperty(FlatClientProperties.STYLE, "border:5,5,5,5,\$Component.borderColor,,20")
        this.setValuesFormat(DecimalFormat("#,##0.## B"))
        this.categoryDataset = categoryDataset
        this.chartColor.addColor(Color.decode("#38bdf8"), Color.decode("#fb7185"), Color.decode("#34d399"))
        val header = JLabel("Project Size Evolution")
        header.putClientProperty(FlatClientProperties.STYLE, ("font:+1;border:0,0,5,0"))
        this.setHeader(header)
    }
    private val lineChartScrollPanel =
        JBScrollPane(JBScrollPane.VERTICAL_SCROLLBAR_NEVER, JBScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED).apply {
            setViewportView(lineChart)
        }

    private var spinner = SpinnerProgress(100, 10)
    private var spinnerPanel = JBPanel<JBPanel<*>>(MigLayout("fill, insets 0, align center center", "[center]")).apply {
        add(spinner)
        spinner.isIndeterminate = true
    }
    private var lastPathNode: String? = null

    init {
        panel.apply {
            add(mainInfoPanel, GridConstraints().apply { row = 0; column = 0 })
            add(
                commitScrollPanel,
                GridConstraints().apply { row = 1; column = 0; fill = GridConstraints.FILL_HORIZONTAL })
            add(
                lineChartScrollPanel,
                GridConstraints().apply { row = 2; column = 0; fill = GridConstraints.FILL_HORIZONTAL })
            add(spinnerPanel, GridConstraints().apply { row = 3; column = 0 })

        }
        spinnerView(isVisible = true)
    }

    @Throws(InterruptedException::class)
    fun open(commitSizeMap: Map<String, CommitSize>, developerInfoMap: Map<String, DeveloperInfo>) {
        val path = getPathByTab(tabManager)
        lastPathNode = path?.substring(path.lastIndexOf("/") + 1)
        spinner.isIndeterminate = false
        Thread.sleep(100)
        updateLabels(developerInfoMap, commitSizeMap)
        updatePieChart(developerInfoMap)
        updateCalendarPanel(commitSizeMap)
        updateLineChart(commitSizeMap)
        spinnerView(isVisible = false)
    }

    private fun spinnerView(isVisible: Boolean) {
        mainInfoPanel.isVisible = !isVisible
        commitScrollPanel.isVisible = !isVisible
        lineChartScrollPanel.isVisible = !isVisible
        spinnerPanel.isVisible = isVisible

    }

    private fun updateLabels(developerInfoMap: Map<String, DeveloperInfo>, commitSizeMap: Map<String, CommitSize>) {
        mainPathLabel.text = lastPathNode
        authorLabel.text =
            commitSizeMap.values.minByOrNull { it.date }?.authorName
        sizeLabel.text =
            commitSizeMap.values.maxByOrNull { it.date }?.projectSize.toString()
        curAuthorLabel.text =
            developerInfoMap.values.maxByOrNull { it.getActualLinesOwner() }?.name
        rowsLabel.text =
            developerInfoMap.values.sumOf { it.getActualLinesOwner() }.toString()
        rowSizeLabel.text =
            developerInfoMap.values.sumOf { it.getActualLinesSize() }.toString()
        revisionLabel.setTextWithShortener(
            commitSizeMap.values.maxByOrNull { it.date }?.name ?: "",
            6
        )
    }

    private fun updatePieChart(developerInfoMap: Map<String, DeveloperInfo>) {
        authorImpactPieChart.setDataset(createPieData(developerInfoMap))
        authorImpactPieChart.startAnimation()
    }

    private fun updateLineChart(commitSizeMap: Map<String, CommitSize>) {
        createLineChartData(commitSizeMap.values)
        lineChart.startAnimation()
    }

    private fun setupYearListListener(commitSizeMap: Map<String, CommitSize>) {
        yearList.addListSelectionListener { event ->
            if (!event.valueIsAdjusting) {
                val year = yearList.selectedValue.toInt()
                updateCommitPanel(commitSizeMap, year)
            }
        }
    }

    private fun updateCommitPanel(commitSizeMap: Map<String, CommitSize>, year: Int) {
        commitPanel.updatePanel(year)
        commitSizeMap.values.forEach {
            val calendar = Calendar.getInstance()
            calendar.time = Date(it.date.toLong() * 1000)
            if (calendar[Calendar.YEAR] == year) {
                commitPanel.addCommitCountForDay(calendar[Calendar.DAY_OF_YEAR], 1)
            }
        }
        splitter.updateUI()
    }

    private fun updateCalendarPanel(commitSizeMap: Map<String, CommitSize>) {

        val calendar = Calendar.getInstance()
        commitSizeMap.values.stream().map { commitSize: CommitSize ->
            val date = Date(commitSize.date.toLong() * 1000)
            calendar.time = date
            calendar[Calendar.YEAR]
        }.collect(Collectors.toSet()).stream().sorted(Comparator.reverseOrder()).forEach { year: Int ->
            listModel.addElement(year.toString())
        }

        setupYearListListener(commitSizeMap)
        yearList.selectedIndex = 0

    }

    private fun createPieData(developerInfoMap: Map<String, DeveloperInfo>): DefaultPieDataset<String> {
        val dataset = DefaultPieDataset<String>()
        developerInfoMap.values.forEach(Consumer { developerInfo: DeveloperInfo ->
            dataset.addValue(
                developerInfo.name,
                developerInfo.changes
            )
        })
        return dataset
    }

    private fun createLineChartData(commitSizeCollection: Collection<CommitSize>) {
        val categoryDataset = DefaultCategoryDataset<String, String>()
        val df = SimpleDateFormat("MMM dd, yyyy")
        commitSizeCollection.groupBy { df.format(Date(it.date.toLong() * 1000)) }
            .mapValues { (_, values) -> values.maxByOrNull { it.date } }
            .values
            .filterNotNull()
            .sortedBy { it.date }
            .forEach { commitSize ->
                val date = Date(commitSize.date.toLong() * 1000)
                val formattedDate = df.format(date)
                categoryDataset.addValue(commitSize.projectSize, "Project ", formattedDate)
            }

        /**
         * Control the legend we do not show all legend
         */
        try {
            val date = df.parse(categoryDataset.getColumnKey(0))
            val dateEnd = df.parse(categoryDataset.getColumnKey(categoryDataset.columnCount - 1))

            val dcal = DateCalculator(date, dateEnd)
            val diff = dcal.differenceDays
            val valuesCount = categoryDataset.columnKeys.size.toLong()

            val d = ceil((valuesCount / 20f).toDouble())
            lineChart.legendRenderer = object : ChartLegendRenderer() {
                override fun getLegendComponent(legend: Any, index: Int): Component? {
                    return if (index % d == 0.0) {
                        super.getLegendComponent(legend, index)
                    } else {
                        null
                    }
                }
            }
        } catch (e: ParseException) {
            System.err.println(e)
        }

        lineChart.categoryDataset = categoryDataset
    }
}