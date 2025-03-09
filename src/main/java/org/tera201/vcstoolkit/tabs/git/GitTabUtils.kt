package org.tera201.vcstoolkit.tabs.git

import com.intellij.notification.Notification
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import java.io.File

object GitTabUtils {

    fun createDirectoryIfNotExists(path: String) {
        val directory = File(path)
        if (!directory.exists()) {
            directory.mkdirs()
        }
    }

    fun createExceptionNotification(e:Exception) {
        val content = "${e.message}"
        System.err.println("message: ${e.message}\nstackTrace: ${e.stackTrace.joinToString("\n")}")
        createNotification(e.javaClass.simpleName, content, NotificationType.ERROR)
    }

    fun createNotification(title:String, message:String, notificationType: NotificationType) {
        val notificationGroup = NotificationGroupManager.getInstance().getNotificationGroup("VCSToolkitNotify")
        val notification: Notification =
            notificationGroup.createNotification("VCS Analysis Toolkit - $title", message, notificationType)
        Notifications.Bus.notify(notification, null)
    }
}