package org.tera201.vcstoolkit.utils

import org.eclipse.uml2.uml.*
import org.tera201.code2uml.util.messages.*
import org.tera201.umlgraph.graph.Graph
import org.tera201.umlgraph.graph.GraphException
import org.tera201.umlgraph.graph.Vertex
import org.tera201.umlgraph.graphview.arrows.ArrowTypes
import org.tera201.umlgraph.graphview.vertices.elements.ElementTypes

fun toClass(graph: Graph<Int, String>, modelId:Int, dataBaseUtil: DataBaseUtil) {
    dataBaseUtil.getRootPackageIds(modelId).forEach { generatePackage(graph, null, dataBaseUtil, it, modelId) }
}


private fun generatePackage(graph: Graph<Int, String>, parent: Vertex<Int>?, dataBaseUtil: DataBaseUtil, id: Int, modelId:Int) {
    val packageDB = dataBaseUtil.getPackage(id, modelId)
    dataBaseUtil.getClassIdsByPackageId(modelId, id).forEach { generateClass(graph, null, dataBaseUtil, it, modelId) }
    dataBaseUtil.getInterfacesIdsByPackageId(modelId, id).forEach { generateInterface(graph, null, dataBaseUtil, it, modelId) }
    dataBaseUtil.getEnumerationsIdsByPackageId(modelId, id).forEach { generateEnumeration(graph, null, dataBaseUtil, it, modelId) }
    packageDB.childrenId.forEach { generatePackage(graph, parent, dataBaseUtil, it, modelId) }
}

private fun generateClass(graph: Graph<Int, String>, parent: Vertex<Int>?, dataBaseUtil: DataBaseUtil, id: Int, modelId:Int) {
    val classDB = dataBaseUtil.getClass(id, modelId)
    val root = getVertexOrCreate(graph, id, classDB.name, ElementTypes.CLASS)
    classDB.interfaceIdList.forEach { interfacesAsJava(graph, root, dataBaseUtil, it, modelId) }
    classDB.parentClassIdList.forEach { parentsAsJava(graph, root, dataBaseUtil, it, modelId) }
//    this.nestedClasses(graph, root)
    if (parent != null) graph.getOrCreateEdge(parent, root, "${classDB.name}-${parent}", ArrowTypes.DEPENDENCY)
}

private fun nestedClasses(graph: Graph<Int, String>, parent: Vertex<Int>?) {
//    val root = graph.insertVertex(name, ElementTypes.CLASS)
//    if (parent != null) graph.insertEdge(parent, root, "$name-$parent", ArrowTypes.DEPENDENCY)
}



private fun interfacesAsJava(graph: Graph<Int, String>, mainClass:Vertex<Int>, dataBaseUtil: DataBaseUtil, id: Int, modelId:Int) {
    val implemented = dataBaseUtil.getInterface(id, modelId)
    val root = getVertexOrCreate(graph, id, implemented.name, ElementTypes.INTERFACE)
    tryInsertEdge(graph, root, mainClass, ArrowTypes.REALIZATION)
//    graph.insertEdge(root, mainClassV, "$mainClassV-$root", ArrowTypes.REALIZATION)
}


private val NamedElement.javaName: String
    get() {
        val longName = qualifiedName.replace("::", ".")
        val k = longName.indexOf('.')
        return longName.substring(k + 1)
    }


private fun parentsAsJava(graph: Graph<Int, String>, mainClass:Vertex<Int>, dataBaseUtil: DataBaseUtil, id: Int, modelId:Int) {
    val parents = dataBaseUtil.getClass(id, modelId)
    val root: Vertex<Int> = getVertexOrCreate(graph, id, parents.name, ElementTypes.CLASS)
    tryInsertEdge(graph, root, mainClass, ArrowTypes.DEPENDENCY)
//    graph.insertEdge(root, mainClassV, "$mainClassV-$root", ArrowTypes.DEPENDENCY)
}

private fun generateEnumeration(graph: Graph<Int, String>, parent: Vertex<Int>?, dataBaseUtil: DataBaseUtil, id: Int, modelId:Int) {
    val enumerationDB = dataBaseUtil.getEnumerations(id, modelId);
    val root:Vertex<Int> = getVertexOrCreate(graph, id, enumerationDB.name, ElementTypes.ENUM)
    if (parent != null) graph.getOrCreateEdge(parent, root, "${root.label}-${parent.label}", ArrowTypes.DEPENDENCY)
}

private fun generateInterface(graph: Graph<Int, String>, parent: Vertex<Int>?, dataBaseUtil: DataBaseUtil, id: Int, modelId:Int) {
    val interfaceDB = dataBaseUtil.getInterface(id, modelId);
    val root:Vertex<Int> = getVertexOrCreate(graph, id, interfaceDB.name, ElementTypes.INTERFACE)
    if (parent != null) graph.getOrCreateEdge(parent, root, "${root.label}-${parent.label}", ArrowTypes.DEPENDENCY)
}
private fun getVertexOrCreate(graph: Graph<Int, String>, nodeId: Int, nodeName: String, types: ElementTypes): Vertex<Int> {
    return graph.getVertex(nodeId)
}

private fun tryInsertEdge(graph: Graph<Int, String>, node1: Vertex<Int>, node2: Vertex<Int>, arrowTypes: ArrowTypes) {
    try {
        graph.getOrCreateEdge(node1, node2, "$node2 - $node1", arrowTypes)
    } catch (_: GraphException) { println("Error adding edge $node2 - $node1") }
}