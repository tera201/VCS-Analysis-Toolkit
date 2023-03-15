package com.example.umldrawer;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class UMLToolWindowFactory implements ToolWindowFactory {

    /**
     * Create the tool window content.
     *
     * @param project    current project
     * @param toolWindow current tool windowA
     */
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        final JFXPanel fxPanel = new JFXPanel();
        JComponent component = toolWindow.getComponent();

        Platform.setImplicitExit(false);
        Platform.runLater(() -> {
            WebView webView = new WebView();
            WebEngine webEngine = webView.getEngine();
            webEngine.load("file:///C:\\Users\\rnary\\IdeaProjects\\cpppainter\\generated-html5\\CppSampleModel\\index.html");
            webEngine.load("file:///C:\\Users\\rnary\\IdeaProjects\\UMLDrawer\\src\\main\\java\\com\\example\\generated-html5\\CppSampleModel\\index.html");
            Group root  =  new Group();
            Scene scene  =  new  Scene(root, 40, 100);
            root.getChildren().add(webView);

            fxPanel.setScene(scene);
        });

        component.getParent().add(fxPanel);
    }
}
