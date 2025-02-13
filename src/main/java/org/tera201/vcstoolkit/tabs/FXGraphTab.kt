package org.tera201.vcstoolkit.tabs

import org.tera201.vcstoolkit.utils.toClass
import org.tera201.vcstoolkit.utils.toGraph
import org.tera201.vcstoolkit.utils.toPackage
import com.intellij.icons.AllIcons
import com.intellij.openapi.ui.ComboBox
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import org.eclipse.uml2.uml.Model
import org.tera201.code2uml.util.messages.DataBaseUtil
import org.tera201.umlgraph.containers.GraphDemoContainer
import org.tera201.umlgraph.graph.Digraph
import org.tera201.umlgraph.graph.DigraphEdgeList
import org.tera201.umlgraph.graph.Graph
import org.tera201.umlgraph.graphview.GraphPanel
import org.tera201.umlgraph.graphview.arrows.ArrowTypes
import org.tera201.umlgraph.graphview.strategy.DigraphTreePlacementStrategy
import org.tera201.umlgraph.graphview.strategy.PlacementStrategy
import org.tera201.umlgraph.graphview.vertices.elements.ElementTypes
import org.tera201.vcstoolkit.helpers.SharedModel
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.JButton
import javax.swing.JPanel

class FXGraphTab(private val tabManager: TabManager, modelListContent:SharedModel): JPanel() {

    var graphView: GraphPanel<Int, String>? = null
    private val fxPanel: JFXPanel = object : JFXPanel() {}
    private val topPanel = JPanel()
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
//                model = gitTab!!.models.stream().filter { it.name == selectedModelName }.findAny().getOrNull()
                model = gitTab!!.modelsIdMap.getOrDefault(selectedModelName, null)
            }
        }

        packageButton.addActionListener {
            if (model != null) {
                val strategy: PlacementStrategy = DigraphTreePlacementStrategy()
                graphView = GraphPanel(build_package_graph(model!!, gitTab!!.dataBaseUtil), strategy)
                val sceneWidth = 800.0
                val sceneHeight = 600.0
                val scene = Scene(GraphDemoContainer(graphView), sceneWidth, sceneHeight)
                fxPanel.scene = scene
//                graphView?.setTheGraph(build_package_graph(model!!, gitTab!!.dataBaseUtil))
            }
        }
//
        classButton.addActionListener {
            if (model != null) {
                val strategy: PlacementStrategy = DigraphTreePlacementStrategy()
                graphView = GraphPanel(build_class_graph(model!!, gitTab!!.dataBaseUtil), strategy)
                val sceneWidth = 800.0
                val sceneHeight = 600.0
                val scene = Scene(GraphDemoContainer(graphView), sceneWidth, sceneHeight)
                fxPanel.scene = scene
//                graphView?.setTheGraph(build_class_graph(model!!, gitTab!!.dataBaseUtil))
            }
        }

        Platform.runLater { make_fxPanel(fxPanel) }
        this.add(topPanel, BorderLayout.NORTH)
        this.add(fxPanel, BorderLayout.CENTER)
    }

    fun make_fxPanel(mainPanel: JFXPanel) {
        val sceneWidth = 800.0
        val sceneHeight = 600.0
        val g = build_sample_digraph()
        val strategy: PlacementStrategy = DigraphTreePlacementStrategy()
        graphView = GraphPanel(g, strategy)
//        if (g.numVertices() > 0) {
//            graphView!!.getStylableVertex("A").setStyle("-fx-fill: gold; -fx-stroke: brown;")
//        }
        val scene = Scene(GraphDemoContainer(graphView), sceneWidth, sceneHeight)
        Platform.runLater { graphView!!.init() }
//        graphView!!.setVertexDoubleClickAction { graphVertex: GraphVertex<String> ->
//            if (!graphVertex.removeStyleClass("myVertex")) {
//                graphVertex.addStyleClass("myVertex")
//            }
//        }
//        graphView!!.setEdgeDoubleClickAction { graphEdge: Edge<String, String> ->
//            graphEdge.setStyle("-fx-stroke: black; -fx-stroke-width: 3;")
//            graphEdge.stylableArrow.setStyle("-fx-stroke: black; -fx-stroke-width: 3;")
//        }
        mainPanel.scene = scene
    }

    companion object {
        public fun build_graph(model: Model): Graph<String, String> {
            val g: Digraph<String, String> = DigraphEdgeList();
            model.toGraph(g)
            return g
        }

        public fun build_package_graph(model: Int, dataBaseUtil: DataBaseUtil): Graph<Int, String> {
            val g: DigraphEdgeList<Int, String> =
                DigraphEdgeList()
            toPackage(g, model, dataBaseUtil)
            return g
        }

        public fun build_package_graph(model: Model): Graph<String, String> {
            val g: Digraph<String, String> = DigraphEdgeList();
            model.toPackage(g)
            return g
        }

        public fun build_class_graph(model: Int, dataBaseUtil: DataBaseUtil): Graph<Int, String> {
            val g: DigraphEdgeList<Int, String> =
                DigraphEdgeList();
            toClass(g, model, dataBaseUtil)
            return g
        }

        public fun build_class_graph(model: Model): Graph<String, String> {
            val g: DigraphEdgeList<String, String> =
                DigraphEdgeList();
            model.toClass(g)
            return g
        }
    }


    private fun build_sample_digraph(): Graph<Int, String> {
        val g: Digraph<Int, String> = DigraphEdgeList()
        val a = g.insertVertex(1, ElementTypes.PACKAGE, "A", "<<package>> A\n included: B, C, D")
        val b = g.insertVertex(2, ElementTypes.INTERFACE,"B")
        val c = g.insertVertex(3, ElementTypes.COMPONENT,"C")
        val d = g.insertVertex(4, ElementTypes.ENUM,"D")
        val e = g.insertVertex(5, ElementTypes.CLASS,"E")
        val f = g.insertVertex(6, ElementTypes.CLASS,"F")
        val mn = g.insertVertex(7, ElementTypes.CLASS,"Main")
        g.insertEdge(a, b, "AB", ArrowTypes.AGGREGATION)
        g.insertEdge(b, a, "AB2", ArrowTypes.DEPENDENCY)
        g.insertEdge(a, c, "AC", ArrowTypes.COMPOSITION)
        g.insertEdge(a, d, "AD")
        g.insertEdge(b, c, "BC")
        g.insertEdge(c, d, "CD", ArrowTypes.REALIZATION)
        g.insertEdge(b, e, "BE")
        g.insertEdge(d, f, "DF2")

        //yep, it's a loop!
        g.insertEdge(a, a, "Loop")
        return g
    }
}