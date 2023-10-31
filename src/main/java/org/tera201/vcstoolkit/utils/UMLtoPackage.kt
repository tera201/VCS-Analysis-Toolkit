package org.tera201.vcstoolkit.utils

import org.eclipse.uml2.uml.*
import org.tera201.umlgraph.graph.Digraph
import org.tera201.umlgraph.graph.Vertex
import org.tera201.umlgraph.graphview.arrows.ArrowTypes
import org.tera201.umlgraph.graphview.vertices.elements.ElementTypes

fun Package.toPackage(graph: Digraph<String, String>) {
    val root = graph.insertVertex(name, ElementTypes.PACKAGE)
    packagedElements
        .filter { !it.hasKeyword("unknown") }
        .forEach {
            when (it) {
                is Package -> it.generatePackage(graph, root)
            }
        }
}

private fun Package.generatePackage(graph: Digraph<String, String>, parent: Vertex<String>) {
    val root = graph.insertVertex(name, ElementTypes.PACKAGE)
    graph.insertEdge(parent, root, "$name-${parent}", ArrowTypes.DEPENDENCY)
    packagedElements
        .filter { !it.hasKeyword("unknown") }
        .forEach {
            when (it) {
                is Package -> it.generatePackage(graph, root)
            }
        }
}