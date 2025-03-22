package org.tera201.vcstoolkit.helpers

import com.intellij.ui.components.JBLabel
import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridLayoutManager
import com.intellij.util.ui.FormBuilder
import java.awt.Cursor
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.JPanel

internal fun JPanel.addComponentPairRow(i: Int, first: JComponent, second: JComponent) {
    this.add(first, GridConstraints().apply { row = i; column = 0})
    this.add(second, GridConstraints().apply { row = i; column = 1; fill = GridConstraints.FILL_HORIZONTAL })
}

internal fun JPanel.addNComponentsRow(i: Int, vararg components: Pair<JComponent, Boolean>) {
    components.forEachIndexed { col, (component, shouldFill) ->
        this.add(component, GridConstraints().apply {
            row = i
            column = col
            vSizePolicy = GridConstraints.SIZEPOLICY_FIXED
            hSizePolicy = GridConstraints.SIZEPOLICY_CAN_SHRINK
            if (shouldFill) {
                fill = GridConstraints.FILL_HORIZONTAL
                hSizePolicy = GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW
            }
        })
    }
}

fun FormBuilder.addCenteredLabel(text: String): FormBuilder {
    return addComponent(JPanel(GridLayoutManager(1,1))
        .apply {
            add(JBLabel(text), GridConstraints().apply { anchor = GridConstraints.ANCHOR_CENTER })
               })

}

fun FormBuilder.addCenteredComponent(component: JComponent): FormBuilder {
    return addComponent(JPanel(GridLayoutManager(1,1))
        .apply {
            add(component, GridConstraints().apply { anchor = GridConstraints.ANCHOR_CENTER })
        })
}

fun FormBuilder.addComponentToLeft(component: JComponent): FormBuilder {
    return addComponent(JPanel(GridLayoutManager(1,1))
        .apply {
            add(component, GridConstraints().apply { anchor = GridConstraints.ANCHOR_WEST })
        })
}

fun JBLabel.setTextWithShortener(text: String, width: Int) {
    val shortenedText = text.take(width) + "..."
    this.text = shortenedText
    this.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
    this.addMouseListener(object : MouseAdapter() {
        override fun mouseClicked(e: MouseEvent) {
            this@setTextWithShortener.text =
                if (this@setTextWithShortener.text == text) shortenedText else text
        }
    })
}