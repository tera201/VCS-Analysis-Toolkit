
package org.tera201.vcstoolkit.info

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.TextBrowseFolderListener
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import java.awt.Component
import java.awt.Dimension
import java.awt.Font
import java.util.regex.PatternSyntaxException
import javax.swing.*

data class GroupData(
    var name: String = "",
    var filePath: String? = null,
    var fileType: String? = null,
    var commitMessageRegex: String? = null,
    var changesRegex: String? = null,
    var count: String = "0"
) {
    fun getGroupByDescription(): String {
        val criteria = mutableListOf<String>()

        if (!filePath.isNullOrBlank()) {
            criteria.add("Path: $filePath")
        }
        if (!fileType.isNullOrBlank()) {
            criteria.add("Type: $fileType")
        }
        if (!commitMessageRegex.isNullOrBlank()) {
            criteria.add("Message: ${commitMessageRegex!!.take(20)}${if (commitMessageRegex!!.length > 20) "..." else ""}")
        }
        if (!changesRegex.isNullOrBlank()) {
            criteria.add("Changes: ${changesRegex!!.take(20)}${if (changesRegex!!.length > 20) "..." else ""}")
        }

        return if (criteria.isEmpty()) "No filters" else criteria.joinToString(", ")
    }
}

class AddGroupDialog(
    parent: Component,
    private val editMode: Boolean = false,
    private val existingData: GroupData? = null
) : DialogWrapper(parent, true) {
    private val nameField = JBTextField(30)

    // Filter fields with checkboxes to enable/disable
    private val useFilePathCheckBox = JBCheckBox("Filter by File Path")
    private val filePathField = TextFieldWithBrowseButton().apply {
        addBrowseFolderListener(
            TextBrowseFolderListener(FileChooserDescriptor(
                true, true,
                false, false, false, false
            ).apply { title = "Select File or Directory" })
        )
    }

    private val useFileTypeCheckBox = JBCheckBox("Filter by File Type")
    private val fileTypeField = JBTextField(30).apply {
        toolTipText = "e.g., .java, .kt, .xml (comma-separated for multiple)"
    }

    private val useCommitMessageCheckBox = JBCheckBox("Filter by Commit Message Pattern")
    private val commitMessageRegexField = JBTextField(30).apply {
        toolTipText = "Regular expression to match commit messages"
    }
    private val commitMessageExampleLabel = JBLabel().apply {
        font = font.deriveFont(Font.ITALIC, 10f)
        foreground = JBColor.GRAY
    }

    private val useChangesCheckBox = JBCheckBox("Filter by Changes Pattern")
    private val changesRegexField = JBTextField(30).apply {
        toolTipText = "Regular expression to match code changes in diffs"
    }
    private val changesExampleLabel = JBLabel().apply {
        font = font.deriveFont(Font.ITALIC, 10f)
        foreground = JBColor.GRAY
    }

    init {
        title = if (editMode) "Edit Commit Group" else "Add New Commit Group"

        // Populate fields if in edit mode
        if (editMode && existingData != null) {
            populateFields(existingData)
        }

        init()
        setupListeners()
        updateFieldStates()
    }

    private fun populateFields(data: GroupData) {
        nameField.text = data.name

        if (!data.filePath.isNullOrBlank()) {
            useFilePathCheckBox.isSelected = true
            filePathField.text = data.filePath.toString()
        }

        if (!data.fileType.isNullOrBlank()) {
            useFileTypeCheckBox.isSelected = true
            fileTypeField.text = data.fileType
        }

        if (!data.commitMessageRegex.isNullOrBlank()) {
            useCommitMessageCheckBox.isSelected = true
            commitMessageRegexField.text = data.commitMessageRegex
        }

        if (!data.changesRegex.isNullOrBlank()) {
            useChangesCheckBox.isSelected = true
            changesRegexField.text = data.changesRegex
        }
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = JBUI.Borders.empty(10)
        }

        // Name field
        panel.add(JBLabel("Group Name:").apply {
            alignmentX = Component.LEFT_ALIGNMENT
            font = font.deriveFont(Font.BOLD)
        })
        panel.add(Box.createVerticalStrut(5))
        panel.add(nameField.apply {
            alignmentX = Component.LEFT_ALIGNMENT
            maximumSize = Dimension(Int.MAX_VALUE, preferredSize.height)
        })

        panel.add(Box.createVerticalStrut(20))

        // Separator
        panel.add(JSeparator().apply {
            alignmentX = Component.LEFT_ALIGNMENT
            maximumSize = Dimension(Int.MAX_VALUE, 2)
        })

        panel.add(Box.createVerticalStrut(15))

        panel.add(JBLabel("Filter Criteria (select one or more):").apply {
            alignmentX = Component.LEFT_ALIGNMENT
            font = font.deriveFont(Font.BOLD)
        })

        panel.add(Box.createVerticalStrut(15))

        // File Path filter
        panel.add(useFilePathCheckBox.apply {
            alignmentX = Component.LEFT_ALIGNMENT
        })
        panel.add(Box.createVerticalStrut(5))
        panel.add(createIndentedField(filePathField))

        panel.add(Box.createVerticalStrut(15))

        // File Type filter
        panel.add(useFileTypeCheckBox.apply {
            alignmentX = Component.LEFT_ALIGNMENT
        })
        panel.add(Box.createVerticalStrut(5))
        panel.add(createIndentedField(fileTypeField))
        panel.add(Box.createVerticalStrut(3))
        panel.add(JBLabel("Example: .java, .kt, .xml").apply {
            font = font.deriveFont(Font.ITALIC, 10f)
            foreground = JBColor.GRAY
            alignmentX = Component.LEFT_ALIGNMENT
            border = JBUI.Borders.emptyLeft(25)
        })

        panel.add(Box.createVerticalStrut(15))

        // Commit Message Regex filter
        panel.add(useCommitMessageCheckBox.apply {
            alignmentX = Component.LEFT_ALIGNMENT
        })
        panel.add(Box.createVerticalStrut(5))
        panel.add(createIndentedField(commitMessageRegexField))
        panel.add(Box.createVerticalStrut(3))
        panel.add(commitMessageExampleLabel.apply {
            text = "Example: ^(feat|fix|docs):.*"
            alignmentX = Component.LEFT_ALIGNMENT
            border = JBUI.Borders.emptyLeft(25)
        })

        panel.add(Box.createVerticalStrut(15))

        // Changes Regex filter
        panel.add(useChangesCheckBox.apply {
            alignmentX = Component.LEFT_ALIGNMENT
        })
        panel.add(Box.createVerticalStrut(5))
        panel.add(createIndentedField(changesRegexField))
        panel.add(Box.createVerticalStrut(3))
        panel.add(changesExampleLabel.apply {
            text = "Example: .*TODO.*|.*FIXME.*"
            alignmentX = Component.LEFT_ALIGNMENT
            border = JBUI.Borders.emptyLeft(25)
        })

        panel.add(Box.createVerticalStrut(15))

        // Info note
        panel.add(Box.createVerticalGlue())
        panel.add(createInfoPanel())

        return JScrollPane(panel).apply {
            border = null
            preferredSize = Dimension(550, 550)
        }
    }

    private fun createIndentedField(field: JComponent): JPanel {
        return JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            alignmentX = Component.LEFT_ALIGNMENT
            maximumSize = Dimension(Int.MAX_VALUE, 30)
            border = JBUI.Borders.emptyLeft(25)

            add(field.apply {
                maximumSize = Dimension(Int.MAX_VALUE, preferredSize.height)
            })
        }
    }

    private fun createInfoPanel(): JPanel {
        return JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            alignmentX = Component.LEFT_ALIGNMENT
            border = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(JBColor.BLUE.darker(), 1),
                JBUI.Borders.empty(10)
            )
            background = JBColor.namedColor("Panel.background", JBColor.WHITE)
            maximumSize = Dimension(Int.MAX_VALUE, 100)

            add(JBLabel("ℹ Info").apply {
                font = font.deriveFont(Font.BOLD, 12f)
                foreground = JBColor.BLUE.darker()
                alignmentX = Component.LEFT_ALIGNMENT
            })
            add(Box.createVerticalStrut(5))
            add(JBLabel("• At least one filter criterion must be selected").apply {
                font = font.deriveFont(Font.PLAIN, 11f)
                alignmentX = Component.LEFT_ALIGNMENT
            })
            add(JBLabel("• Multiple criteria can be combined (AND logic)").apply {
                font = font.deriveFont(Font.PLAIN, 11f)
                alignmentX = Component.LEFT_ALIGNMENT
            })
            add(JBLabel("• Regex patterns are case-sensitive").apply {
                font = font.deriveFont(Font.PLAIN, 11f)
                alignmentX = Component.LEFT_ALIGNMENT
            })
        }
    }

    private fun setupListeners() {
        useFilePathCheckBox.addActionListener { updateFieldStates() }
        useFileTypeCheckBox.addActionListener { updateFieldStates() }
        useCommitMessageCheckBox.addActionListener { updateFieldStates() }
        useChangesCheckBox.addActionListener { updateFieldStates() }

        // Add validation listeners for regex fields
        commitMessageRegexField.document.addDocumentListener(object : javax.swing.event.DocumentListener {
            override fun insertUpdate(e: javax.swing.event.DocumentEvent?) = validateRegex()
            override fun removeUpdate(e: javax.swing.event.DocumentEvent?) = validateRegex()
            override fun changedUpdate(e: javax.swing.event.DocumentEvent?) = validateRegex()

            private fun validateRegex() {
                if (useCommitMessageCheckBox.isSelected && commitMessageRegexField.text.isNotBlank()) {
                    try {
                        commitMessageRegexField.text.toRegex()
                        commitMessageRegexField.foreground = JBColor.foreground()
                        commitMessageExampleLabel.foreground = JBColor.GRAY
                        commitMessageExampleLabel.text = "Example: ^(feat|fix|docs):.*"
                    } catch (e: PatternSyntaxException) {
                        commitMessageRegexField.foreground = JBColor.RED
                        commitMessageExampleLabel.foreground = JBColor.RED
                        commitMessageExampleLabel.text = "Invalid regex pattern!"
                    }
                }
            }
        })

        changesRegexField.document.addDocumentListener(object : javax.swing.event.DocumentListener {
            override fun insertUpdate(e: javax.swing.event.DocumentEvent?) = validateRegex()
            override fun removeUpdate(e: javax.swing.event.DocumentEvent?) = validateRegex()
            override fun changedUpdate(e: javax.swing.event.DocumentEvent?) = validateRegex()

            private fun validateRegex() {
                if (useChangesCheckBox.isSelected && changesRegexField.text.isNotBlank()) {
                    try {
                        changesRegexField.text.toRegex()
                        changesRegexField.foreground = JBColor.foreground()
                        changesExampleLabel.foreground = JBColor.GRAY
                        changesExampleLabel.text = "Example: .*TODO.*|.*FIXME.*"
                    } catch (e: PatternSyntaxException) {
                        changesRegexField.foreground = JBColor.RED
                        changesExampleLabel.foreground = JBColor.RED
                        changesExampleLabel.text = "Invalid regex pattern!"
                    }
                }
            }
        })
    }

    private fun updateFieldStates() {
        filePathField.isEnabled = useFilePathCheckBox.isSelected
        fileTypeField.isEnabled = useFileTypeCheckBox.isSelected
        commitMessageRegexField.isEnabled = useCommitMessageCheckBox.isSelected
        changesRegexField.isEnabled = useChangesCheckBox.isSelected
    }

    fun getGroupData(): GroupData {
        return GroupData(
            name = nameField.text.trim().ifEmpty { "Unnamed Group" },
            filePath = if (useFilePathCheckBox.isSelected) filePathField.text.trim().ifBlank { null } else null,
            fileType = if (useFileTypeCheckBox.isSelected) fileTypeField.text.trim().ifBlank { null } else null,
            commitMessageRegex = if (useCommitMessageCheckBox.isSelected) commitMessageRegexField.text.trim().ifBlank { null } else null,
            changesRegex = if (useChangesCheckBox.isSelected) changesRegexField.text.trim().ifBlank { null } else null,
            count = if (editMode && existingData != null) existingData.count else "0"
        )
    }

    override fun doOKAction() {
        // Validate name
        if (nameField.text.isBlank()) {
            JOptionPane.showMessageDialog(
                contentPanel,
                "Please enter a group name",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
            )
            return
        }

        // Validate at least one filter is selected
        if (!useFilePathCheckBox.isSelected &&
            !useFileTypeCheckBox.isSelected &&
            !useCommitMessageCheckBox.isSelected &&
            !useChangesCheckBox.isSelected) {
            JOptionPane.showMessageDialog(
                contentPanel,
                "Please select at least one filter criterion",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
            )
            return
        }

        // Validate selected filters have values
        if (useFilePathCheckBox.isSelected && filePathField.text.isBlank()) {
            JOptionPane.showMessageDialog(
                contentPanel,
                "Please enter a file path or disable the file path filter",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
            )
            return
        }

        if (useFileTypeCheckBox.isSelected && fileTypeField.text.isBlank()) {
            JOptionPane.showMessageDialog(
                contentPanel,
                "Please enter file types or disable the file type filter",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
            )
            return
        }

        if (useCommitMessageCheckBox.isSelected && commitMessageRegexField.text.isBlank()) {
            JOptionPane.showMessageDialog(
                contentPanel,
                "Please enter a commit message pattern or disable the filter",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
            )
            return
        }

        if (useChangesCheckBox.isSelected && changesRegexField.text.isBlank()) {
            JOptionPane.showMessageDialog(
                contentPanel,
                "Please enter a changes pattern or disable the filter",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
            )
            return
        }

        // Validate regex patterns
        if (useCommitMessageCheckBox.isSelected) {
            try {
                commitMessageRegexField.text.toRegex()
            } catch (e: PatternSyntaxException) {
                JOptionPane.showMessageDialog(
                    contentPanel,
                    "Invalid commit message regex pattern: ${e.message}",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE
                )
                return
            }
        }

        if (useChangesCheckBox.isSelected) {
            try {
                changesRegexField.text.toRegex()
            } catch (e: PatternSyntaxException) {
                JOptionPane.showMessageDialog(
                    contentPanel,
                    "Invalid changes regex pattern: ${e.message}",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE
                )
                return
            }
        }

        super.doOKAction()
    }
}