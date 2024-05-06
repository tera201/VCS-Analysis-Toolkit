package org.tera201.vcstoolkit.tabs

import com.intellij.openapi.ui.ComboBox
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import org.eclipse.uml2.uml.Model
import org.tera201.vcstoolkit.helpers.SharedModel
import org.tera201.vcstoolkit.panels.FXCityPanel
import org.tera201.vcstoolkit.utils.toCity
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.JPanel
import kotlin.jvm.optionals.getOrNull


class FXCityTab(private val tabManager: TabManager, modelListContent:SharedModel) : JPanel(), FXTab {
    val modelComboBox = ComboBox(modelListContent)
    private var model: Int? = null
    val fxCity:FXCityPanel
    private val topPanel = JPanel()

    init {
        this.layout = BorderLayout()
        topPanel.layout = FlowLayout(FlowLayout.LEFT)
        topPanel.add(modelComboBox)
        this.add(topPanel, BorderLayout.NORTH)
        fxCity = FXCityPanel()
        this.add(fxCity, BorderLayout.CENTER)
        // TODO: help with render javafx after plugin hide state - have no idea how
//        FXCityPanel()

        modelComboBox.addActionListener {
            if (modelComboBox.selectedItem != null) {
                val selectedModelName = modelComboBox.selectedItem as String
                val gitTab = tabManager.getTabMap()[TabEnum.GIT] as GitTab
                model = gitTab.modelsIdMap.getOrDefault(selectedModelName, null)
                Platform.runLater {
                if (model != null) {
                        fxCity.citySpace.clean()
                        toCity(fxCity.citySpace, model!!, gitTab.dataBaseUtil)
                        fxCity.citySpace.updateView()
                    }
                }
            }
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