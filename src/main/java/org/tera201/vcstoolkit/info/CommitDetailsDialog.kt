package org.tera201.vcstoolkit.info

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import org.tera201.vcsmanager.db.entities.CommitEntity
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.text.SimpleDateFormat
import java.util.*
import javax.swing.Box
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.table.AbstractTableModel

class CommitDetailsDialog(
    private val project: Project,
    private val commits: List<CommitEntity>,
    private val title: String
) : DialogWrapper(project) {

    private val tableModel = CommitTableModel(commits)
    private val table = JBTable(tableModel)
    private val visitedCommits = mutableSetOf<String>()

    // Column visibility state
    private var showAuthor = true
    private var showDate = true
    private var showFullMessage = false // Start with short message

    // Store original column widths and order
    private val columnWidths = mapOf(
        0 to 100,  // Hash
        1 to 120,  // Author
        2 to 250,  // Short Message
        3 to 350,  // Full Message
        4 to 150,  // Date
        5 to 80,   // Stability
        6 to 60    // Checked
    )

    // Column controls
    private val authorCheckbox = JBCheckBox("Show Author", showAuthor)
    private val dateCheckbox = JBCheckBox("Show Date", showDate)
    private val messageToggleCheckbox = JBCheckBox("Show Full Message", showFullMessage)

    init {
        init()
        setTitle(title)
        setupTable()
        setupColumnControls()
    }

    private fun setupTable() {
        // Set initial column widths
        for (i in 0 until table.columnCount) {
            table.columnModel.getColumn(i).preferredWidth = columnWidths[i] ?: 100
        }

        table.setShowGrid(true)
        table.autoResizeMode = JBTable.AUTO_RESIZE_OFF

        // Apply initial visibility
        updateColumnVisibility()

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
                if (col >= 0 && row >= 0) {
                    val modelCol = table.convertColumnIndexToModel(col)
                    if (modelCol == 6) {
                        val commit = commits[row]
                        if (visitedCommits.contains(commit.hash)) {
                            visitedCommits.remove(commit.hash)
                        } else {
                            visitedCommits.add(commit.hash)
                        }
                        tableModel.fireTableCellUpdated(row, modelCol)
                    }
                }
            }
        })
    }

    private fun setupColumnControls() {
        authorCheckbox.addActionListener {
            showAuthor = authorCheckbox.isSelected
            updateColumnVisibility()
        }

        dateCheckbox.addActionListener {
            showDate = dateCheckbox.isSelected
            updateColumnVisibility()
        }

        messageToggleCheckbox.addActionListener {
            showFullMessage = messageToggleCheckbox.isSelected
            tableModel.setShowFullMessage(showFullMessage)
            updateColumnVisibility()
            tableModel.fireTableStructureChanged()
            // Restore column widths after structure change
            for (i in 0 until table.columnCount) {
                val modelIndex = table.convertColumnIndexToModel(i)
                table.columnModel.getColumn(i).preferredWidth = columnWidths[modelIndex] ?: 100
            }
            updateColumnVisibility()
        }
    }

    private fun updateColumnVisibility() {
        val columnModel = table.columnModel

        // Author column (index 1)
        setColumnVisibility(1, showAuthor)

        // Date column (index 4)
        setColumnVisibility(4, showDate)

        // Message columns (2 = short, 3 = full)
        if (showFullMessage) {
            setColumnVisibility(2, false) // Hide short message
            setColumnVisibility(3, true)  // Show full message
        } else {
            setColumnVisibility(2, true)  // Show short message
            setColumnVisibility(3, false) // Hide full message
        }
    }

    private fun setColumnVisibility(columnIndex: Int, visible: Boolean) {
        try {
            val column = table.columnModel.getColumn(
                table.convertColumnIndexToView(columnIndex)
            )
            if (visible) {
                column.minWidth = 15
                column.maxWidth = Int.MAX_VALUE
                column.preferredWidth = columnWidths[columnIndex] ?: 100
            } else {
                column.minWidth = 0
                column.maxWidth = 0
                column.preferredWidth = 0
            }
        } catch (e: Exception) {
            // Column might not be in view
        }
    }

    private fun openCommitInIDE(commit: CommitEntity): Nothing = TODO()

    override fun createCenterPanel(): JComponent {
        val mainPanel = JPanel(BorderLayout())

        // Create controls panel at the top
        val controlsPanel = JPanel().apply {
            layout = FlowLayout(FlowLayout.LEFT, 10, 5)
            add(JBLabel("Column Visibility:"))
            add(authorCheckbox)
            add(dateCheckbox)
            add(Box.createHorizontalStrut(20))
            add(JBLabel("Message Display:"))
            add(messageToggleCheckbox)
        }

        // Create table scroll pane
        val scrollPane = JBScrollPane(table)
        scrollPane.preferredSize = Dimension(1000, 600)

        mainPanel.add(controlsPanel, BorderLayout.NORTH)
        mainPanel.add(scrollPane, BorderLayout.CENTER)

        return mainPanel
    }

    override fun createActions() = arrayOf(okAction)

    inner class CommitTableModel(private val commits: List<CommitEntity>) : AbstractTableModel() {
        private var showFullMessage = false

        private val baseColumnNames = arrayOf(
            "Hash",
            "Author",
            "Message",
            "Full Message",
            "Date",
            "Stability",
            "Checked"
        )

        fun setShowFullMessage(show: Boolean) {
            showFullMessage = show
        }

        override fun getRowCount() = commits.size

        override fun getColumnCount() = baseColumnNames.size

        override fun getColumnName(column: Int): String {
            return when (column) {
                2 -> if (showFullMessage) "Full Message" else "Short Message"
                3 -> "Full Message"
                else -> baseColumnNames[column]
            }
        }

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
                2 -> if (showFullMessage) commit.fullMessage else commit.shortMessage
                3 -> commit.fullMessage
                4 -> {
                    val date = Date(commit.date * 1000L)
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date)
                }
                5 ->commit.stability
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