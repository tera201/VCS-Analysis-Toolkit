package com.example.umldrawer.services.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.messages.Topic
import com.intellij.util.xmlb.XmlSerializerUtil


@State(name = "UMLToolkitSettingsNew", storages = [Storage("umltoolkit.xml")])
class UMLToolkitSettings : PersistentStateComponent<UMLToolkitSettings> {

    var repoPath:String =  "${System.getProperty("user.dir")}/UmlToolkitCache"

    var modelPath:String = "${System.getProperty("user.dir")}/UmlToolkitCache/Models"
    var showGitLogs:Boolean = true;


    companion object {
        fun getInstance(): UMLToolkitSettings {
            return ApplicationManager.getApplication().getService(UMLToolkitSettings::class.java)
        }
    }

    override fun getState(): UMLToolkitSettings {
        return this
    }

    override fun loadState(state: UMLToolkitSettings) {
        XmlSerializerUtil.copyBean(state, this)
        ApplicationManager.getApplication().messageBus.syncPublisher(SettingsChangedListener.TOPIC)
            .onSettingsChange(this)
    }

    interface SettingsChangedListener {

        companion object {
            val TOPIC = Topic.create(
                "UMLToolkitApplicationSettingsChanged",
                SettingsChangedListener::class.java
            )
        }

        fun onSettingsChange(settings: UMLToolkitSettings)
    }
}