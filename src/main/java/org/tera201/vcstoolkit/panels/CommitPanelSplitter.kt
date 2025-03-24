package org.tera201.vcstoolkit.panels

import com.intellij.ui.JBSplitter
import com.intellij.ui.components.JBList
import org.repodriller.scm.entities.CommitSize
import org.tera201.vcstoolkit.utils.DateUtils.Companion.timestampToLocalDate
import java.util.*
import javax.swing.DefaultListModel
import javax.swing.JList
import javax.swing.ListSelectionModel
import javax.swing.event.ListSelectionListener

class CommitPanelSplitter: JBSplitter(false, 0.95f) {
    private val commitPanel = CommitPanel()
    private val listModel = DefaultListModel<String>()
    private val yearList: JList<String> = JBList(listModel).apply {
        selectionMode = ListSelectionModel.SINGLE_SELECTION
    }

    init {
        dividerWidth = 1
        firstComponent = commitPanel
        secondComponent = yearList
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
        this.updateUI()
    }

    fun updatePanel(commitSizeMap: Map<String, CommitSize>) {
        commitSizeMap.values.map { commitSize: CommitSize ->
            timestampToLocalDate(commitSize.date).year
        }.distinct().sortedDescending().forEach { year: Int ->
            listModel.addElement(year.toString())
        }

        setupYearListListener(commitSizeMap)
        yearList.setSelectedIndex(0)
    }

    fun clear() {
        yearList.listSelectionListeners
            .forEach { listener: ListSelectionListener? -> yearList.removeListSelectionListener(listener) }
        listModel.removeAllElements()
    }
}