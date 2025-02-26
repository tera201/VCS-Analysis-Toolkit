package org.tera201.vcstoolkit.action

import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.components.JBTabbedPane
import org.tera201.vcstoolkit.helpers.FullScreenTabInfo
import org.tera201.vcstoolkit.tabs.TabManager

class ActionManager(val jtp: JBTabbedPane, private val toolWindow: ToolWindow, private val tabManager: TabManager) {
    val openedFxTabs = HashMap<String, FullScreenTabInfo>()
    private val expandTabAction = ExpandTabAction(this)
    private val collapseTabAction = CollapseTabAction(this)
    private val showSettingsAction = ShowSettingsAction()
    private val infoTabAction = InfoTabAction(this, tabManager)

    private val buttonsUmlWithExpand =  listOf(expandTabAction, showSettingsAction)
    private val buttonsUmlWithCollapse = listOf(collapseTabAction, showSettingsAction)
    private val buttonsFxWithExpand = listOf(infoTabAction, expandTabAction, showSettingsAction)
    private val buttonsFxWithCollapse = listOf(infoTabAction, collapseTabAction, showSettingsAction)
    private val settingButton = listOf(infoTabAction, showSettingsAction)

    init {
        setToolBarForUMLPageExpand()
    }

    fun setToolBarForUMLPageExpand() {
        toolWindow.setTitleActions(buttonsUmlWithExpand)
    }

    fun setToolBarForUMLPageCollapse() {
        toolWindow.setTitleActions(buttonsUmlWithCollapse)
    }

    fun setDefaultToolBar() {
        toolWindow.setTitleActions(settingButton)
    }

    fun setToolBarWithExpand() {
        toolWindow.setTitleActions(buttonsFxWithExpand)
        infoTabAction
    }

    fun setToolBarWithCollapse() {
        toolWindow.setTitleActions(buttonsFxWithCollapse)
    }
}