package com.example.umldrawer.tabs

import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import umlgraph.containers.GraphDemoContainer
import umlgraph.graph.Digraph
import umlgraph.graph.DigraphEdgeList
import umlgraph.graph.Graph
import umlgraph.graphview.GraphPanel
import umlgraph.graphview.arrows.ArrowTypes
import umlgraph.graphview.edges.Edge
import umlgraph.graphview.strategy.CircularSortedPlacementStrategy
import umlgraph.graphview.strategy.PlacementStrategy
import umlgraph.graphview.vertices.GraphVertex
import umlgraph.graphview.vertices.elements.ElementTypes

public fun createFXGraph(): JFXPanel? {
    val fxPanel: JFXPanel = object : JFXPanel() {}
    Platform.runLater { initFXGraph(fxPanel) }
    return fxPanel
}

private fun initFXGraph(fxPanel: JFXPanel) {
    val sceneWidth = 800.0
    val sceneHeight = 600.0
    val g = build_sample_digraph()
    val strategy: PlacementStrategy = CircularSortedPlacementStrategy()
    val graphView = GraphPanel(g, strategy)
    if (g.numVertices() > 0) {
        graphView.getStylableVertex("A").setStyle("-fx-fill: gold; -fx-stroke: brown;")
    }
    val scene = Scene(GraphDemoContainer(graphView), sceneWidth, sceneHeight)
    graphView.setVertexDoubleClickAction { graphVertex: GraphVertex<String> ->
        println("Vertex contains element: " + graphVertex.underlyingVertex.element())
        if (!graphVertex.removeStyleClass("myVertex")) {
            graphVertex.addStyleClass("myVertex")
        }
    }
    graphView.setEdgeDoubleClickAction { graphEdge: Edge<String, String> ->
        println("Edge contains element: " + graphEdge.underlyingEdge.element())
        graphEdge.setStyle("-fx-stroke: black; -fx-stroke-width: 3;")
        graphEdge.stylableArrow.setStyle("-fx-stroke: black; -fx-stroke-width: 3;")
    }
    fxPanel.scene = scene
}



private fun build_sample_digraph(): Graph<String, String> {
    val g: Digraph<String, String> = DigraphEdgeList()
    g.insertVertex("A", ElementTypes.PACKAGE, "<<package>> A\n included: B, C, D")
    g.insertVertex("B", ElementTypes.INTERFACE)
    g.insertVertex("C", ElementTypes.COMPONENT)
    g.insertVertex("D", ElementTypes.ENUM)
    g.insertVertex("E", ElementTypes.CLASS)
    g.insertVertex("F")
    g.insertVertex("main")
    g.insertEdge("A", "B", "AB", ArrowTypes.AGGREGATION)
    g.insertEdge("B", "A", "AB2", ArrowTypes.DEPENDENCY)
    g.insertEdge("A", "C", "AC", ArrowTypes.COMPOSITION)
    g.insertEdge("A", "D", "AD")
    g.insertEdge("B", "C", "BC")
    g.insertEdge("C", "D", "CD", ArrowTypes.REALIZATION)
    g.insertEdge("B", "E", "BE")
    g.insertEdge("F", "D", "DF")
    g.insertEdge("F", "D", "DF2")

    //yep, it's a loop!
    g.insertEdge("A", "A", "Loop")
    return g
}