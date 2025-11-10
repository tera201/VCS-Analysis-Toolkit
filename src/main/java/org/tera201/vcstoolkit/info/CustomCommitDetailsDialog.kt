package org.tera201.vcstoolkit.info

import com.intellij.diff.DiffContentFactory
import com.intellij.diff.DiffDialogHints
import com.intellij.diff.DiffManager
import com.intellij.diff.chains.SimpleDiffRequestChain
import com.intellij.diff.requests.SimpleDiffRequest
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.Gray
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import org.tera201.vcsmanager.db.entities.CommitEntity
import org.tera201.vcsmanager.scm.SCM
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.text.SimpleDateFormat
import java.util.*
import javax.swing.*

class CustomCommitDetailsDialog(
    private val project: Project,
    private val commits: List<CommitEntity>,
    private val title: String,
    private val scm: SCM,
    private val checkedCommits: MutableSet<String>,
) : DialogWrapper(project) {
    private var showFullMessage = false
    private var showAuthors = true
    private var showDates = true
    private var hideCheckedCommits = false
    private val messageToggleCheckbox = JBCheckBox("Show Full Message", showFullMessage)
    private val showAuthorsCheckbox = JBCheckBox("Show Authors", showAuthors)
    private val showDatesCheckbox = JBCheckBox("Show Dates", showDates)
    private val hideCheckedCommitsCheckbox = JBCheckBox("Hide Checked Commits", hideCheckedCommits)
    private val commitPanels = mutableListOf<CommitPanel>()

    init {
        init()
        setTitle(title)
    }

    override fun createCenterPanel(): JComponent {
        val mainPanel = JPanel(BorderLayout())

        val controlsPanel = JPanel(FlowLayout(FlowLayout.LEFT, 10, 5)).apply {
            add(JBLabel("Message Display:"))
            add(messageToggleCheckbox)
            add(showAuthorsCheckbox)
            add(showDatesCheckbox)
            add(hideCheckedCommitsCheckbox)
        }

        messageToggleCheckbox.addActionListener {
            showFullMessage = messageToggleCheckbox.isSelected
            commitPanels.forEach { it.updateMessageDisplay() }
        }

        showAuthorsCheckbox.addActionListener {
            showAuthors = showAuthorsCheckbox.isSelected
            commitPanels.forEach { it.updateVisibility() }
        }

        showDatesCheckbox.addActionListener {
            showDates = showDatesCheckbox.isSelected
            commitPanels.forEach { it.updateVisibility() }
        }

        hideCheckedCommitsCheckbox.addActionListener {
            hideCheckedCommits = hideCheckedCommitsCheckbox.isSelected
            commitPanels.forEach { it.updateVisibility() }
        }

        val contentPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = JBUI.Borders.empty(10)
        }

        commits.forEach { commit ->
            val commitPanel = CommitPanel(commit)
            commitPanels.add(commitPanel)
            contentPanel.add(commitPanel)
            contentPanel.add(Box.createVerticalStrut(5))
        }

        val scrollPane = JBScrollPane(contentPanel).apply {
            preferredSize = Dimension(900, 600)
        }

        mainPanel.add(controlsPanel, BorderLayout.NORTH)
        mainPanel.add(scrollPane, BorderLayout.CENTER)

        return mainPanel
    }

    override fun createActions() = arrayOf(okAction)

    inner class CommitPanel(private val commit: CommitEntity) : JPanel(BorderLayout()) {
        private val changesPanel = JPanel()
        private val messageLabel: JBLabel
        private val authorLabel = JBLabel("Author: ")
        private lateinit var authorValue: JComponent
        private  val dateLabel = JBLabel("Date: ")
        private lateinit var dateValue: JComponent

        init {
            border = JBUI.Borders.compound(
                BorderFactory.createLineBorder(JBColor.LIGHT_GRAY, 1),
                JBUI.Borders.empty(10)
            )
            background = JBColor.PanelBackground

            val headerPanel = JPanel(GridBagLayout()).apply {
                background = JBColor.PanelBackground
            }

            val gbc = GridBagConstraints().apply {
                anchor = GridBagConstraints.WEST
                insets = JBUI.insets(2)
                fill = GridBagConstraints.HORIZONTAL
            }
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent?) {
                    if (e?.clickCount == 2) {
                        openMultiFileDiff(project, scm.getCommitDiffByFiles(commit.hash))
                    }
                }
            })

            gbc.gridx = 0
            gbc.gridy = 0
            gbc.gridheight = 1
            gbc.weightx = 0.0
            headerPanel.add(JBLabel("Hash: "), gbc)

            gbc.gridx = 1
            gbc.weightx = 1.0
            headerPanel.add(JBLabel(commit.hash.take(8)), gbc)

            gbc.gridx = 2
            gbc.weightx = 0.0
            headerPanel.add(JBLabel("Stability: "), gbc)

            gbc.gridx = 3
            gbc.weightx = 0.0
            headerPanel.add(JBLabel(String.format("%.2f", commit.stability)), gbc)

            gbc.gridx = 4
            gbc.weightx = 0.0
            val checkedCheckbox = JBCheckBox("Checked", checkedCommits.contains(commit.hash)).apply {
                addActionListener {
                    if (isSelected) {
                        checkedCommits.add(commit.hash)
                    } else {
                        checkedCommits.remove(commit.hash)
                    }
                    updateVisibility()
                }
            }
            headerPanel.add(checkedCheckbox, gbc)

            gbc.gridx = 0
            gbc.gridy = 1
            gbc.weightx = 0.0
            headerPanel.add(authorLabel, gbc)

            gbc.gridx = 1
            gbc.gridwidth = 4
            gbc.weightx = 1.0
            authorValue = JBLabel(commit.authorName)
            headerPanel.add(authorValue, gbc)

            gbc.gridx = 0
            gbc.gridy = 2
            gbc.gridwidth = 1
            gbc.weightx = 0.0
            headerPanel.add(dateLabel, gbc)

            gbc.gridx = 1
            gbc.gridwidth = 4
            gbc.weightx = 1.0
            val date = Date(commit.date * 1000L)
            val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date)
            dateValue = JBLabel(dateStr)
            headerPanel.add(dateValue, gbc)

            gbc.gridx = 0
            gbc.gridy = 3
            gbc.gridwidth = 1
            gbc.weightx = 0.0
            headerPanel.add(JBLabel("Message: "), gbc)

            gbc.gridx = 1
            gbc.gridwidth = 4
            gbc.weightx = 1.0
            messageLabel = JBLabel(if (showFullMessage) commit.fullMessage else commit.shortMessage)
            headerPanel.add(messageLabel, gbc)

            add(headerPanel, BorderLayout.NORTH)

            changesPanel.layout = BoxLayout(changesPanel, BoxLayout.Y_AXIS)
            changesPanel.background = Gray._245
            changesPanel.isVisible = false
            add(changesPanel, BorderLayout.CENTER)
        }

        fun updateMessageDisplay() {
            messageLabel.text = if (showFullMessage) commit.fullMessage else commit.shortMessage
        }

        fun updateVisibility() {
            authorLabel.isVisible = showAuthors
            authorValue.isVisible = showAuthors
            dateLabel.isVisible = showDates
            dateValue.isVisible = showDates
            this.isVisible = !hideCheckedCommits || !checkedCommits.contains(commit.hash)
            revalidate()
            repaint()
        }

        fun openMultiFileDiff(project: Project, diffs: Map<String, Pair<String, String>>) {
            val contentFactory = DiffContentFactory.getInstance()

            // Create a list of DiffRequestProducers
            val request = diffs.map { (filePath, pair) ->
                val oldContent = contentFactory.create(project, pair.first)
                val newContent = contentFactory.create(project, pair.second)
                SimpleDiffRequest(
                    filePath,
                    oldContent,
                    newContent,
                    "Parent",
                    "Commit"
                )
            }
            val requestChain = SimpleDiffRequestChain(request)
            val hints = DiffDialogHints.DEFAULT

            DiffManager.getInstance().showDiff(project, requestChain, hints)
        }
    }
}