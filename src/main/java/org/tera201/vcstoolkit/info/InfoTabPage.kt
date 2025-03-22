package org.tera201.vcstoolkit.info

import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTabbedPane
import org.repodriller.scm.entities.CommitSize
import org.repodriller.scm.entities.DeveloperInfo
import org.tera201.vcstoolkit.helpers.getPathByTab
import org.tera201.vcstoolkit.tabs.GitTab
import org.tera201.vcstoolkit.tabs.TabEnum
import org.tera201.vcstoolkit.tabs.TabManager
import kotlin.concurrent.thread

class InfoTabPage(val tabManager: TabManager) : JBTabbedPane() {
    private val infoPage = InfoPageUI(tabManager)
    private val authorPage = AuthorInfoPageUI(tabManager)
    private val jBScrollPane1: JBScrollPane = JBScrollPane(infoPage.panel)
    private val jBScrollPane2: JBScrollPane = JBScrollPane(authorPage.panel)

    init {
        jBScrollPane1.setBorder(null);
        jBScrollPane1.setHorizontalScrollBarPolicy(JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER)
        jBScrollPane1.setVerticalScrollBarPolicy(JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED)
        jBScrollPane2.setBorder(null);
        jBScrollPane2.setHorizontalScrollBarPolicy(JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER)
        jBScrollPane2.setVerticalScrollBarPolicy(JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED)
        add("Main", jBScrollPane1)
        add("Authors", jBScrollPane2)
    }

    fun start() {
        val gitTab = tabManager.getTabMap()[TabEnum.GIT] as GitTab?
        val path = getPathByTab(tabManager)
        val lastPathNode = if ((path != null)) path.substring(path.lastIndexOf("/") + 1) else null

        gitTab!!.controller.myRepo!!.scm.dbPrepared()

        val commitSizeMap: Map<String, CommitSize> = gitTab.controller.myRepo!!.scm.repositorySize(path)

        //        BlameManager blameManager  = gitTab.getMyRepo().getScm().blameManager();
        val developerInfoMap: Map<String, DeveloperInfo> = gitTab.controller.myRepo!!.scm.getDeveloperInfo(path)
        thread { infoPage.open(commitSizeMap, developerInfoMap) }
        thread { authorPage.open(commitSizeMap, developerInfoMap) }
    }


}