package org.tera201.vcstoolkit.info

import java.awt.*

/**
 * Responsive grid layout that automatically adjusts columns based on available width
 */
class ResponsiveGridLayout(
    private val preferredTileWidth: Int = 200,
    private val minColumns: Int = 1,
    private val maxColumns: Int = 4,
    private val hgap: Int = 10,
    private val vgap: Int = 10
) : LayoutManager {

    override fun addLayoutComponent(name: String?, comp: Component?) {}
    override fun removeLayoutComponent(comp: Component?) {}

    override fun preferredLayoutSize(parent: Container): Dimension {
        return calculateLayoutSize(parent, false)
    }

    override fun minimumLayoutSize(parent: Container): Dimension {
        return calculateLayoutSize(parent, true)
    }

    private fun calculateLayoutSize(parent: Container, useMinimum: Boolean): Dimension {
        val insets = parent.insets
        val components = parent.components.filter { it.isVisible }

        if (components.isEmpty()) {
            return Dimension(insets.left + insets.right, insets.top + insets.bottom)
        }

        val availableWidth = parent.width - insets.left - insets.right
        val cols = calculateColumns(availableWidth)
        val rows = (components.size + cols - 1) / cols

        val componentWidth = if (useMinimum) preferredTileWidth / 2 else preferredTileWidth
        val componentHeight = if (useMinimum) 100 else 120

        val width = cols * componentWidth + (cols - 1) * hgap + insets.left + insets.right
        val height = rows * componentHeight + (rows - 1) * vgap + insets.top + insets.bottom

        return Dimension(width, height)
    }

    private fun calculateColumns(availableWidth: Int): Int {
        if (availableWidth <= 0) return minColumns

        val possibleColumns = (availableWidth + hgap) / (preferredTileWidth + hgap)
        return possibleColumns.coerceIn(minColumns, maxColumns)
    }

    override fun layoutContainer(parent: Container) {
        val insets = parent.insets
        val components = parent.components.filter { it.isVisible }

        if (components.isEmpty()) return

        val availableWidth = parent.width - insets.left - insets.right
        val cols = calculateColumns(availableWidth)

        // Calculate actual tile width to fill available space
        val totalGapWidth = (cols - 1) * hgap
        val tileWidth = (availableWidth - totalGapWidth) / cols

        var x = insets.left
        var y = insets.top
        var col = 0

        for (component in components) {
            val preferredSize = component.preferredSize
            val tileHeight = preferredSize.height.coerceAtLeast(100)

            component.setBounds(x, y, tileWidth, tileHeight)

            col++
            if (col >= cols) {
                // Move to next row
                col = 0
                x = insets.left
                y += tileHeight + vgap
            } else {
                // Move to next column
                x += tileWidth + hgap
            }
        }
    }
}