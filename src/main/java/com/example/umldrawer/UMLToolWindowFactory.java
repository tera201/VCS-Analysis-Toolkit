package com.example.umldrawer;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
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
//        final JFXPanel fxPanel = new JFXPanel();
//        JComponent component = toolWindow.getComponent();
//
//        Platform.setImplicitExit(false);
//        Platform.runLater(() -> {
//            WebView webView = new WebView();
//            WebEngine webEngine = webView.getEngine();
//            webEngine.load("file:///C:\\Users\\rnary\\IdeaProjects\\cpppainter\\generated-html5\\CppSampleModel\\index.html");
//            webEngine.load("file:///C:\\Users\\rnary\\IdeaProjects\\UMLDrawer\\src\\main\\java\\com\\example\\generated-html5\\CppSampleModel\\index.html");
//            Group root  =  new Group();
//            Scene scene  =  new  Scene(root, 40, 100);
//            root.getChildren().add(webView);
//
//            fxPanel.setScene(scene);
//        });

//        component.getParent().add(fxPanel);
        JRootPane jRootPane = new JRootPane();

        JPanel myToolWindowContent = new JPanel();
        myToolWindowContent.setAutoscrolls(true);

        JFrame frame = new JFrame();
//        final JFXPanel fxPanel = new JFXPanel();
        JComponent component = toolWindow.getComponent();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JTabbedPane jtp = new JTabbedPane();
        jtp.add("One", createTab("http://www.example.com"));
        jtp.add("Two", createTab("http://www.example.net"));
        jtp.add("Three", createTab("http://www.example.org"));
        frame.add(jtp, BorderLayout.CENTER);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        myToolWindowContent.add(createTab("file:///C:\\Users\\rnary\\IdeaProjects\\cpppainter\\generated-html5\\CppSampleModel\\index.html"));
        myToolWindowContent.add(createTab("http://www.example.com"));
        myToolWindowContent.add(jtp, BorderLayout.CENTER);
        component.getParent().add(myToolWindowContent);

//        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
//        Content content = contentFactory.createContent(myToolWindowContent, "", true);
//        toolWindow.getContentManager().addContent(content);
    }

    private JFXPanel createTab(String s) {
        final JFXPanel fxPanel = new JFXPanel() {

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(640, 480);
            }
        };
        Platform.runLater(() -> {
            initFX(fxPanel, s);
        });
        return fxPanel;
    }

    private void initFX(JFXPanel fxPanel, String s) {
        // This method is invoked on the JavaFX thread
        StackPane root = new StackPane();
        Scene scene = new Scene(root);
        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();
        webEngine.load(s);
        root.getChildren().add(webView);
        fxPanel.setScene(scene);
    }
}
