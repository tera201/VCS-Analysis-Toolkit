package com.example.umldrawer.tabs

import com.example.umldrawer.utils.toClass
import com.example.umldrawer.utils.toGraph
import com.example.umldrawer.utils.toPackage
import com.intellij.icons.AllIcons
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import org.eclipse.uml2.uml.Model
import umlgraph.containers.GraphDemoContainer
import umlgraph.graph.Digraph
import umlgraph.graph.DigraphTreeEdgeList
import umlgraph.graph.Graph
import umlgraph.graphview.GraphPanel
import umlgraph.graphview.arrows.ArrowTypes
import umlgraph.graphview.edges.Edge
import umlgraph.graphview.strategy.DigraphTreePlacementStrategy
import umlgraph.graphview.strategy.PlacementStrategy
import umlgraph.graphview.vertices.GraphVertex
import umlgraph.graphview.vertices.elements.ElementTypes
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JRadioButton

class FXGraphPanel: JPanel() {

    var graphView: GraphPanel<String, String>? = null

    init {
        this.layout = BorderLayout()
        initFXGraph()
    }

    private fun initFXGraph() {

        val topPanel = JPanel()
        topPanel.layout = FlowLayout(FlowLayout.LEFT)
        val packageButton = JButton("Package")
        val classButton = JButton("Class")
        classButton.icon = AllIcons.Nodes.Class
        packageButton.icon = AllIcons.Nodes.Package
        topPanel.add(packageButton)
        topPanel.add(classButton)

        packageButton.addActionListener {
            if (GitPanel.model != null) graphView?.setTheGraph(build_package_graph(GitPanel.model!!))
        }

        classButton.addActionListener {
            if (GitPanel.model != null) graphView?.setTheGraph(build_class_graph(GitPanel.model!!))
        }

        val rightPanel = JPanel()
        rightPanel.layout = BoxLayout(rightPanel, BoxLayout.Y_AXIS)
        rightPanel.add(JRadioButton("Option 1"))
        rightPanel.add(JRadioButton("Option 2"))
        rightPanel.add(JRadioButton("Option 3"))


        val fxPanel: JFXPanel = object : JFXPanel() {}
        Platform.runLater { make_fxPanel(fxPanel) }
        this.add(topPanel, BorderLayout.NORTH)
        this.add(rightPanel, BorderLayout.EAST)
        this.add(fxPanel, BorderLayout.CENTER)
    }

    fun make_fxPanel(mainPanel: JFXPanel) {
        val sceneWidth = 800.0
        val sceneHeight = 600.0
        val g = build_sample_digraph()
        val strategy: PlacementStrategy = DigraphTreePlacementStrategy()
        graphView = GraphPanel(g, strategy)
        if (g.numVertices() > 0) {
            graphView!!.getStylableVertex("A").setStyle("-fx-fill: gold; -fx-stroke: brown;")
        }
        val scene = Scene(GraphDemoContainer(graphView), sceneWidth, sceneHeight)
        Platform.runLater { graphView!!.init() }
        graphView!!.setVertexDoubleClickAction { graphVertex: GraphVertex<String> ->
            println("Vertex contains element: " + graphVertex.underlyingVertex.element())
            if (!graphVertex.removeStyleClass("myVertex")) {
                graphVertex.addStyleClass("myVertex")
            }
        }
        graphView!!.setEdgeDoubleClickAction { graphEdge: Edge<String, String> ->
            println("Edge contains element: " + graphEdge.underlyingEdge.element())
            graphEdge.setStyle("-fx-stroke: black; -fx-stroke-width: 3;")
            graphEdge.stylableArrow.setStyle("-fx-stroke: black; -fx-stroke-width: 3;")
        }
        mainPanel.scene = scene
    }

    companion object {
        public fun build_graph(model: Model): Graph<String, String> {
            val g: Digraph<String, String> = DigraphTreeEdgeList();
            model.toGraph(g)
            return g
        }

        public fun build_package_graph(model: Model): Graph<String, String> {
            val g: Digraph<String, String> = DigraphTreeEdgeList();
            model.toPackage(g)
            return g
        }

        public fun build_class_graph(model: Model): Graph<String, String> {
            val g: DigraphTreeEdgeList<String, String> = DigraphTreeEdgeList();
            model.toClass(g)
            return g
        }
    }


    private fun build_sample_digraph(): Graph<String, String> {
        val g: Digraph<String, String> = DigraphTreeEdgeList()
        val a = g.insertVertex("A", ElementTypes.PACKAGE, "<<package>> A\n included: B, C, D")
        val b = g.insertVertex("B", ElementTypes.INTERFACE)
        val c = g.insertVertex("C", ElementTypes.COMPONENT)
        val d = g.insertVertex("D", ElementTypes.ENUM)
        val e = g.insertVertex("E", ElementTypes.CLASS)
        val f = g.insertVertex("F")
        val mn = g.insertVertex("main")
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