package org.tera201.vcstoolkit.tabs

import com.intellij.notification.Notification
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.application.ApplicationManager
import org.tera201.vcstoolkit.helpers.SharedModel
import org.tera201.vcstoolkit.services.VCSToolkitCache
import org.tera201.vcstoolkit.services.settings.VCSToolkitSettings
import java.awt.Dimension
import java.awt.FlowLayout
import java.io.File
import java.io.IOException
import javax.swing.*

class GitTab(private val tabManager: TabManager, val modelListContent:SharedModel) : JPanel() {
    private var settings: VCSToolkitSettings = VCSToolkitSettings.getInstance()
    private var cache: VCSToolkitCache = VCSToolkitCache.getInstance(tabManager.getCurrentProject())
    val controller: GitTabController

    private val notificationGroup = NotificationGroupManager.getInstance().getNotificationGroup("VCSToolkitNotify")
    
    init {
        try {
            File(settings.repoPath).mkdirs()
            File(settings.modelPath).mkdirs()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        this.layout = FlowLayout(FlowLayout.LEFT)

//        ApplicationManager.getApplication().messageBus.connect()
//            .subscribe(VCSToolkitSettings.SettingsChangedListener.TOPIC, object :
//                VCSToolkitSettings.SettingsChangedListener {
//                override fun onSettingsChange(settings: VCSToolkitSettings) {
//                    logsJBScrollPane.isVisible = settings.showGitLogs
//                    clearLogButton.isVisible = settings.showGitLogs
//                    createDirectoryIfNotExists(settings.repoPath)
//                    createDirectoryIfNotExists(settings.modelPath)
//                    if (settings.externalProjectMode == 1 &&  cache.projectPathMap[projectComboBox.selectedItem]!!.isExternal) {
//                        branchListModel.clear()
//                        tagListModel.clear()
//                    } else {
//                        projectComboBox.selectedItem = cache.lastProject
//                    }
//                    buildCircle()
//                }
//            })

        this.minimumSize = Dimension(0, 200)
        val ui = GitTabUI(modelListContent)
        ui.createUI(this)
        controller = GitTabController(ui, cache, settings, tabManager)
    }
}