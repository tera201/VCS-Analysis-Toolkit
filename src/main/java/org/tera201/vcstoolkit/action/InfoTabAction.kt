package org.tera201.vcstoolkit.action

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.impl.JComponentEditorProvider
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.util.containers.stream
import javafx.embed.swing.JFXPanel
import org.tera201.vcstoolkit.helpers.FullScreenTabInfo
import org.tera201.vcstoolkit.info.InfoPage
import java.util.function.Supplier
import javax.swing.JComponent
import javax.swing.JPanel

class InfoTabAction(private val actionManager: ActionManager) : DumbAwareAction(Supplier {"Open stat"}, AllIcons.General.Information) {
    override fun actionPerformed(event: AnActionEvent) {
        val selectedIndex = actionManager.jtp.selectedIndex
        val selectedTabTitle = actionManager.jtp.getTitleAt(selectedIndex)
        val content = actionManager.jtp.selectedComponent

        event.project?.let {
            val jfxPanel = (content as JComponent).components.stream().filter { it is JFXPanel }.findAny().get() as JFXPanel
            val infoPanel = InfoPage()
            val editor = JComponentEditorProvider.openEditor(
                it, selectedTabTitle+"Info",
                infoPanel.component
            )
            actionManager.openedFxTabs.set(selectedTabTitle+"Info", FullScreenTabInfo(selectedIndex, JFXPanel(), editor[0].file))
            actionManager.setToolBarWithCollapse()
        }
    }
}