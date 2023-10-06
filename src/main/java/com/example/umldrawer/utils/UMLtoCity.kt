package com.example.umldrawer.utils

import org.eclipse.uml2.uml.*
import org.example.elements.City
import org.example.elements.Quarter

fun Package.toCity(city: City) {
    packagedElements
        .filter { !it.hasKeyword("unknown") }
        .forEach {
            when (it) {
                is Package -> it.generatePackage(city)
            }
        }
}

private fun Package.generatePackage(city: City) {
    val quarter = Quarter(name,500.0, 10.0, 500.0, 50.0)
    city.addQuarter(quarter)
    packagedElements
        .filter { !it.hasKeyword("unknown") }
        .forEach {
            when (it) {
                is Package -> it.generatePackage(city)
            }
        }
}