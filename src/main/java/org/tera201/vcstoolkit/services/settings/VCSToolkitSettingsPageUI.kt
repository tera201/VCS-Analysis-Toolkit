package org.tera201.vcstoolkit.services.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.TextBrowseFolderListener
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.*
import com.intellij.uiDesigner.core.GridLayoutManager
import com.intellij.util.ui.FormBuilder
import org.tera201.code2uml.Language
import org.tera201.code2uml.util.messages.DataBaseUtil.Companion.getInstance
import org.tera201.vcstoolkit.helpers.addCenteredLabel
import org.tera201.vcstoolkit.helpers.addComponentToLeft
import org.tera201.vcstoolkit.helpers.addNComponentsRow
import org.tera201.vcstoolkit.services.colors.ColorScheme
import org.tera201.vcstoolkit.services.settings.VCSToolkitSettings.Companion.getInstance
import org.tera201.vcstoolkit.services.settings.VCSToolkitSettings.SettingsChangedListener.Companion.TOPIC
import java.awt.event.ItemEvent
import java.util.*
import javax.swing.*

class VCSToolkitSettingsPageUI : Configurable {
    val panel: JPanel
    private val repoPathFieldWithBrowseButton = TextFieldWithBrowseButton()
    private val modelPathFieldWithBrowseButton = TextFieldWithBrowseButton()
    private val showLogsPanelCheckBox = JBCheckBox("Show logs panel")
    private val externalProjectSlider = JBSlider(0, 2, 0).apply {
        paintLabels = true
        paintTicks = true
        paintTrack = true
        majorTickSpacing = 1
        minorTickSpacing = 1
        labelTable = Hashtable<Int, JLabel>().apply {
            put(0, JLabel("Unsafe"))
            put(1, JLabel("Safe"))
            put(2, JLabel("Create commit"))
        }
    }
    private val usernameField = JBTextField()
    private val passwordField = JBPasswordField()
    private val circleScrollSpeedSlider = JBSlider(0, 20, 5).apply {
        paintLabels = true
        paintTicks = true
        paintTrack = true
        majorTickSpacing = 5
        minorTickSpacing = 1
    }
    private val circleDynamicSpeedCheckBox = JBCheckBox("Dynamic speed")
    private val circleScrollSpeedPanel = JBPanel<JBPanel<*>>(GridLayoutManager(1, 3)).apply {
        addNComponentsRow(
            0,
            JBLabel("Scroll speed") to false,
            circleScrollSpeedSlider to true,
            circleDynamicSpeedCheckBox to false
        )
    }
    private val circleMethodFactor = JSpinner(SpinnerNumberModel(100, 1, 1000, 10))
    private val circlePackageFactor = JSpinner(SpinnerNumberModel(5, 1, 500, 1))
    private val circleHeightFactor = JSpinner(SpinnerNumberModel(1, 1, 100, 1))
    private val circleGapFactor = JSpinner(SpinnerNumberModel(8, 1, 200, 1))
    private val circleColorScheme = JComboBox(DefaultComboBoxModel(ColorScheme.values()))
    private val circleFactors: JPanel = FormBuilder.createFormBuilder()
        .addLabeledComponent(JBLabel("Method factor"), circleMethodFactor)
        .addLabeledComponent(JBLabel("Package factor"), circlePackageFactor)
        .addLabeledComponent(JBLabel("Height factor"), circleHeightFactor)
        .addLabeledComponent(JBLabel("Gap factor"), circleGapFactor)
        .panel

