package org.tera201.vcstoolkit.tabs

import org.tera201.vcstoolkit.helpers.SharedModel
import org.tera201.vcstoolkit.services.VCSToolkitCache
import org.tera201.vcstoolkit.services.settings.VCSToolkitSettings
import org.tera201.vcstoolkit.tabs.git.GitTabController
import org.tera201.vcstoolkit.tabs.git.GitTabUI
import java.awt.Dimension
import java.awt.FlowLayout
import java.io.File
import java.io.IOException
import javax.swing.*

class GitTab(private val tabManager: TabManager, val modelListContent:SharedModel) : JPanel() {
    private var settings: VCSToolkitSettings = VCSToolkitSettings.getInstance()
    private var cache: VCSToolkitCache = VCSToolkitCache.getInstance(tabManager.getCurrentProject())
    val controller: GitTabController
    
    init {
        try {
            File(settings.repoPath).mkdirs()
            File(settings.modelPath).mkdirs()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        this.layout = FlowLayout(FlowLayout.LEFT)

        this.minimumSize = Dimension(0, 200)
        val ui = GitTabUI(modelListContent)
        ui.createUI(this)
        controller = GitTabController(ui, cache, settings, tabManager)
    }
}