package org.tera201.vcstoolkit.utils

import org.tera201.code2uml.util.messages.*
import org.tera201.elements.FXSpace
import org.tera201.elements.city.Building
import org.tera201.elements.city.City
import org.tera201.elements.city.Quarter

fun toCity(citySpace: FXSpace<Quarter>, modelId:Int, dataBaseUtil: DataBaseUtil) {
    val model = dataBaseUtil.getModel(modelId)
    var city = City(8000.0, 20.0, 8000.0, model.name)
    citySpace.add(city)
    dataBaseUtil.getRootPackageIds(modelId).forEach { generatePackage(city, null, dataBaseUtil, it, modelId) }
}

private fun generatePackage(city: City, parentName: String?, dataBaseUtil: DataBaseUtil, id:Int, modelId: Int) {
    val packageDB = dataBaseUtil.getPackage(id, modelId)
    val size = packageDB.size.toDouble()
    val newName = if (parentName.orEmpty().isNotEmpty())  "$parentName.${packageDB.name}" else packageDB.name
    val quarter = Quarter(newName, size, 10.0, size, 50.0)
    city.addObject(quarter)
    dataBaseUtil.getClassIdsByPackageId(modelId, id).forEach { generateClass(quarter, dataBaseUtil, it, modelId) }
    dataBaseUtil.getInterfacesIdsByPackageId(modelId, id).forEach { generateInterface(quarter, dataBaseUtil, it, modelId) }
    dataBaseUtil.getEnumerationsIdsByPackageId(modelId, id).forEach { generateEnumeration(quarter, dataBaseUtil, it, modelId) }
    packageDB.childrenId.forEach { generatePackage(city, newName, dataBaseUtil, it, modelId) }
}

private fun generateClass(quarter: Quarter?, dataBaseUtil: DataBaseUtil, id:Int, modelId: Int) {
    val classDB = dataBaseUtil.getClass(id, modelId)
    val methods = classDB.methodCount.toDouble() + 1
    val side = classDB.size.toDouble() / 20
    val building = Building(classDB.name, side, 10 * methods, side)
    building.filePath = classDB.filePath
    building.info = """
    """.trimIndent()
    quarter?.addObject(building)
}

private fun generateInterface(quarter: Quarter?, dataBaseUtil: DataBaseUtil, id:Int, modelId: Int) {
    val interfaceDB = dataBaseUtil.getInterface(id, modelId)
    val methods = interfaceDB.methodCount.toDouble() + 1
    val side = interfaceDB.size.toDouble() / 20
    val building = Building(interfaceDB.name, side, 10 * methods, side)
    building.filePath = interfaceDB.filePath
    quarter?.addObject(building)
}

private fun generateEnumeration(quarter: Quarter?, dataBaseUtil: DataBaseUtil, id:Int, modelId: Int) {
    val enumerationDB = dataBaseUtil.getEnumerations(id, modelId)
    val building = Building(enumerationDB.name, 100.0, 900.0, 100.0)
    building.filePath = enumerationDB.filePath
    quarter?.addObject(building)
}