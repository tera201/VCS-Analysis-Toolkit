package org.tera201.vcstoolkit.info

import com.formdev.flatlaf.FlatClientProperties
import com.intellij.ui.components.JBPanel
import net.miginfocom.swing.MigLayout
import org.tera201.swing.chart.data.pie.DefaultPieDataset
import org.tera201.swing.chart.pie.PieChart
import org.tera201.swing.spinner.SpinnerProgress
import org.tera201.vcstoolkit.services.colors.PluginColors
import org.tera201.vcstoolkit.tabs.*
import org.tera201.vcstoolkit.utils.getBranchName
import java.text.SimpleDateFormat
import java.util.*
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.tree.DefaultMutableTreeNode

fun getCirclePath(tabManager: TabManager): String? {
    val fxCircleTab = tabManager.getTabMap()[TabEnum.CIRCLE] as FXCircleTab?
    val circleSelectionManager = fxCircleTab!!.fxCircle.circleSpace.selectionManager
    return circleSelectionManager.selected?.filePath
}

fun getCircleBranch(tabManager: TabManager): String? {
    val fxCircleTab = tabManager.getTabMap()[TabEnum.CIRCLE] as FXCircleTab?
    val circleSelectionManager = fxCircleTab!!.fxCircle.circleSpace.selectionManager
    return circleSelectionManager.selected?.getBranchName()
}

fun getCityPath(tabManager: TabManager): String? {
    val fxCityTab = tabManager.getTabMap()[TabEnum.CITY] as FXCityTab?
    val citySelectionManager = fxCityTab!!.fxCity.citySpace.selectionManager
    return citySelectionManager.selected?.filePath
}

fun getCityBranch(tabManager: TabManager): String? {
    val fxCityTab = tabManager.getTabMap()[TabEnum.CITY] as FXCityTab?
    return fxCityTab!!.modelComboBox.selectedItem?.toString()
}

fun getGitPath(tabManager: TabManager): String? {
    val gitTab = tabManager.getTabMap()[TabEnum.GIT] as GitTab?
    val pathComponent = gitTab!!.controller.pathJTree.selectionPath?.lastPathComponent
    return (pathComponent as? DefaultMutableTreeNode)?.userObject?.toString()
}

fun getPathByTab(tabManager: TabManager): String? {
    when (tabManager.getSelectedTabTitle()) {
        TabEnum.GIT.value -> return getGitPath(tabManager)
        TabEnum.CIRCLE.value -> return getCirclePath(tabManager)
        TabEnum.CITY.value -> return getCityPath(tabManager)
        else -> return null
    }
}

fun checkoutByTab(tabManager: TabManager) {
    val gitTab = tabManager.getTabMap()[TabEnum.GIT] as GitTab?
    when (tabManager.getSelectedTabTitle()) {
        TabEnum.CITY.value -> getCityBranch(tabManager)?.let { gitTab!!.controller.checkoutTo(it) }
        TabEnum.CIRCLE.value -> getCircleBranch(tabManager)?.let { gitTab!!.controller.checkoutTo(it) }
    }
}

fun initPieChart(panel: JPanel, name: String, legend: Boolean): PieChart {
    panel.putClientProperty(FlatClientProperties.STYLE, ("border:5,5,5,5;background:null"))
    panel.layout = MigLayout("wrap,fill,gap 10", "fill")
    val header = JLabel(name).apply { putClientProperty(FlatClientProperties.STYLE, "font:+1") }
    val pieChart = PieChart()
    pieChart.setHeader(header)
    pieChart.putClientProperty(FlatClientProperties.STYLE, "border:5,5,5,5,\$Component.borderColor,,20")
    panel.add(pieChart, "split 5,height 360")
    pieChart.chartColor.addColor(*PluginColors.ALL_COLORS)
    pieChart.visibleLegend(legend)
    pieChart.startAnimation()
    return pieChart
}

fun createSpinnerPanel(): JBPanel<JBPanel<*>> = JBPanel<JBPanel<*>>().apply {
    layout = MigLayout("fill, insets 0, align center center", "[center]")
    add(SpinnerProgress(100, 10).apply { isIndeterminate = true })
}

fun PieChart.updateData(pieDataset: DefaultPieDataset<String>) {
    this.setDataset(pieDataset)
    this.startAnimation()
}


fun formatDate(timestamp: Int): String = SimpleDateFormat("MMM dd, yyyy")
    .format(Date(timestamp.toLong() * 1000))