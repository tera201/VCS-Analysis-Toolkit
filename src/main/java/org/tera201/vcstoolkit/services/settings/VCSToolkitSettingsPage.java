package org.tera201.vcstoolkit.services.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tera201.code2uml.Language;
import org.tera201.code2uml.util.messages.DataBaseUtil;
import org.tera201.vcstoolkit.services.colors.ColorScheme;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.util.Hashtable;

public class VCSToolkitSettingsPage implements Configurable {

    private JPanel mainWindow;
    private JLabel gitLable;
    private JTextField repoPathTextField;
    private JLabel repoPathLable;
    private JLabel modelPathLable;
    private JTextField modelPathTextField;
    private JButton repoBrowseButton;
    private JButton modelBrowseButton;
    private JCheckBox showLogsCheckBox;
    private JSlider slider1;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel usernameLabel;
    private JLabel passworsLabel;
    private JSlider circleScrollSpeedSlider;
    private JLabel circlePaneLable;
    private JLabel scrollSpeedLable;
    private JSlider cityScrollSpeedSlider;
    private JCheckBox circleDynamicSpeedCheckBox;
    private JCheckBox cityDynamicSpeedCheckBox;
    private JButton createButton;
    private JButton removeButton;
    private JSpinner circleMethodFactor;
    private JSpinner cityMethodFactor;
    private JComboBox circleColorScheme;
    private JComboBox cityColorScheme;
    private JSpinner circlePackageFactor;
    private JSpinner circleHeightFactor;
    private JSpinner circleGapFactor;
    private JComboBox languageComboBox;

    public VCSToolkitSettingsPage() {

        repoBrowseButton.addActionListener(e -> {
            browseFolder(repoPathTextField);
        });

        modelBrowseButton.addActionListener(e -> {
            browseFolder(modelPathTextField);
        });

        Hashtable<Integer, JLabel> labels = new Hashtable<>();
        labels.put(0, new JLabel("Unsafe"));
        labels.put(1, new JLabel("Safe"));
        labels.put(2, new JLabel("Create commit"));
        slider1.setLabelTable(labels);

        Hashtable<Integer, JLabel> scrollSpeedSliderLabels = new Hashtable<>();
        for (int i = circleScrollSpeedSlider.getMinimum(); i <= circleScrollSpeedSlider.getMaximum(); i += 5) {
            scrollSpeedSliderLabels.put(i, new JLabel(String.valueOf(i)));
        }
        circleScrollSpeedSlider.setLabelTable(scrollSpeedSliderLabels);
        circleDynamicSpeedCheckBox.addItemListener(e -> circleScrollSpeedSlider.setEnabled(e.getStateChange() != ItemEvent.SELECTED));
        circleScrollSpeedSlider.setEnabled(!circleDynamicSpeedCheckBox.isSelected());

        cityScrollSpeedSlider.setLabelTable(scrollSpeedSliderLabels);
        cityDynamicSpeedCheckBox.addItemListener(e -> cityScrollSpeedSlider.setEnabled(e.getStateChange() != ItemEvent.SELECTED));
        cityScrollSpeedSlider.setEnabled(!cityDynamicSpeedCheckBox.isSelected());
        removeButton.addActionListener(e -> {
            DataBaseUtil dataBaseUtil = DataBaseUtil.Companion.getInstance(modelPathTextField.getText() + "/model.db");
            dataBaseUtil.clearTables();
        });
        createButton.addActionListener(e -> {
            DataBaseUtil dataBaseUtil = DataBaseUtil.Companion.getInstance(modelPathTextField.getText() + "/model.db");
            dataBaseUtil.recreateTables();
        });

        SpinnerNumberModel circleSpinnerModel = new SpinnerNumberModel(100, 1, 1000, 10);
        circleMethodFactor.setModel(circleSpinnerModel);

        SpinnerNumberModel circlePackageModel = new SpinnerNumberModel(5, 1, 500, 1);
        circlePackageFactor.setModel(circlePackageModel);

        SpinnerNumberModel circleHeightModel = new SpinnerNumberModel(1, 1, 100, 1);
        circleHeightFactor.setModel(circleHeightModel);

        SpinnerNumberModel circleGapModel = new SpinnerNumberModel(8, 1, 200, 1);
        circleGapFactor.setModel(circleGapModel);

        SpinnerNumberModel citySpinnerModel = new SpinnerNumberModel(10, 1, 400, 5);
        cityMethodFactor.setModel(citySpinnerModel);

        circleColorScheme.setModel(new DefaultComboBoxModel<>(ColorScheme.values()));
        cityColorScheme.setModel(new DefaultComboBoxModel<>(ColorScheme.values()));

        languageComboBox.setModel(new DefaultComboBoxModel(Language.values()));
    }

