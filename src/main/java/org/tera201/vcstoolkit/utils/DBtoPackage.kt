package org.tera201.vcstoolkit.utils

import org.tera201.code2uml.util.messages.DataBaseUtil
import org.tera201.code2uml.util.messages.getModel
import org.tera201.code2uml.util.messages.getPackage
import org.tera201.code2uml.util.messages.getRootPackageIds
import org.tera201.umlgraph.graph.Graph
import org.tera201.umlgraph.graph.Vertex
import org.tera201.umlgraph.graphview.arrows.ArrowTypes
import org.tera201.umlgraph.graphview.vertices.elements.ElementTypes

fun toPackage(graph: Graph<Int, String>, modelId:Int, dataBaseUtil: DataBaseUtil) {
    val node = getVertexOrCreate(graph, 0, dataBaseUtil.getModel(modelId).name)
    dataBaseUtil.getRootPackageIds(modelId).forEach { generatePackage(graph, node, dataBaseUtil, it, modelId) }
}

private fun generatePackage(graph: Graph<Int, String>, parent: Vertex<Int>?, dataBaseUtil: DataBaseUtil, id: Int, modelId:Int) {
    val packageDB = dataBaseUtil.getPackage(id, modelId)
    val node = getVertexOrCreate(graph, id, packageDB.name)
    parent?.let {  graph.getOrCreateEdge(parent, node, "${node.label}-${parent.label}", ArrowTypes.DEPENDENCY) }
    packageDB.childrenId.forEach { generatePackage(graph, node, dataBaseUtil, it, modelId) }
}

private fun getVertexOrCreate(graph: Graph<Int, String>, nodeId:Int, nodeName: String): Vertex<Int> {
    return graph.getOrCreateVertex(nodeId, ElementTypes.PACKAGE, nodeName)
}