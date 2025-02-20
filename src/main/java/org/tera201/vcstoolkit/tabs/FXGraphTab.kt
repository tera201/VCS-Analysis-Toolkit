package org.tera201.vcstoolkit.tabs

import com.intellij.icons.AllIcons
import com.intellij.openapi.ui.ComboBox
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import org.eclipse.uml2.uml.Model
import org.tera201.code2uml.util.messages.DataBaseUtil
import org.tera201.umlgraph.graph.Graph
import org.tera201.umlgraph.graphview.UMLGraphControlPanel
import org.tera201.umlgraph.graphview.UMLGraphPanel
import org.tera201.umlgraph.graphview.arrows.ArrowTypes
import org.tera201.umlgraph.graphview.strategy.PlacementStrategy
import org.tera201.umlgraph.graphview.vertices.elements.ElementTypes
import org.tera201.vcstoolkit.helpers.SharedModel
import org.tera201.vcstoolkit.utils.toClass
import org.tera201.vcstoolkit.utils.toGraph
import org.tera201.vcstoolkit.utils.toPackage
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.JButton
import javax.swing.JPanel

class FXGraphTab(private val tabManager: TabManager, modelListContent:SharedModel): JPanel() {

    var umlGraphPanel: UMLGraphPanel<Int, String>? = null
    private val fxPanel: JFXPanel = object : JFXPanel() {}
    private val topPanel = JPanel()
    private var umlGraphControlPanel: UMLGraphControlPanel<Int, String>? = null
    private val modelComboBox = ComboBox(modelListContent)
    private var model:Int? = null
    private val packageButton = JButton("Package")
    private val classButton = JButton("Class")
    private var gitTab: GitTab? = null

    init {
        this.layout = BorderLayout()
        initFXGraph()
    }

    private fun initFXGraph() {
        topPanel.layout = FlowLayout(FlowLayout.LEFT)
        classButton.icon = AllIcons.Nodes.Class
        packageButton.icon = AllIcons.Nodes.Package
        topPanel.add(packageButton)
        topPanel.add(classButton)
        topPanel.add(modelComboBox)

        modelComboBox.addActionListener {
            if (modelComboBox.selectedItem != null) {
                val selectedModelName = modelComboBox.selectedItem as String
                gitTab = tabManager.getTabMap()[TabEnum.GIT] as GitTab
                model = gitTab!!.modelsIdMap.getOrDefault(selectedModelName, null)
            }
        }

        packageButton.addActionListener {
            if (model != null) {
                Platform.runLater {
                    umlGraphPanel?.setGraph(build_package_graph(model!!, gitTab!!.dataBaseUtil))
                }
                umlGraphPanel?.update()
                umlGraphControlPanel?.update()
            }
        }

        classButton.addActionListener {
            if (model != null) {
                Platform.runLater {
                    umlGraphPanel?.setGraph(build_class_graph(model!!, gitTab!!.dataBaseUtil))
                }
                umlGraphPanel?.update()
            }
        }

        Platform.runLater { make_fxPanel(fxPanel) }
        this.add(topPanel, BorderLayout.NORTH)
        this.add(fxPanel, BorderLayout.CENTER)
    }

    fun make_fxPanel(mainPanel: JFXPanel) {
        val sceneWidth = 800.0
        val sceneHeight = 600.0
        val g = build_sample()
        val strategy = PlacementStrategy()
        umlGraphPanel = UMLGraphPanel(g, strategy)
        umlGraphControlPanel = UMLGraphControlPanel(umlGraphPanel)
        val scene = Scene(umlGraphControlPanel, sceneWidth, sceneHeight)
        Platform.runLater { umlGraphPanel!!.init() }
        mainPanel.scene = scene
    }

    companion object {
        public fun build_graph(model: Model): Graph<String, String> {
            val g: Graph<String, String> = Graph();
            model.toGraph(g)
            return g
        }

        public fun build_package_graph(model: Int, dataBaseUtil: DataBaseUtil): Graph<Int, String> {
            val g: Graph<Int, String> =
                Graph()
            toPackage(g, model, dataBaseUtil)
            return g
        }

        public fun build_package_graph(model: Model): Graph<String, String> {
            val g: Graph<String, String> = Graph();
            model.toPackage(g)
            return g
        }

        public fun build_class_graph(model: Int, dataBaseUtil: DataBaseUtil): Graph<Int, String> {
            val g: Graph<Int, String> =
                Graph();
            toClass(g, model, dataBaseUtil)
            return g
        }

        public fun build_class_graph(model: Model): Graph<String, String> {
            val g: Graph<String, String> =
                Graph();
            model.toClass(g)
            return g
        }
    }


    private fun build_sample(): Graph<Int, String> {
        val graph = Graph<Int, String>()
        val packageV = graph.getOrCreateVertex(1, ElementTypes.PACKAGE, "<<package>> A\n included: interface, component, enum")
        val interfaceV = graph.getOrCreateVertex(2, ElementTypes.INTERFACE, "interface")
        val componentV = graph.getOrCreateVertex(3, ElementTypes.COMPONENT, "component")
        val enumV = graph.getOrCreateVertex(4, ElementTypes.ENUM, "enum")
        val classV = graph.getOrCreateVertex(5, ElementTypes.CLASS, "class")
        val fClassV = graph.getOrCreateVertex(6, ElementTypes.CLASS, "f class")

        graph.getOrCreateEdge(packageV, interfaceV, "package - interface", ArrowTypes.AGGREGATION)
        graph.getOrCreateEdge(interfaceV, packageV, "interface - package", ArrowTypes.DEPENDENCY)
        graph.getOrCreateEdge(packageV, componentV, "package - component", ArrowTypes.COMPOSITION)
        graph.getOrCreateEdge(packageV, enumV, "package - enum")
        graph.getOrCreateEdge(interfaceV, componentV, "interface - component")
        graph.getOrCreateEdge(componentV, enumV, "component - enum", ArrowTypes.REALIZATION)
        graph.getOrCreateEdge(interfaceV, classV, "interface - class")
        graph.getOrCreateEdge(enumV, fClassV, "enum - f class")
        graph.getOrCreateEdge(fClassV, enumV, "f class - enum")

        return graph
    }
}