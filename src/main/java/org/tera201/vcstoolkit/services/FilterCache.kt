package org.tera201.vcstoolkit.services

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.util.messages.Topic
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.Tag
import org.tera201.vcstoolkit.info.CommitFilterConfig
import org.tera201.vcstoolkit.info.GroupData

@Tag("FilterCacheState")
data class FilterCacheState(
    var commitFilterCache: MutableMap<String, MutableList<CommitFilterConfig>> = mutableMapOf(),
    var groupDataCache: MutableMap<String, MutableList<GroupData>> = mutableMapOf(),
    var checkedCommits: MutableMap<String, MutableSet<String>> = mutableMapOf(),
)

@Service(Service.Level.PROJECT)
@State(name = "VCSToolkitFilterCache", storages = [Storage("vcs_toolkit_filter_cache.xml")])
class FilterCache: PersistentStateComponent<FilterCacheState> {
    private var state = FilterCacheState()

    companion object {
        fun getInstance(project: Project): FilterCache {
            return project.getService(FilterCache::class.java)
        }
    }

    override fun getState(): FilterCacheState{
        return state
    }

    override fun loadState(state: FilterCacheState) {
        XmlSerializerUtil.copyBean(state, this.state)
        this.state = state
        ApplicationManager.getApplication().messageBus.syncPublisher(FilterCacheChangedListener.TOPIC)
            .onFilterCacheChange(state)
    }


    val commitFilterCache get() = state.commitFilterCache
    val groupDataCache get() = state.groupDataCache
    val checkedCommits get() = state.checkedCommits

    interface FilterCacheChangedListener {

        companion object {
            val TOPIC = Topic.create(
                "VCSToolkitApplicationFilterCacheChanged",
                FilterCacheChangedListener::class.java
            )
        }

        fun onFilterCacheChange(cache: FilterCacheState)
    }
}