    private void browseFolder(@NotNull final JTextField target) {
        final FileChooserDescriptor descriptor = new FileChooserDescriptor(false, true,
                false, false, false, false);

        descriptor.setTitle("Select Path");
        String text = target.getText();
        final VirtualFile toSelect = text == null || text.isEmpty() ? null
                : LocalFileSystem.getInstance().findFileByPath(text);

        VirtualFile virtualFile = FileChooser.chooseFile(descriptor, null, toSelect);
        if (virtualFile != null) {
            target.setText(virtualFile.getPath());
        }
    }

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "VCS Analysis Toolkit";
    }

    @Override
    public @Nullable JComponent createComponent() {
        return mainWindow;
    }

    @Override
    public boolean isModified() {
        VCSToolkitSettings settings = VCSToolkitSettings.Companion.getInstance();
        return isModified(settings);
    }

    private boolean isModified(VCSToolkitSettings settings) {
        return !repoPathTextField.getText().equals(settings.getRepoPath()) ||
                !modelPathTextField.getText().equals(settings.getModelPath()) ||
                showLogsCheckBox.isSelected() != settings.getShowGitLogs() ||
                slider1.getValue() != settings.getExternalProjectMode() ||
                !usernameField.getText().equals(settings.getUsername()) ||
                !String.valueOf(passwordField.getPassword()).equals(settings.getPassword()) ||

                circleScrollSpeedSlider.getValue() != settings.getCircleScrollSpeed() ||
                circleDynamicSpeedCheckBox.isSelected() != settings.getCircleDynamicScrollSpeed() ||
                ((int) circleMethodFactor.getValue()) != settings.getCircleMethodFactor() ||
                circleColorScheme.getSelectedItem() != settings.getCircleColorScheme() ||
                ((int) circlePackageFactor.getValue()) != settings.getCirclePackageFactor() ||
                ((int) circleHeightFactor.getValue()) != settings.getCircleHeightFactor() ||
                ((int) circleGapFactor.getValue()) != settings.getCircleGapFactor() ||


                cityScrollSpeedSlider.getValue() != settings.getCityScrollSpeed() ||
                cityDynamicSpeedCheckBox.isSelected() != settings.getCityDynamicScrollSpeed() ||
                ((int) cityMethodFactor.getValue()) != settings.getCityMethodFactor() ||
                cityColorScheme.getSelectedItem() != settings.getCityColorScheme() ||

                languageComboBox.getSelectedItem() != settings.getLanguage();
    }

    @Override
    public void apply() throws ConfigurationException {
        VCSToolkitSettings settings = VCSToolkitSettings.Companion.getInstance();
        applySetting(settings);
        ApplicationManager.getApplication().getMessageBus()
                .syncPublisher(VCSToolkitSettings.SettingsChangedListener.Companion.getTOPIC())
                .onSettingsChange(settings);
    }

    @Override
    public void reset() {
        VCSToolkitSettings settings = VCSToolkitSettings.Companion.getInstance();
        getSettings(settings);
    }

    private void applySetting(VCSToolkitSettings settings) {
        settings.setRepoPath(repoPathTextField.getText());
        settings.setModelPath(modelPathTextField.getText());
        settings.setShowGitLogs(showLogsCheckBox.isSelected());
        settings.setExternalProjectMode(slider1.getValue());
        settings.setUsername(usernameField.getText());
        settings.setPassword(String.valueOf(passwordField.getPassword()));

        settings.setCircleScrollSpeed(circleScrollSpeedSlider.getValue());
        settings.setCircleDynamicScrollSpeed(circleDynamicSpeedCheckBox.isSelected());
        settings.setCircleMethodFactor((int) circleMethodFactor.getValue());
        if (circleColorScheme.getSelectedItem() != null)
            settings.setCircleColorScheme((ColorScheme) circleColorScheme.getSelectedItem());
        settings.setCirclePackageFactor((int) circlePackageFactor.getValue());
        settings.setCircleHeightFactor((int) circleHeightFactor.getValue());
        settings.setCircleGapFactor((int) circleGapFactor.getValue());

        settings.setCityScrollSpeed(cityScrollSpeedSlider.getValue());
        settings.setCityDynamicScrollSpeed(cityDynamicSpeedCheckBox.isSelected());
        settings.setCityMethodFactor((int) cityMethodFactor.getValue());
        if (cityColorScheme.getSelectedItem() != null)
            settings.setCityColorScheme((ColorScheme) cityColorScheme.getSelectedItem());


        if (languageComboBox.getSelectedItem() != null)
            settings.setLanguage((Language) languageComboBox.getSelectedItem());
    }

    private void getSettings(VCSToolkitSettings settings) {
        repoPathTextField.setText(settings.getRepoPath());
        modelPathTextField.setText(settings.getModelPath());
        showLogsCheckBox.setSelected(settings.getShowGitLogs());
        slider1.setValue(settings.getExternalProjectMode());
        usernameField.setText(settings.getUsername());
        passwordField.setText(settings.getPassword());

        circleScrollSpeedSlider.setValue(settings.getCircleScrollSpeed());
        circleDynamicSpeedCheckBox.setSelected(settings.getCircleDynamicScrollSpeed());
        circleMethodFactor.setValue(settings.getCircleMethodFactor());
        circleColorScheme.setSelectedIndex(settings.getCircleColorScheme().getIndex());
        circlePackageFactor.setValue(settings.getCirclePackageFactor());
        circleHeightFactor.setValue(settings.getCircleHeightFactor());
        circleGapFactor.setValue(settings.getCircleGapFactor());

        cityScrollSpeedSlider.setValue(settings.getCityScrollSpeed());
        cityDynamicSpeedCheckBox.setSelected(settings.getCityDynamicScrollSpeed());
        cityMethodFactor.setValue(settings.getCityMethodFactor());
        cityColorScheme.setSelectedIndex(settings.getCityColorScheme().getIndex());

        languageComboBox.setSelectedIndex(settings.getLanguage().getIndex());
    }

}
