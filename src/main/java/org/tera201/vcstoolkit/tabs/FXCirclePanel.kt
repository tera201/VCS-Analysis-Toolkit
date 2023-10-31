package org.tera201.vcstoolkit.tabs

import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.scene.SceneAntialiasing
import javafx.scene.layout.StackPane
import org.tera201.InfoPane
import org.tera201.MainSubScene
import org.tera201.SelectionManager
import org.tera201.elements.FXSpace
import org.tera201.elements.circle.ClassCircle
import org.tera201.elements.circle.HollowCylinder
import org.tera201.elements.circle.PackageCircle


class FXCirclePanel : JFXPanel() {

    companion object {
        var circleSpace = FXSpace<HollowCylinder>()
    }
    private val SCENE_WIDTH = 800.0
    private val SCENE_HEIGHT = 600.0

    init {
        Platform.runLater { initFXCircle(circleSpace) }
    }

    private fun initFXCircle(circleSpace: FXSpace<HollowCylinder>) {
        val packageCircle = PackageCircle("First", 2000.0, 1900.0, 100.0)
        val circle = ClassCircle("First", 500.0, 400.0, 100.0)
        val circle2 = ClassCircle("Second", 800.0, 400.0, 100.0)
        val circle3 = ClassCircle("3", 800.0, 400.0, 100.0)
        packageCircle.addObject(circle)
        packageCircle.addObject(circle2)
        packageCircle.addObject(circle3)


        val packageCircle2 = PackageCircle("SecondPack", 600.0, 500.0, 100.0)
        val circle4 = ClassCircle("First", 300.0, 200.0, 100.0)
        packageCircle2.addObject(circle4)
        packageCircle.addObject(packageCircle2)

        circleSpace.add(packageCircle)

        val mainSubScene =
            MainSubScene(circleSpace, SCENE_WIDTH, SCENE_HEIGHT, true, SceneAntialiasing.BALANCED)
        val stackPane = StackPane(mainSubScene)
        val infoPane = InfoPane()
        infoPane.mainPane = stackPane
        val selectionManager = SelectionManager(infoPane)
        circleSpace.setSelectionManager(selectionManager)

        mainSubScene.heightProperty().bind(stackPane.heightProperty())
        mainSubScene.widthProperty().bind(stackPane.widthProperty())

        val scene2 = Scene(stackPane, SCENE_WIDTH, SCENE_HEIGHT, true, SceneAntialiasing.BALANCED)
        this.scene = scene2
    }
}