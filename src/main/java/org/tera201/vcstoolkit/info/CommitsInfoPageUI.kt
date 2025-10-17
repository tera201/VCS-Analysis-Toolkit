package org.tera201.vcstoolkit.info

import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBPanel
import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridLayoutManager
import org.tera201.vcstoolkit.helpers.addNComponentsRow
import org.tera201.vcstoolkit.tabs.TabManager
import javax.swing.JButton

class CommitsInfoPageUI(val tabManager: TabManager) {
    val panel = JBPanel<JBPanel<*>>(GridLayoutManager(1, 4))
    private val emailComboBox = ComboBox<String>()
    private val branchFilter = ComboBox<String>()
    private val commitFilter = ComboBox<String>()
    private val filterConfigButton = JButton("Get")
    val filterLine = JBPanel<JBPanel<*>>(GridLayoutManager(1, 4)).apply {
        this.add(emailComboBox, GridConstraints().apply { row = 0; column = 0})
        this.add(branchFilter, GridConstraints().apply { row = 0; column = 1 })
        this.add(commitFilter, GridConstraints().apply { row = 0; column = 2 })
        this.add(filterConfigButton, GridConstraints().apply { row = 0; column = 3 })
    }

    init {
        panel.apply {
            addNComponentsRow(0, filterLine to false)
        }
    }
}