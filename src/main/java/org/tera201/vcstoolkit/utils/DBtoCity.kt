package org.tera201.vcstoolkit.utils

import org.tera201.code2uml.util.messages.*
import org.tera201.elements.FXSpace
import org.tera201.elements.city.Building
import org.tera201.elements.city.City
import org.tera201.elements.city.Quarter
import org.tera201.vcstoolkit.services.colors.UMLColorScheme
import org.tera201.vcstoolkit.services.colors.ColorScheme
import org.tera201.vcstoolkit.services.settings.VCSToolkitSettings

private var settings: VCSToolkitSettings = VCSToolkitSettings.getInstance()

fun toCity(citySpace: FXSpace<Quarter>, modelId: Int, dataBaseUtil: DataBaseUtil) {
    val model = dataBaseUtil.getModel(modelId)
    val city = City(8000.0, 20.0, 8000.0, model.name)
    citySpace.add(city)
    dataBaseUtil.getRootPackageIds(modelId).forEach { generatePackage(city, null, dataBaseUtil, it, modelId) }
}

private fun generatePackage(city: City, parentName: String?, dataBaseUtil: DataBaseUtil, id: Int, modelId: Int) {
    val packageDB = dataBaseUtil.getPackage(id, modelId)
    val size = packageDB.size.toDouble()
    val newName = if (parentName.orEmpty().isNotEmpty()) "$parentName.${packageDB.name}" else packageDB.name
    val quarter = Quarter(newName, size, 10.0, size, 50.0)
    if (settings.circleColorScheme == ColorScheme.UML) {
        quarter.setColor(UMLColorScheme.PACKAGE.color)
    }
    city.addObject(quarter)
    dataBaseUtil.getClassIdsByPackageId(modelId, id)
        .forEach { generateClass(quarter, dataBaseUtil, it, modelId) }
    dataBaseUtil.getInterfacesIdsByPackageId(modelId, id)
        .forEach { generateInterface(quarter, dataBaseUtil, it, modelId) }
    dataBaseUtil.getEnumerationsIdsByPackageId(modelId, id)
        .forEach { generateEnumeration(quarter, dataBaseUtil, it, modelId) }
    packageDB.childrenId.forEach { generatePackage(city, newName, dataBaseUtil, it, modelId) }
}

private fun generateClass(quarter: Quarter, dataBaseUtil: DataBaseUtil, id: Int, modelId: Int) {
    val classDB = dataBaseUtil.getClassFull(id, modelId)
    val methods = classDB.methodCount.toDouble() + 1
    val side = classDB.size.toDouble() / 20
    val name = if (classDB.nestedIn != 0)
        "${dataBaseUtil.getClass(classDB.nestedIn, modelId).name}.${classDB.name}"
    else
        classDB.name
    val building = Building(name, side, settings.cityMethodFactor * methods, side)
    building.filePath = classDB.filePath
    quarter.addObject(building)
    if (settings.circleColorScheme == ColorScheme.UML) {
        building.setColor(UMLColorScheme.CLASS.color)
    }
}

private fun generateInterface(quarter: Quarter, dataBaseUtil: DataBaseUtil, id: Int, modelId: Int) {
    val interfaceDB = dataBaseUtil.getInterface(id, modelId)
    val methods = interfaceDB.methodCount.toDouble() + 1
    val side = interfaceDB.size.toDouble() / 20
    val building = Building(interfaceDB.name, side, settings.cityMethodFactor * methods, side)
    building.filePath = interfaceDB.filePath
    quarter.addObject(building)
    if (settings.circleColorScheme == ColorScheme.UML) {
        building.setColor(UMLColorScheme.INTERFACE.color)
    }
}

private fun generateEnumeration(quarter: Quarter, dataBaseUtil: DataBaseUtil, id: Int, modelId: Int) {
    val enumerationDB = dataBaseUtil.getEnumerations(id, modelId)
    val side = enumerationDB.size.toDouble() / 20
    val building = Building(enumerationDB.name, side, settings.cityMethodFactor.toDouble(), side)
    building.filePath = enumerationDB.filePath
    quarter.addObject(building)
    if (settings.circleColorScheme == ColorScheme.UML) {
        building.setColor(UMLColorScheme.ENUM.color)
    }
}