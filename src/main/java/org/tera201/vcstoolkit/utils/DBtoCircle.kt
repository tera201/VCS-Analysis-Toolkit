package org.tera201.vcstoolkit.utils

import org.tera201.code2uml.util.messages.*
import org.tera201.elements.FXSpace
import org.tera201.elements.circle.ClassCircle
import org.tera201.elements.circle.HollowCylinder
import org.tera201.elements.circle.PackageCircle
import org.tera201.vcstoolkit.services.colors.UMLColorScheme
import org.tera201.vcstoolkit.services.colors.ColorScheme
import org.tera201.vcstoolkit.services.settings.VCSToolkitSettings

private const val height = 500.0
private const val gap = 1000.0
private const val defaultPackageWidth = 100

fun toCircle(circleSpace: FXSpace<HollowCylinder>, number: Int = 0, modelId: Int, dataBaseUtil: DataBaseUtil) {
    val model = dataBaseUtil.getModel(modelId)
    val radiusInner = 6000.0
    val radiusOuter = radiusInner + defaultPackageWidth * getSettings().circlePackageFactor
    val packageCircle = PackageCircle(model.name, radiusOuter, radiusInner, height * getSettings().circleHeightFactor)
    packageCircle.translateY = number * gap * getSettings().circleGapFactor
    packageCircle.filePath = model.filePath
    circleSpace.add(packageCircle)
    dataBaseUtil.getRootPackageIds(modelId).forEach { generatePackage(packageCircle, dataBaseUtil, it, modelId) }
}

private fun getSettings():VCSToolkitSettings = VCSToolkitSettings.getInstance()

private fun generatePackage(circleParent: PackageCircle, dataBaseUtil: DataBaseUtil, id: Int, modelId: Int) {
    val packageDB = dataBaseUtil.getPackage(id, modelId)
    val classes = dataBaseUtil.getClassIdsByPackageId(modelId, id)
    val interfaces = dataBaseUtil.getInterfacesIdsByPackageId(modelId, id)
    val enumerations = dataBaseUtil.getEnumerationsIdsByPackageId(modelId, id)
    if (classes.isEmpty() and interfaces.isEmpty() and enumerations.isEmpty() and (packageDB.childrenId.size <= 1)) {
        packageDB.childrenId.forEach { generatePackage(circleParent, dataBaseUtil, it, modelId) }
    } else {
        val radiusInner = circleParent.innerRadius / 2
        val radiusOuter = radiusInner + defaultPackageWidth * getSettings().circlePackageFactor
        val packageCircle = PackageCircle(packageDB.packageName, radiusOuter, radiusInner, height * getSettings().circleHeightFactor)

        if (getSettings().circleColorScheme == ColorScheme.UML) {
            packageCircle.setColor(UMLColorScheme.PACKAGE.color)
        }
        circleParent.addObject(packageCircle)
        classes.forEach { generateClass(packageCircle, dataBaseUtil, it, modelId) }
        interfaces.forEach { generateInterface(packageCircle, dataBaseUtil, it, modelId) }
        enumerations.forEach { generateEnumeration(packageCircle, dataBaseUtil, it, modelId) }
        packageDB.childrenId.forEach { generatePackage(packageCircle, dataBaseUtil, it, modelId) }
    }
}

private fun generateClass(circleParent: PackageCircle, dataBaseUtil: DataBaseUtil, classId: Int, modelId: Int) {
    val classDB = dataBaseUtil.getClassFull(classId, modelId)
    val methods = classDB.methodCount.toDouble() + 1
    val radiusInner = classDB.size.toDouble() / 2
    val radiusOuter = radiusInner + methods * getSettings().circleMethodFactor
    val name = if (classDB.nestedIn != 0)
        "${dataBaseUtil.getClass(classDB.nestedIn, modelId).name}.${classDB.name}"
    else
        classDB.name
    val classCircle = ClassCircle(name, radiusOuter, radiusInner, height * getSettings().circleHeightFactor)
    classCircle.filePath = classDB.filePath
    circleParent.addObject(classCircle)
    if (getSettings().circleColorScheme == ColorScheme.UML) {
        classCircle.setColor(UMLColorScheme.CLASS.color)
    }
}

private fun generateInterface(circleParent: PackageCircle, dataBaseUtil: DataBaseUtil, interfaceId: Int, modelId: Int) {
    val interfaceDB = dataBaseUtil.getInterface(interfaceId, modelId)
    val size = if (interfaceDB.size == 0L) 700.0 else interfaceDB.size.toDouble()
    val methods = interfaceDB.methodCount.toDouble() + 1
    val radiusInner = size / 2
    val radiusOuter = radiusInner + methods * getSettings().circleMethodFactor
    val classCircle = ClassCircle(interfaceDB.name, radiusOuter, radiusInner,height * getSettings().circleHeightFactor)
    classCircle.filePath = interfaceDB.filePath
    circleParent.addObject(classCircle)
    if (getSettings().circleColorScheme == ColorScheme.UML) {
        classCircle.setColor(UMLColorScheme.INTERFACE.color)
    }
}

private fun generateEnumeration(
    circleParent: PackageCircle,
    dataBaseUtil: DataBaseUtil,
    enumerationId: Int,
    modelId: Int
) {
    val enumerationDB = dataBaseUtil.getEnumerations(enumerationId, modelId)
    val radiusInner = enumerationDB.size.toDouble() / 20
    val radiusOuter = radiusInner + getSettings().circleMethodFactor
    val classCircle =
        ClassCircle(enumerationDB.name,  radiusOuter, radiusInner, height * getSettings().circleHeightFactor)
    classCircle.filePath = enumerationDB.filePath
    circleParent.addObject(classCircle)
    if (getSettings().circleColorScheme == ColorScheme.UML) {
        classCircle.setColor(UMLColorScheme.ENUM.color)
    }
}