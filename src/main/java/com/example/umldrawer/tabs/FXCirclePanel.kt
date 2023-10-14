package com.example.umldrawer.tabs

import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.SceneAntialiasing
import javafx.scene.layout.StackPane
import org.tera201.InfoPane
import org.tera201.MainSubScene
import org.tera201.elements.ClassCircle
import org.tera201.elements.PackageCircle


class FXCirclePanel : JFXPanel() {

    companion object {
        var circleGroup = Group()
    }
    var infoPane = InfoPane()
    private val SCENE_WIDTH = 800.0
    private val SCENE_HEIGHT = 600.0

    init {
        Platform.runLater { initFXCircle(circleGroup) }
    }

    private fun initFXCircle(mainCircle: Group) {
        val packageCircle = PackageCircle("First", 2000.0, 1900.0, 100.0)
        val circle = ClassCircle("First", 500.0, 400.0, 100.0)
        val circle2 = ClassCircle("Second", 800.0, 400.0, 100.0)
        val circle3 = ClassCircle("3", 800.0, 400.0, 100.0)
        packageCircle.addCircle(circle)
        packageCircle.addCircle(circle2)
        packageCircle.addCircle(circle3)


        val packageCircle2 = PackageCircle("SecondPack", 600.0, 500.0, 100.0)
        val circle4 = ClassCircle("First", 300.0, 200.0, 100.0)
        packageCircle2.addCircle(circle4)
        packageCircle.addCircle(packageCircle2)

        mainCircle.children.add(packageCircle.group)

        val mainSubScene =
            MainSubScene(mainCircle, SCENE_WIDTH, SCENE_HEIGHT, true, SceneAntialiasing.BALANCED)
        val stackPane = StackPane(mainSubScene)
        InfoPane.setMainPane(stackPane)

        mainSubScene.heightProperty().bind(stackPane.heightProperty())
        mainSubScene.widthProperty().bind(stackPane.widthProperty())

        val scene2 = Scene(stackPane, SCENE_WIDTH, SCENE_HEIGHT, true, SceneAntialiasing.BALANCED)
        this.scene = scene2
    }
}