package com.example.umldrawer.settings

import com.intellij.openapi.options.Configurable
import java.awt.Button
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JPanel

class UMLToolkitSettingsPage : Configurable {


    private var panel: JPanel
    init {
        panel = JPanel()
        panel.size = Dimension(400, 400)
    }

    override fun createComponent(): JComponent {
        return panel
    }

    override fun isModified(): Boolean {
        return false
    }

    override fun apply() {
        TODO("Not yet implemented")
    }

    override fun getDisplayName(): String {
        return "UML Toolkit"
    }
}