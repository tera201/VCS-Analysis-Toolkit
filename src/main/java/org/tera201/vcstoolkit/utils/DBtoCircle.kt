package org.tera201.vcstoolkit.utils

import org.tera201.code2uml.util.messages.*
import org.tera201.elements.FXSpace
import org.tera201.elements.circle.ClassCircle
import org.tera201.elements.circle.HollowCylinder
import org.tera201.elements.circle.PackageCircle

private const val height = 500.0
private const val gap = 8000.0
fun toCircle(circleSpace: FXSpace<HollowCylinder>, number: Int=0, modelId:Int, dataBaseUtil: DataBaseUtil) {
    val model = dataBaseUtil.getModel(modelId)
    val packageCircle = PackageCircle(model.name, 6000.0, 5500.0, height)
    packageCircle.translateY = number * gap
    packageCircle.filePath = model.filePath
    circleSpace.add(packageCircle)
    dataBaseUtil.getRootPackageIds(modelId).forEach { generatePackage(packageCircle, dataBaseUtil, it, modelId) }
}

private fun generatePackage(circleParent: PackageCircle, dataBaseUtil: DataBaseUtil, id:Int, modelId: Int) {
    val packageDB = dataBaseUtil.getPackage(id, modelId)
    val classes = dataBaseUtil.getClassIdsByPackageId(modelId, id)
    val interfaces = dataBaseUtil.getInterfacesIdsByPackageId(modelId, id)
    val enumerations = dataBaseUtil.getEnumerationsIdsByPackageId(modelId, id)
    if (classes.isEmpty() and interfaces.isEmpty() and enumerations.isEmpty()) {
        packageDB.childrenId.forEach { generatePackage(circleParent, dataBaseUtil, it, modelId) }
    } else {
        val packageCircle = PackageCircle(packageDB.packageName, circleParent.innerRadius/2 + 500, circleParent.innerRadius/2, height)
//        packageCircle.filePath = packageDB.filePath
        circleParent.addObject(packageCircle)
        classes.forEach { generateClass(packageCircle, dataBaseUtil, it, modelId) }
        interfaces.forEach { generateInterface(packageCircle, dataBaseUtil, it, modelId) }
        enumerations.forEach { generateEnumeration(packageCircle, dataBaseUtil, it, modelId) }
        packageDB.childrenId.forEach { generatePackage(packageCircle, dataBaseUtil, it, modelId) }
    }
}

private fun generateClass(circleParent: PackageCircle, dataBaseUtil: DataBaseUtil, classId: Int, modelId: Int) {
    val classDB = dataBaseUtil.getClass(classId, modelId)
    val methods = if (classDB.methodCount > 0) (classDB.methodCount.toDouble() + 1) * 10 else 10.0
    val side =  classDB.size.toDouble() / 2
    val classCircle = ClassCircle(classDB.name, side + methods * 10, side, height)
    classCircle.filePath = classDB.filePath
    circleParent.addObject(classCircle)
}

private fun generateInterface(circleParent: PackageCircle, dataBaseUtil: DataBaseUtil, interfaceId: Int, modelId: Int) {
    val interfaceDB = dataBaseUtil.getInterface(interfaceId, modelId)
    val size = if (interfaceDB.size == 0L) 700.0 else interfaceDB.size.toDouble()
    val methods = if (interfaceDB.methodCount > 0) (interfaceDB.methodCount.toDouble() + 1) * 10 else 10.0
    val side = size / 2
    val classCircle = ClassCircle(interfaceDB.name, side + methods * 10, side, height)
    classCircle.filePath = interfaceDB.filePath
    circleParent.addObject(classCircle)
}

private fun generateEnumeration(circleParent: PackageCircle, dataBaseUtil: DataBaseUtil, enumerationId: Int, modelId: Int) {
    val enumerationDB = dataBaseUtil.getEnumerations(enumerationId, modelId)
    val classCircle = ClassCircle(enumerationDB.name, 1000.0, 700.0, height)
    classCircle.filePath = enumerationDB.filePath
    circleParent.addObject(classCircle)
}