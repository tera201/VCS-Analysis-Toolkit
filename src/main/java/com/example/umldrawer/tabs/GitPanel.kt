package com.example.umldrawer.tabs

import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JTextField

public fun createGit(): JPanel?{
    val gitPanel: JPanel = object : JPanel() {}
    initGit(gitPanel)
    return gitPanel
}

private fun initGit(gitJPanel: JPanel) {
    gitJPanel.layout = FlowLayout()
    val button = JButton("Get")
    val urlField = JTextField()
    urlField.preferredSize = Dimension(300, 20)
    gitJPanel.add(urlField)
    gitJPanel.add(button)
}