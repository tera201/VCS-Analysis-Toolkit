package org.tera201.vcstoolkit.tabs.git

import com.intellij.notification.Notification
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import java.io.File
import javax.swing.JComponent
import javax.swing.JPanel
import net.miginfocom.swing.MigLayout
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import java.awt.Dimension
import com.formdev.flatlaf.FlatClientProperties

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
    
    fun showCloneDialog(): String? {
        val dialog = CloneRepositoryDialog()
        return if (dialog.showAndGet()) {
            dialog.getRepositoryUrl()
        } else {
            null
        }
    }
    
    private class CloneRepositoryDialog : DialogWrapper(true) {
        private val urlField = JBTextField().apply {
            preferredSize = Dimension(400, 30)
            putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "https://github.com/username/repository.git")
        }
        
        init {
            title = "Clone Repository"
            init()
        }
        
        override fun createCenterPanel(): JComponent {
            return JPanel(MigLayout("insets 10", "[right]10[grow,fill]", "[]10[]")).apply {
                border = JBUI.Borders.empty(10)
                
                add(JBLabel("Repository URL:"))
                add(urlField, "wrap")
                
                add(JBLabel("<html><i>Example: https://github.com/username/repo.git</i></html>").apply {
                    foreground = JBUI.CurrentTheme.ContextHelp.FOREGROUND
                }, "span 2")
            }
        }
        
        override fun getPreferredFocusedComponent(): JComponent = urlField
        
        fun getRepositoryUrl(): String = urlField.text.trim()
        
        override fun doOKAction() {
            if (urlField.text.trim().isEmpty()) {
                return
            }
            super.doOKAction()
        }
    }
}