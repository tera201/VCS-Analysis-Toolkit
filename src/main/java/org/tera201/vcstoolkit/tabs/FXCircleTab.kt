package org.tera201.vcstoolkit.tabs

import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import org.tera201.vcstoolkit.panels.FXCirclePanel
import org.tera201.code2uml.util.messages.DataBaseUtil
import org.tera201.vcstoolkit.utils.toCircle
import java.awt.BorderLayout
import javax.swing.JPanel


class FXCircleTab(val tabManager: TabManager) : JPanel(), FXTab {
    val fxCircle = FXCirclePanel()
    init {
        this.layout = BorderLayout()
        this.add(fxCircle, BorderLayout.CENTER)
    }

    fun renderByModel(models: ArrayList<Int>, dataBaseUtil: DataBaseUtil) {
        Platform.runLater {
            fxCircle.circleSpace.clean()
            for (i in 0 until models.size) {
               toCircle(fxCircle.circleSpace, i, models[i], dataBaseUtil)
            }
            fxCircle.circleSpace.updateView(true)
        }
    }

    override fun setJFXPanel(panel:JFXPanel) {
        this.add(panel, BorderLayout.CENTER)
    }

    override fun setExpandMode() {
        TODO("Not yet implemented")
    }

    override fun setCollapseMode() {
        TODO("Not yet implemented")
    }
}