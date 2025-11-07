package org.tera201.vcstoolkit.info

import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTabbedPane
import org.tera201.vcsmanager.db.entities.CommitSize
import org.tera201.vcsmanager.db.entities.DeveloperInfo
import org.tera201.vcstoolkit.tabs.GitTab
import org.tera201.vcstoolkit.tabs.TabEnum
import org.tera201.vcstoolkit.tabs.TabManager
import kotlin.concurrent.thread

class InfoTabPage(val tabManager: TabManager) : JBTabbedPane() {
    private val infoPage = InfoPageUI(tabManager)
    private val authorPage = AuthorInfoPageUI(tabManager)
    private val commitsPage = CommitsInfoPageUI(tabManager)
    private val jBScrollPane1: JBScrollPane = JBScrollPane(infoPage.panel)
    private val jBScrollPane2: JBScrollPane = JBScrollPane(authorPage.panel)
    private val jBScrollPane3: JBScrollPane = JBScrollPane(commitsPage.panel)

    init {
        jBScrollPane1.setBorder(null)
        jBScrollPane1.setHorizontalScrollBarPolicy(JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER)
        jBScrollPane1.setVerticalScrollBarPolicy(JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED)
        jBScrollPane2.setBorder(null)
        jBScrollPane2.setHorizontalScrollBarPolicy(JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER)
        jBScrollPane2.setVerticalScrollBarPolicy(JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED)
        jBScrollPane3.setBorder(null);
        jBScrollPane3.setHorizontalScrollBarPolicy(JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER)
        jBScrollPane3.setVerticalScrollBarPolicy(JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED)
        add("Main", jBScrollPane1)
        add("Authors", jBScrollPane2)
        add("Commits", jBScrollPane3)
    }

    fun start() {
        val gitTab = tabManager.getTabMap()[TabEnum.GIT] as GitTab?
        val path = getPathByTab(tabManager)
        gitTab!!.controller.myRepo!!.scm.dbPrepared(tabManager.project)

        val commitSizeMap: Map<String, CommitSize> = gitTab.controller.myRepo!!.scm.getRepositorySize(true, null, path)
        val developerInfoMap: Map<String, DeveloperInfo> = gitTab.controller.myRepo!!.scm.getDeveloperInfo(path)

        if (developerInfoMap.isEmpty() || commitSizeMap.isEmpty()) throw Exception("Not found git info! Path: $path")
        thread { infoPage.open(commitSizeMap, developerInfoMap) }
        thread { authorPage.open(commitSizeMap, developerInfoMap) }
        thread { commitsPage.open(gitTab.controller.myRepo!!.scm) }
    }


}