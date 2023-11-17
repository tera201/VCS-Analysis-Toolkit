package org.tera201.vcstoolkit.tabs

import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import org.tera201.vcstoolkit.panels.FXCirlePanel
import org.eclipse.uml2.uml.Model
import org.tera201.vcstoolkit.utils.toCircle
import java.awt.BorderLayout
import javax.swing.JPanel


class FXCircleTab : JPanel(), FXTab {
    var fxCircle = FXCirlePanel()
    init {
        this.layout = BorderLayout()
        this.add(fxCircle, BorderLayout.CENTER)
    }

    fun renderByModel(models: ArrayList<Model>) {
        Platform.runLater {
            fxCircle.circleSpace.clean()
            for (i in 0 until GitPanel.models.size) {
                models[i].toCircle(fxCircle.circleSpace, i)
            }
            fxCircle.circleSpace.updateView()
        }
    }

    override fun setJFXPanel(panel:JFXPanel) {
        this.add(panel, BorderLayout.CENTER)
    }
}