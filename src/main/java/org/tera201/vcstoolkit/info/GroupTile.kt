package org.tera201.vcstoolkit.info

import com.formdev.flatlaf.FlatClientProperties
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import java.awt.BorderLayout
import java.awt.Cursor
import java.awt.Dimension
import java.awt.Font
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.SwingConstants

class GroupTile(groupData: GroupData): JBPanel<JBPanel<*>>() {
    val editButton = JButton("✎").apply {
        toolTipText = "Edit group"
        preferredSize = Dimension(24, 24)
        cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        foreground = JBColor.BLUE
        putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_BORDERLESS)

    }

    val removeButton = JButton("×").apply {
        toolTipText = "Remove group"
        preferredSize = Dimension(24, 24)
        cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        foreground = JBColor.RED
        putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_BORDERLESS)

    }

    val buttonsPanel = JBPanel<JBPanel<*>>().apply {
        layout = BoxLayout(this, BoxLayout.X_AXIS)
        isOpaque = false
        add(editButton)
        add(Box.createHorizontalStrut(2))
        add(removeButton)
    }

    val titleLabel = JBLabel(groupData.name).apply {
        font = font.deriveFont(Font.BOLD, 13f)
    }

    val topPanel = JBPanel<JBPanel<*>>().apply {
        layout = BorderLayout()
        isOpaque = false
        add(titleLabel, BorderLayout.CENTER)
        add(buttonsPanel, BorderLayout.EAST)
    }

    // Center section with count
    val countLabel = JBLabel(groupData.count).apply {
        font = font.deriveFont(Font.BOLD, 28f)
        foreground = JBColor.BLUE
        horizontalAlignment = SwingConstants.CENTER
    }

    // Bottom section with group by info
    val groupByLabel = JBLabel("Grouped by: ${groupData.getGroupByDescription()}").apply {
        font = font.deriveFont(Font.PLAIN, 10f)
        foreground = JBColor.GRAY
        horizontalAlignment = SwingConstants.CENTER
    }

    fun data() = this.getClientProperty("groupData") as? GroupData

    init {
        layout = BorderLayout(5, 5)
        border = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(JBColor.border(), 1, true),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        )
        cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        preferredSize = Dimension(200, 120)
        minimumSize = Dimension(150, 100)

        putClientProperty(FlatClientProperties.STYLE, "arc:8")
        // Store the group data for editing
        putClientProperty("groupData", groupData)

        // Store count label for updates
        putClientProperty("countLabel", countLabel)
        putClientProperty("groupByLabel", groupByLabel)

        add(topPanel, BorderLayout.NORTH)
        add(countLabel, BorderLayout.CENTER)
        add(groupByLabel, BorderLayout.SOUTH)
    }

    fun addRemoveButtonAction(unit: (JPanel, existingData: GroupData) -> Unit) {
        removeButton.addActionListener { data()?.let { data -> unit(this, data) } }
    }

    fun addEditButtonAction(unit: (tile: JPanel, existingData: GroupData) -> Unit) {
        editButton.addActionListener { data()?.let { data -> unit(this, data) } }
    }

    fun addDragHandler(groupTilesContainer: JBPanel<JBPanel<*>>) {
        val dragHandler = SmoothTileDragHandler(this, groupTilesContainer)
        addMouseListener(dragHandler)
        addMouseMotionListener(dragHandler)
    }

    fun addMouseClickAction(unit: (dataName: String, description: String) -> Unit) {
        addMouseListener(object : MouseAdapter() {
            override fun mouseEntered(e: MouseEvent) {
                border = BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(JBColor.BLUE, 2, true),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
                )
            }

            override fun mouseExited(e: MouseEvent) {
                border = BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(JBColor.border(), 1, true),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
                )
            }

            override fun mouseClicked(e: MouseEvent) {
                data()?.let { data -> unit(data.name, data.getGroupByDescription()) }
            }
        })
    }

}