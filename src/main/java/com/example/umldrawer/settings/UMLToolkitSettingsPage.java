package com.example.umldrawer.settings;

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

import javax.swing.*;

public class UMLToolkitSettingsPage implements Configurable {

    private JPanel mainWindow;
    private JLabel gitLable;
    private JTextField repoPathTextField;
    private JLabel repoPathLable;
    private JLabel modelPathLable;
    private JTextField modelPathTextField;
    private JButton repoBrowseButton;
    private JButton modelBrowseButton;
    private JCheckBox showLogsCheckBox;

    public UMLToolkitSettingsPage() {

        repoBrowseButton.addActionListener(e -> {
            browseFolder(repoPathTextField);
        });

        modelBrowseButton.addActionListener(e -> {
            browseFolder(modelPathTextField);
        });
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
        return "UML Toolkit";
    }

    @Override
    public @Nullable JComponent createComponent() {
        return mainWindow;
    }

    @Override
    public boolean isModified() {
        UMLToolkitSettings settings = UMLToolkitSettings.Companion.getInstance();
        return isModified(settings);
    }

    private  boolean isModified(UMLToolkitSettings settings) {
        return !repoPathTextField.getText().equals(settings.getRepoPath()) ||
                !modelPathTextField.getText().equals(settings.getModelPath()) ||
                showLogsCheckBox.isSelected() != settings.getShowGitLogs();
    }

    @Override
    public void apply() throws ConfigurationException {
        UMLToolkitSettings settings = UMLToolkitSettings.Companion.getInstance();
        applySetting(settings);
        ApplicationManager.getApplication().getMessageBus()
                .syncPublisher(UMLToolkitSettings.SettingsChangedListener.Companion.getTOPIC())
                .onSettingsChange(settings);
    }

    @Override
    public void reset() {
        UMLToolkitSettings settings = UMLToolkitSettings.Companion.getInstance();
        getSettings(settings);
    }

    private void applySetting(UMLToolkitSettings settings) {
        settings.setRepoPath(repoPathTextField.getText());
        settings.setModelPath(modelPathTextField.getText());
        settings.setShowGitLogs(showLogsCheckBox.isSelected());
    }

    private void getSettings(UMLToolkitSettings settings) {
        repoPathTextField.setText(settings.getRepoPath());
        modelPathTextField.setText(settings.getModelPath());
        showLogsCheckBox.setSelected(settings.getShowGitLogs());
    }

}