    private val circleColorSchemePanel = JBPanel<JBPanel<*>>(GridLayoutManager(1, 2)).apply {
        addNComponentsRow(0, JBLabel("Color scheme") to false, circleColorScheme to false)
    }
    private val circlePanel = JBPanel<JBPanel<*>>(GridLayoutManager(1, 2)).apply {
        addNComponentsRow(0, circleFactors to false, circleColorSchemePanel to false)
    }
    private val cityScrollSpeedSlider = JBSlider(0, 20, 5).apply {
        paintLabels = true
        paintTicks = true
        paintTrack = true
        majorTickSpacing = 5
        minorTickSpacing = 1
    }
    private val cityDynamicSpeedCheckBox = JBCheckBox("Dynamic speed")
    private val cityScrollSpeedPanel = JBPanel<JBPanel<*>>(GridLayoutManager(1, 3)).apply {
        addNComponentsRow(
            0,
            JBLabel("Scroll speed") to false,
            cityScrollSpeedSlider to true,
            cityDynamicSpeedCheckBox to false
        )
    }
    private val cityMethodFactor = JSpinner(SpinnerNumberModel(10, 1, 400, 5))
    private val cityColorScheme = JComboBox(DefaultComboBoxModel(ColorScheme.values()))
    private val cityFactors = JBPanel<JBPanel<*>>(GridLayoutManager(1, 2)).apply {
        addNComponentsRow(0, JBLabel("Method factor") to false, cityMethodFactor to false)
    }
    private val cityColorSchemePanel = JBPanel<JBPanel<*>>(GridLayoutManager(1, 2)).apply {
        addNComponentsRow(0, JBLabel("Color scheme") to false, cityColorScheme to false)
    }
    private val cityPanel = JBPanel<JBPanel<*>>(GridLayoutManager(1, 2)).apply {
        addNComponentsRow(0, cityFactors to false, cityColorSchemePanel to false)
    }
    private val createButton = JButton("Create")
    private val deleteButton = JButton("Delete")
    private val dbControlPanel = JBPanel<JBPanel<*>>(GridLayoutManager(1, 2)).apply {
        addNComponentsRow(0, createButton to false, deleteButton to false)
    }
    private val languageComboBox = JComboBox(DefaultComboBoxModel(Language.values()))

    init {
        panel = FormBuilder.createFormBuilder()
            .addCenteredLabel("Git properties")
            .addLabeledComponent("Default repo path:", repoPathFieldWithBrowseButton, true)
            .addLabeledComponent("Default model path:", modelPathFieldWithBrowseButton, true)
            .addComponent(showLogsPanelCheckBox)
            .addLabeledComponent("External projects", externalProjectSlider)
            .addSeparator()
            .addCenteredLabel("Git auth")
            .addLabeledComponent("Username", usernameField, true)
            .addLabeledComponent("Password", passwordField, true)
            .addSeparator()
            .addCenteredLabel("Circle properties")
            .addComponent(circleScrollSpeedPanel)
            .addComponent(circlePanel)
            .addSeparator()
            .addCenteredLabel("City properties")
            .addComponent(cityScrollSpeedPanel)
            .addComponent(cityPanel)
            .addSeparator()
            .addComponent(JBLabel("Data Base controls"))
            .addComponentToLeft(dbControlPanel)
            .addSeparator()
            .addCenteredLabel("Analyzer properties")
            .addLabeledComponent("Language", languageComboBox, true)
            .panel


        circleScrollSpeedSlider.isEnabled = !circleDynamicSpeedCheckBox.isSelected
        cityScrollSpeedSlider.isEnabled = !cityDynamicSpeedCheckBox.isSelected
        addListeners()
    }

    private fun addListeners() {
        repoPathFieldWithBrowseButton.addBrowseFolderListener(TextBrowseFolderListener(createFileChooserDescriptor()))
        modelPathFieldWithBrowseButton.addBrowseFolderListener(TextBrowseFolderListener(createFileChooserDescriptor()))

        circleDynamicSpeedCheckBox.addItemListener { e: ItemEvent ->
            circleScrollSpeedSlider.isEnabled =
                e.stateChange != ItemEvent.SELECTED
        }
        cityDynamicSpeedCheckBox.addItemListener { e: ItemEvent ->
            cityScrollSpeedSlider.isEnabled =
                e.stateChange != ItemEvent.SELECTED
        }

        deleteButton.addActionListener({
            val dataBaseUtil = getInstance(modelPathFieldWithBrowseButton.text + "/model.db")
            dataBaseUtil.clearTables()
        })
        createButton.addActionListener {
            val dataBaseUtil =
                getInstance(modelPathFieldWithBrowseButton.text + "/model.db")
            dataBaseUtil.recreateTables()
        }
    }

    private fun createFileChooserDescriptor() = FileChooserDescriptor(
        false, true,
        false, false, false, false
    ).apply { title = "Select Path" }


    override fun createComponent(): JComponent {
        return panel
    }

    override fun isModified(): Boolean {
        val settings = getInstance()
        return isModified(settings)
    }

