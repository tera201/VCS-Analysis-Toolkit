package org.tera201.vcstoolkit.panels

import com.intellij.ui.JBColor
import com.intellij.ui.components.JBPanel
import com.intellij.util.ui.JBUI
import java.awt.Font
import javax.swing.BorderFactory

class TilePanel(title: String): JBPanel<JBPanel<*>>() {
    init {
        border = JBUI.Borders.compound(
            JBUI.Borders.empty(5),
            JBUI.Borders.compound(
                BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(JBColor.border(), 1, true),
                    title,
                    0,
                    2,
                    Font(Font.SANS_SERIF, Font.BOLD, 12),
                    JBColor.namedColor("Label.infoForeground", JBColor.GRAY)
                ),
                JBUI.Borders.empty(10)
            )
        )
    }
}