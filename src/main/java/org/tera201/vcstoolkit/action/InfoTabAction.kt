package org.tera201.vcstoolkit.action

import com.intellij.notification.Notification
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.impl.JComponentEditorProviderUtils
import com.intellij.openapi.project.DumbAwareAction
import icons.MyIcons
import javafx.embed.swing.JFXPanel
import org.tera201.vcstoolkit.helpers.FullScreenTabInfo
import org.tera201.vcstoolkit.info.InfoTabPage
import org.tera201.vcstoolkit.tabs.*
import java.util.function.Supplier
import kotlin.concurrent.thread

class InfoTabAction(private val actionManager: ActionManager, private val tabManager: TabManager) : DumbAwareAction(Supplier {"Open stat"}, MyIcons.VCS) {
    private val notificationGroup = NotificationGroupManager.getInstance().getNotificationGroup("VCSToolkitNotify")
    override fun actionPerformed(event: AnActionEvent) {
        val selectedTabTitle = tabManager.getSelectedTabTitle()

        val gitTab = tabManager.getTabMap()[TabEnum.GIT] as GitTab?
        val fxCityTab = tabManager.getTabMap()[TabEnum.CITY] as FXCityTab?
        val fxCircleTab = tabManager.getTabMap()[TabEnum.CIRCLE] as FXCircleTab?

        val citySelected = fxCityTab!!.fxCity.citySpace.selectionManager.selected
        val circleSelected = fxCircleTab!!.fxCircle.circleSpace.selectionManager.selected

        if (selectedTabTitle == TabEnum.CIRCLE.value && circleSelected == null ||
            selectedTabTitle == TabEnum.CITY.value && citySelected == null) {
            val notification: Notification =
                notificationGroup.createNotification("VCS Analysis Toolkit - $selectedTabTitle", "Please select object", NotificationType.WARNING)
            Notifications.Bus.notify(notification, null)
        } else if (selectedTabTitle == TabEnum.CIRCLE.value && gitTab?.controller?.models?.isEmpty() == true ||
            selectedTabTitle == TabEnum.CITY.value && gitTab?.controller?.models?.isEmpty() == true) {
            val notification: Notification =
                notificationGroup.createNotification("VCS Analysis Toolkit - $selectedTabTitle", "Please analyze repo", NotificationType.WARNING)
            Notifications.Bus.notify(notification, null)
        } else if (selectedTabTitle == TabEnum.CITY.value && fxCityTab.modelComboBox.selectedIndex == -1) {
            val notification: Notification =
                notificationGroup.createNotification("VCS Analysis Toolkit - $selectedTabTitle", "Please select model", NotificationType.WARNING)
            Notifications.Bus.notify(notification, null)
        }
        else {

            event.project?.let {
                val infoTabPanel = InfoTabPage(tabManager)
                val editor =
                    JComponentEditorProviderUtils.openEditor(it, selectedTabTitle + "Info", infoTabPanel)
                thread { infoTabPanel.start() }
                actionManager.openedFxTabs.set(
                    selectedTabTitle + "Info",
                    FullScreenTabInfo(actionManager.jtp.selectedIndex, JFXPanel(), editor[0].file)
                )
                actionManager.setToolBarWithCollapse()
            }
        }
    }
}