package org.tera201.vcstoolkit.info

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import org.tera201.vcsmanager.db.entities.CommitEntity
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.text.SimpleDateFormat
import java.util.*
import javax.swing.JComponent
import javax.swing.table.AbstractTableModel

class CommitDetailsDialog(
    private val project: Project,
    private val commits: List<CommitEntity>,
    private val title: String
) : DialogWrapper(project) {

    private val tableModel = CommitTableModel(commits)
    private val table = JBTable(tableModel)
    private val visitedCommits = mutableSetOf<String>()

    init {
        init()
        setTitle(title)
        setupTable()
    }

    private fun setupTable() {
        // Set column widths
        table.columnModel.getColumn(0).preferredWidth = 100 // Hash
        table.columnModel.getColumn(1).preferredWidth = 120 // Author
        table.columnModel.getColumn(2).preferredWidth = 250 // Short Message
        table.columnModel.getColumn(3).preferredWidth = 350 // Full Message
        table.columnModel.getColumn(4).preferredWidth = 150 // Date
        table.columnModel.getColumn(5).preferredWidth = 80  // Stability
        table.columnModel.getColumn(6).preferredWidth = 60  // Checked

        // Make columns optional (hidden by default)
        table.columnModel.getColumn(1).minWidth = 0 // Author - optional
        table.columnModel.getColumn(3).minWidth = 0 // Full Message - optional
        table.columnModel.getColumn(4).minWidth = 0 // Date - optional

        table.setShowGrid(true)
        table.autoResizeMode = JBTable.AUTO_RESIZE_OFF

        // Add double-click listener
        table.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2) {
                    val row = table.rowAtPoint(e.point)
                    if (row >= 0) {
                        openCommitInIDE(commits[row])
                    }
                }
            }

            override fun mousePressed(e: MouseEvent) {
                val row = table.rowAtPoint(e.point)
                val col = table.columnAtPoint(e.point)

                // Handle checkbox column click
                if (col == 6 && row >= 0) {
                    val commit = commits[row]
                    if (visitedCommits.contains(commit.hash)) {
                        visitedCommits.remove(commit.hash)
                    } else {
                        visitedCommits.add(commit.hash)
                    }
                    tableModel.fireTableCellUpdated(row, col)
                }
            }
        })
    }

    private fun openCommitInIDE(commit: CommitEntity): Nothing = TODO()

    override fun createCenterPanel(): JComponent {
        val scrollPane = JBScrollPane(table)
        scrollPane.preferredSize = Dimension(1000, 600)
        return scrollPane
    }

    override fun createActions() = arrayOf(okAction)

    inner class CommitTableModel(private val commits: List<CommitEntity>) : AbstractTableModel() {
        private val columnNames = arrayOf(
            "Hash",
            "Author",
            "Short Message",
            "Full Message",
            "Date",
            "Stability",
            "Checked"
        )

        override fun getRowCount() = commits.size

        override fun getColumnCount() = columnNames.size

        override fun getColumnName(column: Int) = columnNames[column]

        override fun getColumnClass(columnIndex: Int): Class<*> {
            return when (columnIndex) {
                5 -> java.lang.Double::class.java
                6 -> java.lang.Boolean::class.java
                else -> String::class.java
            }
        }

        override fun isCellEditable(rowIndex: Int, columnIndex: Int) = columnIndex == 6

        override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
            val commit = commits[rowIndex]
            return when (columnIndex) {
                0 -> commit.hash.substring(0, minOf(8, commit.hash.length))
                1 -> commit.authorName
                2 -> commit.shortMessage
                3 -> commit.fullMessage
                4 -> {
                    val date = Date(commit.date * 1000L)
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date)
                }
                5 -> commit.stability
                6 -> visitedCommits.contains(commit.hash)
                else -> ""
            }
        }

        override fun setValueAt(aValue: Any?, rowIndex: Int, columnIndex: Int) {
            if (columnIndex == 6 && aValue is Boolean) {
                val commit = commits[rowIndex]
                if (aValue) {
                    visitedCommits.add(commit.hash)
                } else {
                    visitedCommits.remove(commit.hash)
                }
                fireTableCellUpdated(rowIndex, columnIndex)
            }
        }
    }
}