package org.tera201.vcstoolkit.info

import com.intellij.ui.JBSplitter
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import org.repodriller.scm.entities.CommitSize
import org.repodriller.scm.entities.DeveloperInfo
import org.tera201.vcstoolkit.tabs.TabManager
import java.util.*
import javax.swing.*
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener

class AuthorTabPage(val tabManager: TabManager): JBSplitter() {
    var listModel: DefaultListModel<String> = DefaultListModel()
    var authorList: JList<String> = JBList(listModel)
    var listScrollPane: JScrollPane? = JBScrollPane(authorList)

    init {
//        this.setTabPlacement(RIGHT)
        this.autoscrolls = true
        orientation = false
        dividerWidth = 1
//        this.setTabLayoutPolicy(SCROLL_TAB_LAYOUT)
    }


    @Throws(InterruptedException::class)
    fun create(commitSizeMap: Map<String, CommitSize>, developerInfoMap: Map<String, DeveloperInfo>) {
        val authorInfoPage = AuthorInfoPage(tabManager)
        secondComponent = listScrollPane
        firstComponent = authorInfoPage.component
        developerInfoMap.forEach {
            SwingUtilities.invokeLater {
                listModel.addElement(it.key)
            }
        }
        authorList.addListSelectionListener(ListSelectionListener { e: ListSelectionEvent ->
            if (!e.valueIsAdjusting) {
                val authorEmail = authorList.selectedValue
                authorInfoPage.open(authorEmail, commitSizeMap, developerInfoMap)
                this.updateUI()
            }
        })
        authorList.selectedIndex = 0
    }


}