package org.tera201.vcstoolkit.utils

import org.eclipse.uml2.uml.*
import org.tera201.code2uml.util.messages.*
import org.tera201.umlgraph.graph.DigraphTreeEdgeList
import org.tera201.umlgraph.graph.InvalidEdgeException
import org.tera201.umlgraph.graph.InvalidVertexException
import org.tera201.umlgraph.graph.Vertex
import org.tera201.umlgraph.graphview.arrows.ArrowTypes
import org.tera201.umlgraph.graphview.vertices.elements.ElementTypes

fun toClass(graph: DigraphTreeEdgeList<String, String>, modelId:Int, dataBaseUtil: DataBaseUtil) {
    dataBaseUtil.getRootPackageIds(modelId).forEach { generatePackage(graph, null, dataBaseUtil, it, modelId) }
}


private fun generatePackage(graph: DigraphTreeEdgeList<String, String>, parent: Vertex<String>?, dataBaseUtil: DataBaseUtil, id: Int,  modelId:Int) {
    val packageDB = dataBaseUtil.getPackage(id, modelId)
    dataBaseUtil.getClassIdsByPackageId(modelId, id).forEach { generateClass(graph, null, dataBaseUtil, it, modelId) }
    dataBaseUtil.getInterfacesIdsByPackageId(modelId, id).forEach { generateInterface(graph, null, dataBaseUtil, it, modelId) }
    dataBaseUtil.getEnumerationsIdsByPackageId(modelId, id).forEach { generateEnumeration(graph, null, dataBaseUtil, it, modelId) }
    packageDB.childrenId.forEach { generatePackage(graph, parent, dataBaseUtil, it, modelId) }
}

private fun generateClass(graph: DigraphTreeEdgeList<String, String>, parent: Vertex<String>?, dataBaseUtil: DataBaseUtil, id: Int,  modelId:Int) {
    val classDB = dataBaseUtil.getClass(id, modelId)
    val root = getVertexOrCreate(graph, classDB.name, ElementTypes.CLASS)
    classDB.interfaceIdList.forEach { interfacesAsJava(graph, root, dataBaseUtil, it, modelId) }
    classDB.parentClassIdList.forEach { parentsAsJava(graph, root, dataBaseUtil, it, modelId) }
//    this.nestedClasses(graph, root)
    if (parent != null) graph.insertEdge(parent, root, "${classDB.name}-${parent}", ArrowTypes.DEPENDENCY)
}

private fun nestedClasses(graph: DigraphTreeEdgeList<String, String>, parent: Vertex<String>?) {
//    val root = graph.insertVertex(name, ElementTypes.CLASS)
//    if (parent != null) graph.insertEdge(parent, root, "$name-$parent", ArrowTypes.DEPENDENCY)
}



private fun interfacesAsJava(graph: DigraphTreeEdgeList<String, String>, mainClass:Vertex<String>, dataBaseUtil: DataBaseUtil, id: Int,  modelId:Int) {
    val implemented = dataBaseUtil.getInterface(id, modelId)
    val root = getVertexOrCreate(graph, implemented.name, ElementTypes.INTERFACE)
    tryInsertEdge(graph, root, mainClass, ArrowTypes.REALIZATION)
//    graph.insertEdge(root, mainClassV, "$mainClassV-$root", ArrowTypes.REALIZATION)
}


private val NamedElement.javaName: String
    get() {
        val longName = qualifiedName.replace("::", ".")
        val k = longName.indexOf('.')
        return longName.substring(k + 1)
    }


private fun parentsAsJava(graph: DigraphTreeEdgeList<String, String>, mainClass:Vertex<String>, dataBaseUtil: DataBaseUtil, id: Int,  modelId:Int) {
    val parents = dataBaseUtil.getClass(id, modelId)
    val root: Vertex<String> = getVertexOrCreate(graph, parents.name, ElementTypes.CLASS)
    tryInsertEdge(graph, root, mainClass, ArrowTypes.DEPENDENCY)
//    graph.insertEdge(root, mainClassV, "$mainClassV-$root", ArrowTypes.DEPENDENCY)
}

private fun generateEnumeration(graph: DigraphTreeEdgeList<String, String>, parent: Vertex<String>?, dataBaseUtil: DataBaseUtil, id: Int,  modelId:Int) {
    val enumerationDB = dataBaseUtil.getEnumerations(id, modelId)
    var root:Vertex<String>;
    root = getVertexOrCreate(graph, enumerationDB.name, ElementTypes.ENUM)
    if (parent != null) graph.insertEdge(parent, root, "${enumerationDB.name}-$parent", ArrowTypes.DEPENDENCY)
}

private fun generateInterface(graph: DigraphTreeEdgeList<String, String>, parent: Vertex<String>?, dataBaseUtil: DataBaseUtil, id: Int,  modelId:Int) {
    val interfaceDB = dataBaseUtil.getInterface(id, modelId)
    var root:Vertex<String>;
    root = getVertexOrCreate(graph, interfaceDB.name, ElementTypes.INTERFACE)
    if (parent != null) graph.insertEdge(parent, root, "${interfaceDB.name}-$parent", ArrowTypes.DEPENDENCY)
}
private fun getVertexOrCreate(graph: DigraphTreeEdgeList<String, String>, nodeName: String, types: ElementTypes): Vertex<String> {
    return try {
        graph.getVertex(nodeName)
    } catch (e: InvalidVertexException) {
        graph.insertVertex(nodeName, types)
    }
}

private fun tryInsertEdge(graph: DigraphTreeEdgeList<String, String>, nodeName: Vertex<String>, nodeName2: Vertex<String>, arrowTypes: ArrowTypes) {
    try {
        graph.insertEdge(nodeName, nodeName2, "$nodeName2 - $nodeName", arrowTypes)
    } catch (_: InvalidEdgeException) { println("Error adding edge $nodeName2 - $nodeName") }
}