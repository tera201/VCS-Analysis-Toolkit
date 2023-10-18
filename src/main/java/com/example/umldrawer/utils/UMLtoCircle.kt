package com.example.umldrawer.utils

import com.example.umldrawer.tabs.FXCirclePanel
import org.eclipse.uml2.uml.*
import org.tera201.elements.ClassCircle
import org.tera201.elements.PackageCircle

fun Package.toCircle() {
    val packageCircle = PackageCircle(name, 6000.0, 5500.0, 100.0)
    FXCirclePanel.circleSpace.add(packageCircle)
    FXCirclePanel.circleSpace.mainCircle = packageCircle
    packagedElements
        .filter { !it.hasKeyword("unknown") }
        .forEach {
            when (it) {
                is Package -> it.generatePackage(packageCircle)
                is Class -> it.generateClass(packageCircle)
            }
        }
}

private fun Package.generatePackage(circleParent: PackageCircle) {
    val size = ownedComments[0].body.toDouble()
    val side = size / 20
    println("${circleParent.name} call $name radiusIn ${circleParent.innerRadius/2}")
    val packageCircle = PackageCircle(name, circleParent.innerRadius/2 + 500, circleParent.innerRadius/2, 100.0)
    circleParent.addCircle(packageCircle)
    packagedElements
        .filter { !it.hasKeyword("unknown") }
        .forEach {
            when (it) {
                is Package -> it.generatePackage(packageCircle)
                is Class -> it.generateClass(packageCircle)
                is Interface -> it.generateInterface(packageCircle)
                is Enumeration -> it.generateEnumeration(packageCircle)
            }
        }
}

private fun Class.generateClass(circleParent: PackageCircle) {
    val size = ownedComments[0].body.toDouble()
    val methods = (ownedComments[1].body.toDouble() + 1) * 10
    val side = size / 2
    println("${circleParent.name} call $name radiusIn $side")

    val classCircle = ClassCircle(name, side + methods * 10, side, 100.0)
    circleParent.addCircle(classCircle)
}

private fun Interface.generateInterface(circleParent: PackageCircle) {
    val size = ownedComments[0].body.toDouble()
    val methods = (ownedComments[1].body.toDouble() + 1) * 10
    val side = size / 2
    val classCircle = ClassCircle(name, side + methods * 10, side, 100.0)
    circleParent.addCircle(classCircle)
}

private fun Enumeration.generateEnumeration(circleParent: PackageCircle) {
    val classCircle = ClassCircle(name, 1000.0, 700.0, 100.0)
    circleParent.addCircle(classCircle)
}

private val Classifier.parentsAsJava: String
    get() {
        val parents = generalizations
            .map { it.general }
            .filter { !it.javaName.endsWith("java.lang.Object") }
            .joinToString { it.name }
        return if (parents.isNotEmpty()) " extends $parents" else ""
    }


private val NamedElement.javaName: String
    get() {
        val longName = qualifiedName.replace("::", ".")
        val k = longName.indexOf('.')
        return longName.substring(k + 1)
    }