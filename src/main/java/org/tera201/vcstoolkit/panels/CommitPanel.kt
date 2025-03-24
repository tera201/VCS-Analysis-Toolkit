package org.tera201.vcstoolkit.panels

import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import java.awt.Color
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.time.LocalDate
import java.time.Month
import java.time.Year
import java.time.format.TextStyle
import java.util.*
import javax.swing.BorderFactory

class CommitPanel internal constructor() : JBPanel<CommitPanel>() {
    private var year: Int = LocalDate.now().year
    private var daysInYear: Int = Year.of(year).length()
    private var offset: Int = LocalDate.ofYearDay(year, 1).getDayOfWeek().value - 1
    private var panels: Array<JBPanel<JBPanel<*>>?> = arrayOfNulls(366)
    private val dayCommit = IntArray(366)
    private val gbc: GridBagConstraints = GridBagConstraints()
    private var date: LocalDate = LocalDate.ofYearDay(year, 1)

    init {
        setLayout(GridBagLayout())
        gbc.fill = GridBagConstraints.BOTH
        gbc.weightx = 10.0
        gbc.weighty = 10.0

    }

    constructor(year: Int) : this() {
        updatePanel(year)
    }

    private fun clearPanels() = panels.forEachIndexed() { i, _ -> panels[i] = null }

    fun updatePanel(year: Int) {
        this.removeAll()
        clearPanels()
        date = LocalDate.ofYearDay(year, 1)
        dayCommit.forEachIndexed() { i, _ -> dayCommit[i] = 0 }
        daysInYear = Year.of(year).length()
        offset = date.getDayOfWeek().value - 1

        gbc.gridx = 0
        gbc.gridy = 0
        drawPanel()
    }

    private fun drawPanel() {

        // add labels for month
        addMonthLabels(date, gbc.clone() as GridBagConstraints)

        // add labels for day of week
        addDayLabels(gbc)

        //fill offset before 1st day
        for (i in 0 until offset) {
            gbc.gridx = 1
            gbc.gridy = i + 1
            val nullPanel = JBPanel<JBPanel<*>>()
            nullPanel.preferredSize = Dimension(1, 1)
            this.add(nullPanel, gbc.clone())
        }

        // Fill the calendar with day panels
        for (i in offset until daysInYear + offset) {
            val dayPanel =
                createDayPanel(date.month.getDisplayName(TextStyle.FULL, Locale.getDefault()), date.dayOfMonth)
            gbc.gridx = i / 7 + 1
            gbc.gridy = i % 7 + 1
            panels[i - offset] = dayPanel
            this.add(dayPanel, gbc.clone())
            date = date.plusDays(1)
        }
    }

    fun setCommitCountForDay(day: Int, commitCount: Int) {
        val date = LocalDate.ofYearDay(year, day)
        val panel = panels[day - 1] ?: return
        dayCommit[day - 1] = commitCount
        panel.background = getColorForCommits(dayCommit[day - 1])
        panel.toolTipText = "${commitCount} commits on ${
            date.month.getDisplayName(
                TextStyle.FULL,
                Locale.getDefault()
            )
        } ${date.dayOfMonth}"
    }

    fun addCommitCountForDay(day: Int, commitCount: Int) {
        val date = LocalDate.ofYearDay(year, day)
        val panel = panels[day - 1] ?: return
        dayCommit[day - 1] += commitCount
        panel.background = getColorForCommits(dayCommit[day - 1])
        panel.toolTipText = "${dayCommit[day - 1]} commits on ${
            date.month.getDisplayName(
                TextStyle.FULL,
                Locale.getDefault()
            )
        } ${date.dayOfMonth}"
    }

    private fun addDayLabels(gbc: GridBagConstraints) {
        gbc.gridx = 0
        listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEachIndexed { index, dayLabel ->
            gbc.gridy = index + 1
            add(JBLabel(dayLabel, JBLabel.CENTER).apply {
                verticalAlignment = JBLabel.NORTH
            }, gbc.clone())
        }
    }

    private fun addMonthLabels(date: LocalDate, gbc: GridBagConstraints) {
        val months = Month.entries.map { it.getDisplayName(TextStyle.SHORT, Locale.getDefault()) }
        val weeksPerMonths = IntArray(12)
        var currentMonth = 0
        weeksPerMonths[currentMonth] = 1
        var date = date.withDayOfYear(1).plusDays(8L - date.dayOfWeek.value.toLong())
        for (i in 0..52) {
            if (date.monthValue - 1 != currentMonth && currentMonth < 11) {
                currentMonth = date.monthValue - 1
                weeksPerMonths[currentMonth] = 0
            }
            weeksPerMonths[currentMonth]++
            date = date.plusWeeks(1)
        }

        gbc.gridx = 1
        gbc.gridwidth = weeksPerMonths[0]
        months.forEachIndexed { index, month ->
            if (index > 0) {
                gbc.gridx += weeksPerMonths[index - 1]
                gbc.gridwidth = weeksPerMonths[index]
            }
            add(JBLabel(month, JBLabel.LEFT), gbc)
        }
    }

    companion object {
        private fun createDayPanel(month: String, dayOfMonth: Int) = JBPanel<JBPanel<*>>().apply {
            preferredSize = Dimension(1, 1)
            toolTipText = "0 commits on $month $dayOfMonth"
            border = BorderFactory.createLineBorder(Color.DARK_GRAY)
            background = getColorForCommits(0)
        }

        private fun getColorForCommits(commits: Int): Color = when {
            commits > 15 -> Color(0, 200, 0)
            commits > 10 -> Color(0, 150, 0)
            commits > 5 -> Color(0, 100, 0)
            commits > 0 -> Color(0, 50, 0)
            else -> Color.LIGHT_GRAY
        }
    }
}
