package org.tera201.vcstoolkit.info

import org.repodriller.scm.entities.CommitSize
import org.repodriller.scm.entities.DeveloperInfo
import org.tera201.vcstoolkit.tabs.TabManager
import java.awt.BorderLayout
import java.util.*
import javax.swing.*


class AuthorTabPage(val tabManager: TabManager) : JPanel() {
    init {
        this.setLayout(BorderLayout())
        this.autoscrolls = true
    }


    @Throws(InterruptedException::class)
    fun create(commitSizeMap: Map<String, CommitSize>, developerInfoMap: Map<String, DeveloperInfo>) {
        val authorInfoPage = AuthorInfoPage(tabManager)
        this.add(authorInfoPage.component, BorderLayout.CENTER)
        developerInfoMap.forEach {
            authorInfoPage.emailComboBox.addItem(it.key)
        }
        authorInfoPage.emailComboBox.addActionListener {
            val selectedEmail = authorInfoPage.emailComboBox.selectedItem as String
            authorInfoPage.open(selectedEmail, commitSizeMap, developerInfoMap)
        }
    }


}