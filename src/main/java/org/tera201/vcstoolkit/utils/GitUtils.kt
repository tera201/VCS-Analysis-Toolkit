package org.tera201.vcstoolkit.utils

import org.tera201.elements.Selectable

fun Selectable.getBranchName(): String {
    return objectPath.substringBefore(":")
}

fun Selectable.getFullPackage(): String {
    return objectPath.substringAfter(":").replace(":", "/").replace(".", "/")
}