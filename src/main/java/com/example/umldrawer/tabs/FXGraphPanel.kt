package com.example.umldrawer.tabs

import com.example.umldrawer.utils.toGraph
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import org.eclipse.uml2.uml.Model
import umlgraph.containers.GraphDemoContainer
import umlgraph.graph.Digraph
import umlgraph.graph.Graph
import umlgraph.graph.DigraphTreeEdgeList
import umlgraph.graphview.GraphPanel
import umlgraph.graphview.arrows.ArrowTypes
import umlgraph.graphview.edges.Edge
import umlgraph.graphview.strategy.DigraphTreePlacementStrategy
import umlgraph.graphview.strategy.PlacementStrategy
import umlgraph.graphview.vertices.GraphVertex
import umlgraph.graphview.vertices.elements.ElementTypes

var graphView: GraphPanel<String,String>? = null
public fun createFXGraph(): JFXPanel? {
    val fxPanel: JFXPanel = object : JFXPanel() {}
    Platform.runLater { initFXGraph(fxPanel) }
    return fxPanel
}

private fun initFXGraph(fxPanel: JFXPanel) {
    val sceneWidth = 800.0
    val sceneHeight = 600.0
    val g = build_sample_digraph()
    val strategy: PlacementStrategy = DigraphTreePlacementStrategy()
    graphView = GraphPanel(g, strategy)
    if (g.numVertices() > 0) {
        graphView!!.getStylableVertex("A").setStyle("-fx-fill: gold; -fx-stroke: brown;")
    }
    val scene = Scene(GraphDemoContainer(graphView), sceneWidth, sceneHeight)
    Platform.runLater({graphView!!.init()})
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
    fxPanel.scene = scene
}

public fun build_graph(model: Model): Graph<String, String> {
    val g: Digraph<String, String> = DigraphTreeEdgeList();
    model.toGraph(g)
    return g
}


private fun build_sample_digraph(): Graph<String, String> {
    val g: Digraph<String, String> = DigraphTreeEdgeList()
     val a = g.insertVertex("A", ElementTypes.PACKAGE, "<<package>> A\n included: B, C, D")
    val b = g.insertVertex("B", ElementTypes.INTERFACE)
    val c = g.insertVertex("C", ElementTypes.COMPONENT)
    val d = g.insertVertex("D", ElementTypes.ENUM)
    val e = g.insertVertex("E", ElementTypes.CLASS)
    val f =  g.insertVertex("F")
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