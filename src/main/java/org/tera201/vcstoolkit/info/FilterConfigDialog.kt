package org.tera201.vcstoolkit.info

import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.JBColor
import com.intellij.ui.components.*
import com.intellij.util.ui.JBUI
import java.awt.*
import java.text.SimpleDateFormat
import java.util.*
import javax.swing.*

data class CommitFilterConfig(
    val name: String,
    val startDate: String? = null,
    val endDate: String? = null,
    val commitAmount: Int? = null,
    val isLastCommits: Boolean = true
)

class FilterConfigDialog(parent: Component) : DialogWrapper(parent, true) {
    private val filterListModel = DefaultListModel<String>()
    private val filterList = JBList(filterListModel)
    private val filters = mutableListOf<CommitFilterConfig>()

    // Add filter components
    private val nameField = JBTextField(20)
    private val startDateField = JBTextField(10).apply {
        toolTipText = "Format: yyyy-MM-dd"
    }
    private val endDateField = JBTextField(10).apply {
        toolTipText = "Format: yyyy-MM-dd"
    }
    private val useDateRangeCheckBox = JCheckBox("Use Date Range")
    private val commitAmountSpinner = JSpinner(SpinnerNumberModel(10, 1, 10000, 1))
    private val useAmountCheckBox = JCheckBox("Limit Commit Amount")
    private val commitOrderComboBox = ComboBox<String>(arrayOf("Last Commits", "First Commits"))

    init {
        title = "Configure Commit Filters"

        // Load sample filters
        addSampleFilters()

        init()

        setupListeners()
    }

    private fun addSampleFilters() {
        val sampleFilters = listOf(
            CommitFilterConfig("Last 30 Commits", commitAmount = 30, isLastCommits = true),
            CommitFilterConfig("First 100 Commits", commitAmount = 100, isLastCommits = false),
            CommitFilterConfig("All Time")
        )

        filters.addAll(sampleFilters)
        updateFilterList()
    }

    override fun createCenterPanel(): JComponent {
        val mainPanel = JPanel(BorderLayout(10, 10)).apply {
            border = JBUI.Borders.empty(10)
            preferredSize = Dimension(700, 500)
        }

        // Left panel - Filter list
        val leftPanel = JPanel(BorderLayout(5, 5)).apply {
            preferredSize = Dimension(250, 0)
        }

        leftPanel.add(JBLabel("Existing Filters:"), BorderLayout.NORTH)

        val scrollPane = JBScrollPane(filterList).apply {
            border = BorderFactory.createLineBorder(JBColor.border())
        }
        leftPanel.add(scrollPane, BorderLayout.CENTER)

        val removeButton = JButton("Remove Selected").apply {
            addActionListener {
                val selectedIndex = filterList.selectedIndex
                if (selectedIndex >= 0) {
                    filters.removeAt(selectedIndex)
                    updateFilterList()
                }
            }
        }
        leftPanel.add(removeButton, BorderLayout.SOUTH)

        // Right panel - Add new filter
        val rightPanel = createAddFilterPanel()

        mainPanel.add(leftPanel, BorderLayout.WEST)
        mainPanel.add(rightPanel, BorderLayout.CENTER)

        return mainPanel
    }

    private fun createAddFilterPanel(): JPanel {
        val panel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = JBUI.Borders.empty(0, 10, 0, 0)
        }

        // Title
        panel.add(JBLabel("Add New Filter:").apply {
            font = font.deriveFont(Font.BOLD, 14f)
            alignmentX = Component.LEFT_ALIGNMENT
        })
        panel.add(Box.createVerticalStrut(15))

        // Name field
        panel.add(createLabeledField("Filter Name:", nameField))
        panel.add(Box.createVerticalStrut(15))

        // Date range section
        panel.add(useDateRangeCheckBox.apply {
            alignmentX = Component.LEFT_ALIGNMENT
        })
        panel.add(Box.createVerticalStrut(10))

