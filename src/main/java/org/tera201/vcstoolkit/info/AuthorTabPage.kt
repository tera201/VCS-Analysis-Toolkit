package org.tera201.vcstoolkit.info

import org.repodriller.scm.entities.CommitSize
import org.repodriller.scm.entities.DeveloperInfo
import org.tera201.vcstoolkit.tabs.TabManager
import javax.swing.JTabbedPane
import javax.swing.SwingUtilities

class AuthorTabPage(val tabManager: TabManager): JTabbedPane() {

    init {
        this.setTabPlacement(RIGHT)
        this.autoscrolls = true
        this.setTabLayoutPolicy(SCROLL_TAB_LAYOUT)
    }


    @Throws(InterruptedException::class)
    fun open(commitSizeMap: Map<String, CommitSize>, developerInfoMap: Map<String, DeveloperInfo>) {
        developerInfoMap.forEach {
            val authorInfoPage = AuthorInfoPage(tabManager, it.key)
            SwingUtilities.invokeLater {
                this.add(it.value.name, authorInfoPage.component)
                authorInfoPage.open(commitSizeMap, developerInfoMap)
            }
        }
        this.validate()
        this.repaint()
    }


}