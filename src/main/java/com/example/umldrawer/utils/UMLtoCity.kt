package com.example.umldrawer.utils

import com.example.umldrawer.tabs.FXCityPanel
import org.eclipse.uml2.uml.*
import org.tera201.elements.city.Building
import org.tera201.elements.city.City
import org.tera201.elements.city.Quarter

fun Package.toCity() {
    var city = City(8000.0, 20.0, 8000.0)
    FXCityPanel.citySpace.add(city)
    FXCityPanel.citySpace.mainObject = city;
    packagedElements
        .filter { !it.hasKeyword("unknown") }
        .forEach {
            when (it) {
                is Package -> it.generatePackage(null)
                is Class -> it.generateClass(null)
            }
        }
}

private fun Package.generatePackage(parentName: String?) {
    val size = ownedComments[0].body.toDouble()
    val newName = if (parentName.orEmpty().isNotEmpty())  "$parentName.$name" else name
    val quarter = Quarter(newName, size, 10.0, size, 50.0)
    FXCityPanel.citySpace.add(quarter)
    packagedElements
        .filter { !it.hasKeyword("unknown") }
        .forEach {
            when (it) {
                is Package -> it.generatePackage(newName)
                is Class -> it.generateClass(quarter)
                is Interface -> it.generateInterface(quarter)
                is Enumeration -> it.generateEnumeration(quarter)
            }
        }
}

private fun Class.generateClass(quarter: Quarter?) {
    val size = ownedComments[0].body.toDouble()
    val methods = ownedComments[1].body.toDouble() + 1
    val side = size / 20
    val building = Building(name, side, 10 * methods, side)
    building.info = """
        $parentsAsJava
    """.trimIndent()
    quarter?.addObject(building)
}

private fun Interface.generateInterface(quarter: Quarter?) {
    val size = ownedComments[0].body.toDouble()
    val methods = ownedComments[1].body.toDouble() + 1
    val side = size / 20
    val building = Building(name, side, 10 * methods, side)
    quarter?.addObject(building)
}

private fun Enumeration.generateEnumeration(quarter: Quarter?) {
    val building = Building(name, 100.0, 900.0, 100.0)
    quarter?.addObject(building)
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