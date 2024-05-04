package org.tera201.vcstoolkit.utils

import org.tera201.code2uml.util.messages.DataBaseUtil
import org.tera201.code2uml.util.messages.getPackage
import org.tera201.code2uml.util.messages.getRootPackageIds
import org.tera201.umlgraph.graph.Digraph
import org.tera201.umlgraph.graph.DigraphTreeEdgeList
import org.tera201.umlgraph.graph.InvalidVertexException
import org.tera201.umlgraph.graph.Vertex
import org.tera201.umlgraph.graphview.arrows.ArrowTypes
import org.tera201.umlgraph.graphview.vertices.elements.ElementTypes

fun toPackage(graph: DigraphTreeEdgeList<String, String>, modelId:Int, dataBaseUtil: DataBaseUtil) {
    dataBaseUtil.getRootPackageIds(modelId).forEach { generatePackage(graph, null, dataBaseUtil, it, modelId) }
}

private fun generatePackage(graph: DigraphTreeEdgeList<String, String>, parent: Vertex<String>?, dataBaseUtil: DataBaseUtil, id: Int,  modelId:Int) {
    val packageDB = dataBaseUtil.getPackage(id, modelId)
    val node = getVertexOrCreate(graph, packageDB.name, ElementTypes.PACKAGE)
//    val node = graph.insertVertex(packageDB.name, ElementTypes.PACKAGE)
    parent?.let {  graph.insertEdge(parent, node, "${packageDB.name}-${parent}", ArrowTypes.DEPENDENCY) }
    packageDB.childrenId.forEach { generatePackage(graph, node, dataBaseUtil, it, modelId) }
}

private fun getVertexOrCreate(graph: DigraphTreeEdgeList<String, String>, nodeName: String, types: ElementTypes): Vertex<String> {
    return try {
        graph.getVertex(nodeName)
    } catch (e: InvalidVertexException) {
        graph.insertVertex(nodeName, types)
    }
}