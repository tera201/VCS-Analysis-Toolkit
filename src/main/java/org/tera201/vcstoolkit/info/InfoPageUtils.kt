package org.tera201.vcstoolkit.info

import org.tera201.vcstoolkit.tabs.*
import org.tera201.vcstoolkit.utils.getBranchName
import javax.swing.tree.DefaultMutableTreeNode

fun getCirclePath(tabManager: TabManager):String? {
    val fxCircleTab = tabManager.getTabMap()[TabEnum.CIRCLE] as FXCircleTab?
    val circleSelectionManager = fxCircleTab!!.fxCircle.circleSpace.selectionManager
    return circleSelectionManager.selected?.filePath
}

fun getCircleBranch(tabManager: TabManager):String? {
    val fxCircleTab = tabManager.getTabMap()[TabEnum.CIRCLE] as FXCircleTab?
    val circleSelectionManager = fxCircleTab!!.fxCircle.circleSpace.selectionManager
    return circleSelectionManager.selected?.getBranchName()
}

fun getCityPath(tabManager: TabManager):String? {
    val fxCityTab = tabManager.getTabMap()[TabEnum.CITY] as FXCityTab?
    val citySelectionManager = fxCityTab!!.fxCity.citySpace.selectionManager
    return citySelectionManager.selected?.filePath
}

fun getCityBranch(tabManager: TabManager):String? {
    val fxCityTab = tabManager.getTabMap()[TabEnum.CITY] as FXCityTab?
    return fxCityTab!!.modelComboBox.selectedItem?.toString()
}

fun getGitPath(tabManager: TabManager):String? {
    val gitTab = tabManager.getTabMap()[TabEnum.GIT] as GitTab?
    val  pathComponent = gitTab!!.pathJTree.selectionPath?.lastPathComponent
    return (pathComponent as? DefaultMutableTreeNode)?.userObject?.toString()
}

fun getPathByTab(tabManager: TabManager):String? {
    when (tabManager.getSelectedTabTitle()) {
        TabEnum.GIT.value -> return getGitPath(tabManager)
        TabEnum.CIRCLE.value -> return getCirclePath(tabManager)
        TabEnum.CITY.value -> return getCityPath(tabManager)
        else -> return null
    }
}

fun checkoutByTab(tabManager: TabManager){
    val gitTab = tabManager.getTabMap()[TabEnum.GIT] as GitTab?
    when (tabManager.getSelectedTabTitle()) {
        TabEnum.CITY.value -> getCityBranch(tabManager)?.let { gitTab!!.checkoutTo(it) }
        TabEnum.CIRCLE.value -> getCircleBranch(tabManager)?.let { gitTab!!.checkoutTo(it) }
    }
}