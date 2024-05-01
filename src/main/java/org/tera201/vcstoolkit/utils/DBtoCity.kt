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
    dataBaseUtil.getRootPackageIds(modelId).forEach { generatePackage(citySpace, null, dataBaseUtil, it) }
}

private fun generatePackage(citySpace: FXSpace<Quarter>, parentName: String?, dataBaseUtil: DataBaseUtil, id:Int) {
    val packageDB = dataBaseUtil.getPackage(id)
    val size = packageDB.size.toDouble()
    val newName = if (parentName.orEmpty().isNotEmpty())  "$parentName.${packageDB.name}" else packageDB.name
    val quarter = Quarter(newName, size, 10.0, size, 50.0)
    citySpace.mainObject.addObject(quarter)
//    quarter.filePath = packageDB.filePath
    dataBaseUtil.getClassIdsByPackageId(id).forEach { generateClass(quarter, dataBaseUtil, it) }
    dataBaseUtil.getInterfacesIdsByPackageId(id).forEach { generateInterface(quarter, dataBaseUtil, it) }
    dataBaseUtil.getEnumerationsIdsByPackageId(id).forEach { generateEnumeration(quarter, dataBaseUtil, it) }
    packageDB.childrenId.forEach { generatePackage(citySpace, newName, dataBaseUtil, it) }
}

private fun generateClass(quarter: Quarter?, dataBaseUtil: DataBaseUtil, id:Int) {
    val classDB = dataBaseUtil.getClass(id)
    val methods = classDB.methodCount.toDouble() + 1
    val side = classDB.size.toDouble() / 20
    val building = Building(classDB.name, side, 10 * methods, side)
    building.filePath = classDB.filePath
    building.info = """
    """.trimIndent()
    quarter?.addObject(building)
}

private fun generateInterface(quarter: Quarter?, dataBaseUtil: DataBaseUtil, id:Int) {
    val interfaceDB = dataBaseUtil.getInterface(id)
    val methods = interfaceDB.methodCount.toDouble() + 1
    val side = interfaceDB.size.toDouble() / 20
    val building = Building(interfaceDB.name, side, 10 * methods, side)
    building.filePath = interfaceDB.filePath
    quarter?.addObject(building)
}

private fun generateEnumeration(quarter: Quarter?, dataBaseUtil: DataBaseUtil, id:Int) {
    val enumerationDB = dataBaseUtil.getEnumerations(id)
    val building = Building(enumerationDB.name, 100.0, 900.0, 100.0)
    building.filePath = enumerationDB.filePath
    quarter?.addObject(building)
}