        val datePanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            alignmentX = Component.LEFT_ALIGNMENT
            border = JBUI.Borders.emptyLeft(25)
        }

        datePanel.add(createLabeledField("Start Date:", startDateField))
        datePanel.add(Box.createVerticalStrut(5))
        datePanel.add(JBLabel("(yyyy-MM-dd)").apply {
            font = font.deriveFont(Font.ITALIC, 10f)
            foreground = JBColor.GRAY
            alignmentX = Component.LEFT_ALIGNMENT
            border = JBUI.Borders.emptyLeft(130)
        })
        datePanel.add(Box.createVerticalStrut(10))
        datePanel.add(createLabeledField("End Date:", endDateField))
        datePanel.add(Box.createVerticalStrut(5))
        datePanel.add(JBLabel("(yyyy-MM-dd)").apply {
            font = font.deriveFont(Font.ITALIC, 10f)
            foreground = JBColor.GRAY
            alignmentX = Component.LEFT_ALIGNMENT
            border = JBUI.Borders.emptyLeft(130)
        })

        panel.add(datePanel)
        panel.add(Box.createVerticalStrut(15))

        // Commit amount section
        panel.add(useAmountCheckBox.apply {
            alignmentX = Component.LEFT_ALIGNMENT
        })
        panel.add(Box.createVerticalStrut(10))

        val amountPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            alignmentX = Component.LEFT_ALIGNMENT
            border = JBUI.Borders.emptyLeft(25)
        }

        amountPanel.add(createLabeledField("Commit Amount:", commitAmountSpinner))
        amountPanel.add(Box.createVerticalStrut(10))
        amountPanel.add(createLabeledField("Order:", commitOrderComboBox))

        panel.add(amountPanel)
        panel.add(Box.createVerticalStrut(20))

        // Add button
        val addButton = JButton("Add Filter").apply {
            alignmentX = Component.LEFT_ALIGNMENT
            addActionListener {
                addNewFilter()
            }
        }
        panel.add(addButton)

        panel.add(Box.createVerticalGlue())

        // Initially disable date and amount fields
        startDateField.isEnabled = false
        endDateField.isEnabled = false
        commitAmountSpinner.isEnabled = false
        commitOrderComboBox.isEnabled = false

        return panel
    }

    private fun createLabeledField(labelText: String, component: JComponent): JPanel {
        return JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            alignmentX = Component.LEFT_ALIGNMENT
            maximumSize = Dimension(Int.MAX_VALUE, 30)

            add(JBLabel(labelText).apply {
                preferredSize = Dimension(120, 25)
            })
            add(Box.createHorizontalStrut(10))
            add(component.apply {
                if (component is JTextField) {
                    maximumSize = Dimension(200, preferredSize.height)
                } else {
                    maximumSize = Dimension(200, preferredSize.height)
                }
            })
            add(Box.createHorizontalGlue())
        }
    }

    private fun setupListeners() {
        useDateRangeCheckBox.addActionListener {
            val enabled = useDateRangeCheckBox.isSelected
            startDateField.isEnabled = enabled
            endDateField.isEnabled = enabled
        }

        useAmountCheckBox.addActionListener {
            val enabled = useAmountCheckBox.isSelected
            commitAmountSpinner.isEnabled = enabled
            commitOrderComboBox.isEnabled = enabled
        }
    }

    private fun addNewFilter() {
        val name = nameField.text.trim()
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(
                contentPanel,
                "Please enter a filter name",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
            )
            return
        }

        // Validate dates if date range is selected
        var startDate: String? = null
        var endDate: String? = null

        if (useDateRangeCheckBox.isSelected) {
            val startText = startDateField.text.trim()
            val endText = endDateField.text.trim()

            if (startText.isNotEmpty()) {
                if (!isValidDate(startText)) {
                    JOptionPane.showMessageDialog(
                        contentPanel,
                        "Invalid start date format. Use yyyy-MM-dd",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE
                    )
                    return
                }
                startDate = startText
            }

            if (endText.isNotEmpty()) {
                if (!isValidDate(endText)) {
                    JOptionPane.showMessageDialog(
                        contentPanel,
                        "Invalid end date format. Use yyyy-MM-dd",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE
                    )
                    return
                }
                endDate = endText
            }
        }

        val amount = if (useAmountCheckBox.isSelected) commitAmountSpinner.value as Int else null
        val isLast = commitOrderComboBox.selectedIndex == 0

        val filter = CommitFilterConfig(
            name = name,
            startDate = startDate,
            endDate = endDate,
            commitAmount = amount,
            isLastCommits = isLast
        )

        filters.add(filter)
        updateFilterList()

        // Clear fields
        nameField.text = ""
        startDateField.text = ""
        endDateField.text = ""
        commitAmountSpinner.value = 10
        useDateRangeCheckBox.isSelected = false
        useAmountCheckBox.isSelected = false
        startDateField.isEnabled = false
        endDateField.isEnabled = false
        commitAmountSpinner.isEnabled = false
        commitOrderComboBox.isEnabled = false
    }

    private fun isValidDate(dateStr: String): Boolean {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd")
            format.isLenient = false
            format.parse(dateStr)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun updateFilterList() {
        filterListModel.clear()
        filters.forEach { filter ->
            filterListModel.addElement(formatFilterDisplay(filter))
        }
    }

    private fun formatFilterDisplay(filter: CommitFilterConfig): String {
        val parts = mutableListOf(filter.name)

        if (filter.startDate != null || filter.endDate != null) {
            val dateRange = buildString {
                append("Date: ")
                append(filter.startDate ?: "∞")
                append(" to ")
                append(filter.endDate ?: "∞")
            }
            parts.add(dateRange)
        }

        if (filter.commitAmount != null) {
            val amountStr = if (filter.isLastCommits) {
                "Last ${filter.commitAmount}"
            } else {
                "First ${filter.commitAmount}"
            }
            parts.add(amountStr)
        }

        return parts.joinToString(" | ")
    }

    fun getFilters(): List<CommitFilterConfig> = filters.toList()
}