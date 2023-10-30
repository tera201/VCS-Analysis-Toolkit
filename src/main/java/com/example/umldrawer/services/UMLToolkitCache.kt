package com.example.umldrawer.services

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.messages.Topic
import com.intellij.util.xmlb.XmlSerializerUtil


@State(name = "UMLToolkitCachesNew", storages = [Storage("umltoolkit_cache.xml")])
class UMLToolkitCache : PersistentStateComponent<UMLToolkitCache> {
    var lastProject: String = ""
    var projectPathMap:MutableMap<String, String> = hashMapOf()


    companion object {
        fun getInstance(): UMLToolkitCache {
            return ApplicationManager.getApplication().getService(UMLToolkitCache::class.java)
        }
    }

    override fun getState(): UMLToolkitCache {
        return this
    }

    override fun loadState(state: UMLToolkitCache) {
        XmlSerializerUtil.copyBean(state, this)
        ApplicationManager.getApplication().messageBus.syncPublisher(CacheChangedListener.TOPIC)
            .onCacheChange(this)
    }

    interface CacheChangedListener {

        companion object {
            val TOPIC = Topic.create(
                "UMLToolkitApplicationCacheChanged",
                CacheChangedListener::class.java
            )
        }

        fun onCacheChange(cache: UMLToolkitCache)
    }

}