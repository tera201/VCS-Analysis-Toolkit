package com.example.umldrawer.tabs

import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.scene.SceneAntialiasing
import javafx.scene.layout.StackPane
import org.tera201.InfoPane
import org.tera201.MainSubScene
import org.tera201.SelectionManager
import org.tera201.elements.FXSpace
import org.tera201.elements.city.Building
import org.tera201.elements.city.City
import org.tera201.elements.city.Quarter
import java.util.*


class FXCityPanel : JFXPanel() {

    companion object {
        var citySpace = FXSpace<Quarter>()
    }
    var rand = Random()
    private val SCENE_WIDTH = 800.0
    private val SCENE_HEIGHT = 600.0

    init {
        Platform.runLater { initFXCity(citySpace) }

//         TODO: remove this listener
//        this.addComponentListener(object : ComponentAdapter() {
//            override fun componentShown(e: ComponentEvent?) {
//                FXCityPanel()
//            }
//        })
    }

    private fun initFXCity(fxSpace: FXSpace<Quarter>) {
        val sceneWidth = 800.0
        val sceneHeight = 600.0
        var city = City(8000.0, 20.0, 8000.0)

        createQuarters(60, city)
        fxSpace.add(city)

        val mainSubScene =
            MainSubScene(fxSpace, SCENE_WIDTH, SCENE_HEIGHT, true, SceneAntialiasing.BALANCED)
        val stackPane = StackPane(mainSubScene)
        val infoPane = InfoPane()
        infoPane.mainPane = stackPane
        val selectionManager = SelectionManager(infoPane)
        fxSpace.selectionManager = selectionManager

        mainSubScene.heightProperty().bind(stackPane.heightProperty())
        mainSubScene.widthProperty().bind(stackPane.widthProperty())

        val scene2 = Scene(stackPane, sceneWidth, sceneHeight, true, SceneAntialiasing.BALANCED)
        this.scene = scene2
        city.updateView()
    }

    fun createQuarters(n: Int, city: City) {
        for (i in 0 until n) {
            val quarter = Quarter("$i quarter", 500.0, 10.0, 500.0, 50.0)
            createBuildings(randomValue(), quarter)
            city.addObject(quarter)
        }
    }

    fun createBuildings(n: Int, quarter: Quarter) {
        for (i in 0 until n) {
            val building1 =
                Building("$i", 200 * randomDValue(), 900 * randomDValue(), 200 * randomDValue())
            quarter.addObject(building1)
        }
    }

    private fun randomDValue(): Double {
        return rand.nextDouble(0.2, 1.0)
    }

    private fun randomValue(): Int {
        return rand.nextInt(4, 13)
    }
}