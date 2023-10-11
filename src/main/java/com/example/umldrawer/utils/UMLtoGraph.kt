package com.example.umldrawer.utils

import org.eclipse.uml2.uml.*
import uml.util.nl
import umlgraph.graph.Digraph
import umlgraph.graph.Vertex
import umlgraph.graphview.arrows.ArrowTypes
import umlgraph.graphview.vertices.elements.ElementTypes

fun Package.toGraph(graph: Digraph<String, String>) {
    val root = graph.insertVertex(name, ElementTypes.PACKAGE)
    packagedElements
        .filter { !it.hasKeyword("unknown") }
        .forEach {
            when (it) {
                is Class -> it.generateClass(graph, root)
                is Interface -> it.generateInterface(graph, root)
                is Enumeration -> it.generateEnumeration(graph, root)
                is Package -> it.generatePackage(graph, root)
            }
        }
}


private fun Package.generatePackage(graph: Digraph<String, String>, parent: Vertex<String>) {
    val root = graph.insertVertex(name, ElementTypes.PACKAGE)

    packagedElements
        .filter { !it.hasKeyword("unknown") }
        .forEach {
            when (it) {
                is Class -> it.generateClass(graph, root)
                is Interface -> it.generateInterface(graph, root)
                is Enumeration -> it.generateEnumeration(graph, root)
                is Package -> it.generatePackage(graph, root)
            }
        }
}

private fun Class.generateClass(graph: Digraph<String, String>, parent: Vertex<String>) {
    val root = graph.insertVertex(name, ElementTypes.CLASS)
    graph.insertEdge(parent, root, "$name-${parent}", ArrowTypes.DEPENDENCY)
}

private val newLine: CharSequence = "\n"

private val VisibilityKind.asJava
    get() = if (this == VisibilityKind.PACKAGE_LITERAL) "" else "$literal "

private val NamedElement.javaName: String
    get() {
        val longName = qualifiedName.replace("::", ".")
        val k = longName.indexOf('.')
        return longName.substring(k + 1)
    }

private val Classifier.packageAsJava
    get() = "package ${nearestPackage.javaName};$nl"

private val Classifier.importsAsJava
    get() = importedMembers
        .map { "import ${it.javaName};" }
        .filter { !it.startsWith("import java.lang") }
        .joinToString(newLine)

private val Classifier.parentsAsJava: String
    get() {
        val parents = generalizations
            .map { it.general }
            .filter { !it.javaName.endsWith("java.lang.Object") }
            .joinToString { it.name }
        return if (parents.isNotEmpty()) " extends $parents" else ""
    }

private val Class.interfacesAsJava: String
    get() {
        val implemented = interfaceRealizations.joinToString { it.contract.name }
        return if (implemented.isNotEmpty()) " implements $implemented" else ""
    }

private val Property.propertyAsJava
    get() = "$modifiers${type.name} $name;"

private val Property.modifiers: String
    get() {
        var modifiers = visibility.asJava
        if (isStatic) modifiers += "static "
        return modifiers
    }

private val Operation.operationAsJava: String
    get() {
        val returns = returnResult?.type?.name ?: "void"
        val tail = if (isAbstract) ";" else " {$newLine}$newLine"

        return "$modifiers$returns $name$parameters$tail"
    }

private val Operation.modifiers: String
    get() {
        var modifiers = visibility.asJava
        if (isStatic) modifiers += "static "
        if (isAbstract) modifiers += "abstract "
        return modifiers
    }

private val Operation.parameters
    get() = ownedParameters
        .filter { it.direction != ParameterDirectionKind.RETURN_LITERAL }
        .joinToString(prefix = "(", postfix = ")")
        { "${it.type.name} ${it.name}" }

private val Enumeration.modifiers: String
    get() {
        var modifiers = visibility.asJava
        if (isLeaf) modifiers += "final "
        return modifiers
    }

private fun Enumeration.generateEnumeration(graph: Digraph<String, String>, parent: Vertex<String>) {
    val root = graph.insertVertex(name, ElementTypes.ENUM)
    graph.insertEdge(parent, root, "$name-$parent", ArrowTypes.DEPENDENCY)
}

private val Interface.modifiers: String
    get() {
        var modifiers = visibility.asJava
        if (isLeaf) modifiers += "final "
        return modifiers
    }

private fun Interface.generateInterface(graph: Digraph<String, String>, parent: Vertex<String>) {
    val root = graph.insertVertex(name, ElementTypes.INTERFACE)
    graph.insertEdge(parent, root, "$name-$parent", ArrowTypes.DEPENDENCY)
}