package org.tera201.vcstoolkit.services.settings

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.extensions.PluginId
import com.intellij.util.messages.Topic
import com.intellij.util.xmlb.XmlSerializerUtil
import org.tera201.vcstoolkit.services.colors.ColorScheme


@State(name = "VCSToolkitSettings", storages = [Storage("vcs_toolkit_settings.xml")])
class VCSToolkitSettings : PersistentStateComponent<VCSToolkitSettings> {

    var repoPath: String =
        "${PluginManagerCore.getPlugin(PluginId.getId("org.tera201.vcs-analysis-toolkit"))?.pluginPath}/VCSToolkitCache"

    var modelPath: String =
        "${PluginManagerCore.getPlugin(PluginId.getId("org.tera201.vcs-analysis-toolkit"))?.pluginPath}/VCSToolkitCache/Models"
    var username: String = ""
    var password: String = ""
    var showGitLogs: Boolean = false
    var externalProjectMode: Int = 0

    var circleScrollSpeed: Int = 5
    var circleDynamicScrollSpeed: Boolean = false
    var circleMethodFactor: Int = 100
    var circleColorScheme: ColorScheme = ColorScheme.DEFAULT
    var circlePackageFactor: Int = 5
    var circleHeightFactor: Int = 1
    var circleGapFactor: Int = 16

    var cityScrollSpeed: Int = 5
    var cityDynamicScrollSpeed: Boolean = false
    var cityMethodFactor: Int = 10
    var cityColorScheme: ColorScheme = ColorScheme.DEFAULT


    companion object {
        fun getInstance(): VCSToolkitSettings {
            return ApplicationManager.getApplication().getService(VCSToolkitSettings::class.java)
        }
    }

    override fun getState(): VCSToolkitSettings {
        return this
    }

    override fun loadState(state: VCSToolkitSettings) {
        XmlSerializerUtil.copyBean(state, this)
        ApplicationManager.getApplication().messageBus.syncPublisher(SettingsChangedListener.TOPIC)
            .onSettingsChange(this)
    }

    interface SettingsChangedListener {

        companion object {
            val TOPIC = Topic.create(
                "VCSToolkitApplicationSettingsChanged",
                SettingsChangedListener::class.java
            )
        }

        fun onSettingsChange(settings: VCSToolkitSettings)
    }
}