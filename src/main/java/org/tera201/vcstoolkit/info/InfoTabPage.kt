package org.tera201.vcstoolkit.info

import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTabbedPane
import org.repodriller.scm.entities.CommitSize
import org.repodriller.scm.entities.DeveloperInfo
import org.tera201.vcstoolkit.tabs.GitTab
import org.tera201.vcstoolkit.tabs.TabEnum
import org.tera201.vcstoolkit.tabs.TabManager

class InfoTabPage(val tabManager: TabManager): JBTabbedPane() {
    val infoPage = InfoPage(tabManager)
    val authorTab = AuthorTabPage(tabManager)
    val jBScrollPane1: JBScrollPane = JBScrollPane(infoPage.component)
    val jBScrollPane2: JBScrollPane = JBScrollPane(authorTab)

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

        var startTime = System.currentTimeMillis()
        gitTab!!.myRepo!!.scm.dbPrepared()
        var endTime = System.currentTimeMillis()
        var executionTime = endTime - startTime
        println("dbPrepared выполнился за $executionTime мс")

        startTime = System.currentTimeMillis()
        val commitSizeMap: Map<String, CommitSize> = gitTab!!.myRepo!!.scm.repositorySize(path)
        endTime = System.currentTimeMillis()
        executionTime = endTime - startTime
        println("commitSizeMap выполнился за $executionTime мс")


        //        BlameManager blameManager  = gitTab.getMyRepo().getScm().blameManager();
        startTime = System.currentTimeMillis()
        val developerInfoMap: Map<String, DeveloperInfo> = gitTab.myRepo!!.scm.getDeveloperInfo(path)
        endTime = System.currentTimeMillis()
        executionTime = endTime - startTime
        println("developerInfoMap выполнился за $executionTime мс")
        infoPage.open(commitSizeMap, developerInfoMap)
        authorTab.create(commitSizeMap, developerInfoMap)
    }


}