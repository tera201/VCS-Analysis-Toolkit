package org.tera201.vcstoolkit.services.colors

import com.intellij.ui.JBColor
import java.awt.Color

object PluginColors {
    val RED = JBColor(Color(248, 113, 113), Color(180, 60, 60))
    val ORANGE = JBColor(Color(251, 146, 60), Color(189, 96, 40))
    val YELLOW = JBColor(Color(251, 191, 36), Color(190, 140, 30))
    val LIGHT_GREEN = JBColor(Color(163, 230, 53), Color(120, 180, 40))
    val TEAL_GREEN = JBColor(Color(52, 211, 153), Color(30, 150, 100))
    val SKY_BLUE = JBColor(Color(34, 211, 238), Color(20, 150, 180))
    val LIGHT_PURPLE = JBColor(Color(129, 140, 248), Color(90, 100, 200))
    val PURPLE = JBColor(Color(192, 132, 252), Color(140, 80, 200))

    val ALL_COLORS = arrayOf(
        LIGHT_GREEN, RED, ORANGE, YELLOW, TEAL_GREEN, SKY_BLUE, LIGHT_PURPLE, PURPLE
    )
}