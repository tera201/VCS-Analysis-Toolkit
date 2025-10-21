package org.tera201.vcstoolkit.info

import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import javax.swing.*
import kotlin.math.abs

class SmoothTileDragHandler(
    private val tile: JPanel,
    private val container: JPanel
) : MouseAdapter() {

    private var dragStartPoint: Point? = null
    private var glassPane: JComponent? = null
    private var ghostLabel: JLabel? = null
    private var ghostOffset: Point? = null

    override fun mousePressed(e: MouseEvent) {
        dragStartPoint = e.point
    }

    override fun mouseDragged(e: MouseEvent) {
        if (glassPane == null) {
            startDragging(e)
        }
        updateGhostLocation(e)
    }

    override fun mouseReleased(e: MouseEvent) {
        stopDragging(e)
    }

    // --- helpers ---

    private fun startDragging(e: MouseEvent) {
        val rootPane = SwingUtilities.getRootPane(tile) ?: return
        val gp = rootPane.glassPane as? JComponent ?: return
        glassPane = gp
        gp.isVisible = true
        gp.layout = null

        // Capture an image of the tile
        val image = BufferedImage(tile.width, tile.height, BufferedImage.TYPE_INT_ARGB)
        val g2 = image.createGraphics()
        tile.printAll(g2)
        g2.dispose()

        val ghost = JLabel(ImageIcon(image)).apply {
            isOpaque = false
            putClientProperty("opacity", 0.4f) // 👈 More transparent
        }

        val tileLocation = SwingUtilities.convertPoint(tile.parent, tile.location, gp)
        ghost.setBounds(tileLocation.x, tileLocation.y, tile.width, tile.height)

        gp.add(ghost)
        gp.repaint()

        ghostLabel = ghost
        ghostOffset = e.point
    }

    private fun updateGhostLocation(e: MouseEvent) {
        val gp = glassPane ?: return
        val ghost = ghostLabel ?: return

        val glassPoint = SwingUtilities.convertPoint(tile, e.point, gp)
        val offset = ghostOffset ?: Point()
        val newX = glassPoint.x - offset.x
        val newY = glassPoint.y - offset.y
        ghost.setLocation(newX, newY)
        gp.repaint()
    }

    private fun stopDragging(e: MouseEvent) {
        val gp = glassPane ?: return
        val ghost = ghostLabel

        // Convert mouse point to container coordinates
        val glassPoint = SwingUtilities.convertPoint(tile, e.point, gp)
        val containerPoint = SwingUtilities.convertPoint(gp, glassPoint, container)

        // Helper: index of component that contains point, or null
        fun findContainingComponentIndex(pt: Point): Int? {
            for (i in 0 until container.componentCount) {
                val comp = container.getComponent(i)
                if (comp.bounds.contains(pt)) return i
            }
            return null
        }

        // Helper: find nearest component index by center distance
        fun findNearestComponentIndex(pt: Point): Int? {
            if (container.componentCount == 0) return null
            var bestIndex = 0
            var bestDistSq = Long.MAX_VALUE
            for (i in 0 until container.componentCount) {
                val comp = container.getComponent(i)
                val cx = comp.x + comp.width / 2
                val cy = comp.y + comp.height / 2
                val dx = (cx - pt.x).toLong()
                val dy = (cy - pt.y).toLong()
                val distSq = dx * dx + dy * dy
                if (distSq < bestDistSq) {
                    bestDistSq = distSq
                    bestIndex = i
                }
            }
            return bestIndex
        }

        // Find a candidate component index under or nearest the drop point
        val targetIndex = findContainingComponentIndex(containerPoint) ?: findNearestComponentIndex(containerPoint)

        val insertIndex = if (targetIndex == null) {
            // empty container -> append
            0
        } else {
            // Decide before/after relative to the target component
            val targetComp = container.getComponent(targetIndex)
            // Use both X and Y to decide for grid-like layouts:
            val verticalDecision = if (containerPoint.y < targetComp.y + targetComp.height / 2) -1 else 1
            val horizontalDecision = if (containerPoint.x < targetComp.x + targetComp.width / 2) -1 else 1

            // If layout is mainly vertical (single column) verticalDecision is used.
            // For multi-column grid, combine decisions: prefer horizontal for left/right moves,
            // fall back to vertical if nearer vertically.
            val dx = abs((targetComp.x + targetComp.width / 2) - containerPoint.x)
            val dy = abs((targetComp.y + targetComp.height / 2) - containerPoint.y)

            val beforeAfter = if (dx > dy) horizontalDecision else verticalDecision

            // If beforeAfter == -1 -> insert before targetIndex, else after (targetIndex + 1)
            if (beforeAfter < 0) targetIndex else targetIndex + 1
        }

        // Remove and insert safely (account for componentCount change)
        container.remove(tile)
        val safeIndex = insertIndex.coerceIn(0, container.componentCount)
        container.add(tile, safeIndex)
        container.revalidate()
        container.repaint()

        // Clean up ghost fully
        if (ghost != null) {
            gp.remove(ghost)
            ghostLabel = null
        }
        gp.repaint()
        glassPane = null
    }

}