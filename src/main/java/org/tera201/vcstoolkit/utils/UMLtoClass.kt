package org.tera201.vcstoolkit.utils

import org.eclipse.uml2.uml.*
import org.tera201.umlgraph.graph.Graph
import org.tera201.umlgraph.graph.Vertex
import org.tera201.umlgraph.graphview.arrows.ArrowTypes
import org.tera201.umlgraph.graphview.vertices.elements.ElementTypes

fun Package.toClass(graph: Graph<String, String>) {
    packagedElements
        .filter { !it.hasKeyword("unknown") }
        .forEach {
            when (it) {
                is Class -> it.generateClass(graph, null)
                is Interface -> it.generateInterface(graph, null)
                is Enumeration -> it.generateEnumeration(graph, null)
                is Package -> it.generatePackage(graph, null)
            }
        }
}


private fun Package.generatePackage(graph: Graph<String, String>, parent: Vertex<String>?) {
    packagedElements
        .filter { !it.hasKeyword("unknown") }
        .forEach {
            when (it) {
                is Class -> it.generateClass(graph, null)
                is Interface -> it.generateInterface(graph, null)
                is Enumeration -> it.generateEnumeration(graph, null)
                is Package -> it.generatePackage(graph, null)
            }
        }
}

private fun Class.generateClass(graph: Graph<String, String>, parent: Vertex<String>?) {
    this.parentsAsJava(graph, name)
    this.interfacesAsJava(graph, name)
    val root = getVertexOrCreate(graph, name, ElementTypes.CLASS)
//    this.nestedClasses(graph, root)
    if (parent != null) graph.getOrCreateEdge(parent, root, "$name-${parent}", ArrowTypes.DEPENDENCY)
}

private fun Classifier.nestedClasses(graph: Graph<String, String>, parent: Vertex<String>?) {
    val root = graph.getOrCreateVertex(name, ElementTypes.CLASS)
//    if (parent != null) graph.insertEdge(parent, root, "$name-$parent", ArrowTypes.DEPENDENCY)
}



private fun Class.interfacesAsJava(graph: Graph<String, String>, mainClass:String) {
        val implemented = interfaceRealizations.joinToString { it.contract.name }
        if (implemented.isNotEmpty()) {
            val root = getVertexOrCreate(graph, implemented, ElementTypes.INTERFACE)
            val mainClassV = getVertexOrCreate(graph, mainClass, ElementTypes.CLASS)
            graph.getOrCreateEdge(root, mainClassV, "$mainClassV-$root", ArrowTypes.REALIZATION)
        }
}


private val NamedElement.javaName: String
    get() {
        val longName = qualifiedName.replace("::", ".")
        val k = longName.indexOf('.')
        return longName.substring(k + 1)
    }


private fun Classifier.parentsAsJava(graph: Graph<String, String>, mainClass:String) {
    val parents = generalizations
        .map { it.general }
        .filter { !it.javaName.endsWith("java.lang.Object") }
        .joinToString { it.name }
    if (parents.isNotEmpty()) {
        val root: Vertex<String> = getVertexOrCreate(graph, parents, ElementTypes.CLASS)
        val mainClassV = getVertexOrCreate(graph, mainClass, ElementTypes.CLASS)
        graph.getOrCreateEdge(root, mainClassV, "$mainClassV-$root", ArrowTypes.DEPENDENCY)
    }
}

private fun Enumeration.generateEnumeration(graph: Graph<String, String>, parent: Vertex<String>?) {
    var root:Vertex<String>;
    root = getVertexOrCreate(graph, name, ElementTypes.ENUM)
    if (parent != null) graph.getOrCreateEdge(parent, root, "$name-$parent", ArrowTypes.DEPENDENCY)
}

private fun Interface.generateInterface(graph: Graph<String, String>, parent: Vertex<String>?) {
    var root:Vertex<String>;
    root = getVertexOrCreate(graph, name, ElementTypes.INTERFACE)
    if (parent != null) graph.getOrCreateEdge(parent, root, "$name-$parent", ArrowTypes.DEPENDENCY)
}
private fun getVertexOrCreate(graph: Graph<String, String>, nodeName: String, types: ElementTypes): Vertex<String> {
    return graph.getOrCreateVertex(nodeName, types)
}