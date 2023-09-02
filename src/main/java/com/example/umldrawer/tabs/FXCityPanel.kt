package com.example.umldrawer.tabs

import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.embed.swing.JFXPanel
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.SceneAntialiasing
import javafx.scene.layout.BorderPane
import org.example.MainSubScene
import org.example.elements.Building
import org.example.elements.City
import org.example.elements.Quarter


public fun createFXCity(): JFXPanel? {
    val fxPanel: JFXPanel = object : JFXPanel() {}
    Platform.runLater { initFXCity(fxPanel) }
    return fxPanel
}

private fun initFXCity(fxPanel: JFXPanel) {
    val sceneWidth = 800.0
    val sceneHeight = 600.0
    val city = City(8000.0, 20.0, 8000.0)
    val quarter = Quarter(500.0, 10.0, 500.0, 50.0)
    quarter.setPosition(0.0, 0.0)
    val building1 = Building(100.0, 900.0, 100.0)
    val building2 = Building(100.0, 700.0, 100.0)
    val building3 = Building(100.0, 600.0, 100.0)
    val building4 = Building(100.0, 600.0, 100.0)
    val building5 = Building(200.0, 600.0, 200.0)
    val building6 = Building(50.0, 600.0, 50.0)
    val building7 = Building(100.0, 600.0, 100.0)
    val building8 = Building(100.0, 600.0, 100.0)
    building1.setNotes("dawfawfwa")
    quarter.addAllBuildings(building1, building2, building3, building4, building5, building6, building7, building8)
    city.addQuarter(quarter)
    val mainSubScene = MainSubScene(city, sceneWidth, sceneHeight, true, SceneAntialiasing.BALANCED)
    val borderPane = BorderPane()
    borderPane.center = mainSubScene
    mainSubScene.heightProperty().bind(borderPane.heightProperty())
    mainSubScene.widthProperty().bind(borderPane.widthProperty())
    val scene2 = Scene(borderPane, sceneWidth, sceneHeight, true, SceneAntialiasing.BALANCED)
    scene2.focusOwnerProperty().addListener(object : ChangeListener<Node?> {
        override fun changed(observable: ObservableValue<out Node?>?, oldValue: Node?, newValue: Node?) {
            println("focus owner: $newValue")
        }
    })
    fxPanel.scene = scene2
}