package com.example.umldrawer.utils

import org.eclipse.uml2.uml.*
import uml.util.nl
import umlgraph.graph.Digraph
import umlgraph.graph.Vertex
import umlgraph.graphview.arrows.ArrowTypes
import umlgraph.graphview.vertices.elements.ElementTypes

fun Package.toPackage(graph: Digraph<String, String>) {
    val root = graph.insertVertex(name, ElementTypes.PACKAGE)
    ownedMembers
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
    ownedMembers
        .filter { !it.hasKeyword("unknown") }
        .forEach {
            when (it) {
                is Package -> it.generatePackage(graph, root)
            }
        }
}

private val VisibilityKind.asJava
    get() = if (this == VisibilityKind.PACKAGE_LITERAL) "" else "$literal "

private val NamedElement.javaName: String
    get() {
        val longName = qualifiedName.replace("::", ".")
        val k = longName.indexOf('.')
        return longName.substring(k + 1)
    }