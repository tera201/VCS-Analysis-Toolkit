package org.tera201.vcstoolkit.action

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.impl.JComponentEditorProvider
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.ui.components.JBTabbedPane
import com.intellij.util.containers.stream
import javafx.embed.swing.JFXPanel
import org.tera201.vcstoolkit.helpers.FullScreenTabInfo
import java.util.function.Supplier
import javax.swing.JComponent

class FullScreenAction(private val jtp: JBTabbedPane) : DumbAwareAction(Supplier {"Plugin Settings"}, AllIcons.General.ExpandComponentHover) {

    val openedFxTabs = HashMap<String, FullScreenTabInfo>()
    override fun actionPerformed(event: AnActionEvent) {
        val selectedIndex = jtp.selectedIndex
        val selectedTabTitle = jtp.getTitleAt(selectedIndex)
        val content = jtp.selectedComponent

        event.project?.let {
            val jfxPanel = (content as JComponent).components.stream().filter { it is JFXPanel }.findAny().get() as JFXPanel
            openedFxTabs.set(selectedTabTitle, FullScreenTabInfo(selectedIndex, jfxPanel))
            JComponentEditorProvider.openEditor(
                it, selectedTabTitle,
                jfxPanel as JComponent
            )
        }
    }
}