package org.tera201.vcstoolkit.services.settings

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.extensions.PluginId
import com.intellij.util.messages.Topic
import com.intellij.util.xmlb.XmlSerializerUtil


@State(name = "VCSToolkitSettings", storages = [Storage("vcs_toolkit_settings.xml")])
class VCSToolkitSettings : PersistentStateComponent<VCSToolkitSettings> {

    var repoPath:String =  "${PluginManagerCore.getPlugin(PluginId.getId("org.tera201.VCSToolkit"))?.pluginPath}/VCSToolkitCache"

    var modelPath:String = "${PluginManagerCore.getPlugin(PluginId.getId("org.tera201.VCSToolkit"))?.pluginPath}/VCSToolkitCache/Models"
    var username: String = ""
    var password: String = ""
    var showGitLogs:Boolean = false
    var externalProjectMode:Int = 0
    var circleScrollSpeed:Int = 5
    var circleDynamicScrollSpeed:Boolean = false;
    var cityScrollSpeed:Int = 5
    var cityDynamicScrollSpeed:Boolean = false;


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