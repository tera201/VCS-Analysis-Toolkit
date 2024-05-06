package org.tera201.vcstoolkit.info

import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTabbedPane
import org.tera201.vcstoolkit.tabs.TabManager

class InfoTabPage(val tabManager: TabManager): JBTabbedPane() {
    val infoPage = InfoPage(tabManager)
    val jBScrollPane: JBScrollPane = JBScrollPane(infoPage.component)

    init {
        jBScrollPane.setBorder(null);
        jBScrollPane.setHorizontalScrollBarPolicy(JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER)
        jBScrollPane.setVerticalScrollBarPolicy(JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED)
        add("Main", jBScrollPane)
    }

    fun start() {
        infoPage.open()
    }


}