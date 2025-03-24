package org.tera201.vcstoolkit.action

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.DumbAwareAction
import org.tera201.vcstoolkit.services.settings.VCSToolkitSettingsPageUI
import java.util.function.Supplier

class ShowSettingsAction : DumbAwareAction(Supplier { "Plugin Settings" }, AllIcons.General.Settings) {
    override fun actionPerformed(event: AnActionEvent) {
        ShowSettingsUtil.getInstance().editConfigurable(
            event.project, "VCSToolkitSettings",
            VCSToolkitSettingsPageUI()
        )
    }
}