    private fun isModified(settings: VCSToolkitSettings): Boolean {
        return (repoPathFieldWithBrowseButton.text != settings.repoPath) ||
                (modelPathFieldWithBrowseButton.text != settings.modelPath) ||
                showLogsPanelCheckBox.isSelected() != settings.showGitLogs ||
                externalProjectSlider.getValue() != settings.externalProjectMode ||
                (usernameField.text != settings.username) ||
                (String(passwordField.getPassword()) != settings.password) ||

                circleScrollSpeedSlider.value != settings.circleScrollSpeed ||
                circleDynamicSpeedCheckBox.isSelected != settings.circleDynamicScrollSpeed ||
                (circleMethodFactor.value as Int) != settings.circleMethodFactor ||
                circleColorScheme.selectedItem !== settings.circleColorScheme ||
                (circlePackageFactor.value as Int) != settings.circlePackageFactor ||
                (circleHeightFactor.value as Int) != settings.circleHeightFactor ||
                (circleGapFactor.value as Int) != settings.circleGapFactor ||

                cityScrollSpeedSlider.value != settings.cityScrollSpeed ||
                cityDynamicSpeedCheckBox.isSelected != settings.cityDynamicScrollSpeed ||
                (cityMethodFactor.value as Int) != settings.cityMethodFactor ||
                cityColorScheme.selectedItem !== settings.cityColorScheme ||

                languageComboBox.selectedItem !== settings.language
    }

    override fun apply() {
        val settings = getInstance()
        applySetting(settings)
        ApplicationManager.getApplication().messageBus
            .syncPublisher(TOPIC)
            .onSettingsChange(settings)
    }

    override fun reset() {
        val settings = getInstance()
        getSettings(settings)
    }

    private fun applySetting(settings: VCSToolkitSettings) {
        settings.repoPath = repoPathFieldWithBrowseButton.text
        settings.modelPath = modelPathFieldWithBrowseButton.text
        settings.showGitLogs = showLogsPanelCheckBox.isSelected
        settings.externalProjectMode = externalProjectSlider.value
        settings.username = usernameField.text
        settings.password = String(passwordField.password)

        settings.circleScrollSpeed = circleScrollSpeedSlider.value
        settings.circleDynamicScrollSpeed = circleDynamicSpeedCheckBox.isSelected
        settings.circleMethodFactor = circleMethodFactor.value as Int
        if (circleColorScheme.selectedItem != null) settings.circleColorScheme =
            (circleColorScheme.selectedItem as ColorScheme)
        settings.circlePackageFactor = circlePackageFactor.value as Int
        settings.circleHeightFactor = circleHeightFactor.value as Int
        settings.circleGapFactor = circleGapFactor.value as Int

        settings.cityScrollSpeed = cityScrollSpeedSlider.value
        settings.cityDynamicScrollSpeed = cityDynamicSpeedCheckBox.isSelected
        settings.cityMethodFactor = cityMethodFactor.value as Int
        if (cityColorScheme.selectedItem != null) settings.cityColorScheme =
            (cityColorScheme.selectedItem as ColorScheme)


        if (languageComboBox.selectedItem != null) settings.language = (languageComboBox.selectedItem as Language)
    }

    private fun getSettings(settings: VCSToolkitSettings) {
        repoPathFieldWithBrowseButton.text = settings.repoPath
        modelPathFieldWithBrowseButton.text = settings.modelPath
        showLogsPanelCheckBox.setSelected(settings.showGitLogs)
        externalProjectSlider.setValue(settings.externalProjectMode)
        usernameField.text = settings.username
        passwordField.text = settings.password

        circleScrollSpeedSlider.value = settings.circleScrollSpeed
        circleDynamicSpeedCheckBox.isSelected = settings.circleDynamicScrollSpeed
        circleMethodFactor.value = settings.circleMethodFactor
        circleColorScheme.selectedIndex = settings.circleColorScheme.index
        circlePackageFactor.value = settings.circlePackageFactor
        circleHeightFactor.value = settings.circleHeightFactor
        circleGapFactor.value = settings.circleGapFactor

        cityScrollSpeedSlider.value = settings.cityScrollSpeed
        cityDynamicSpeedCheckBox.isSelected = settings.cityDynamicScrollSpeed
        cityMethodFactor.value = settings.cityMethodFactor
        cityColorScheme.selectedIndex = settings.cityColorScheme.index

        languageComboBox.selectedIndex = settings.language.index
    }

    override fun getDisplayName(): String {
        return "VCS Analysis Toolkit"
    }
}