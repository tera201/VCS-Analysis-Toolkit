package com.example.umldrawer;

import com.intellij.openapi.wm.ToolWindow;

import javax.swing.*;
import console.ReverserStart;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;

public class UMLToolWindow {
    private JPanel umlToolWindowContent;
    private JEditorPane jep;
    private JScrollPane scrollPane;

    public UMLToolWindow(ToolWindow toolWindow) {
        String rootPath = Paths.get("").toAbsolutePath().toString().replace("\\", "/") + "/%s";
        System.out.println(rootPath);
//        ReverserStart.buildUMLModel("C:\\Users\\rnary\\IdeaProjects\\UMLDrawer\\src\\main\\java\\com\\example\\models\\CppSampleModel.uml", "C:\\Users\\rnary\\IdeaProjects\\UMLDrawer\\src\\main\\java\\com\\example\\generated-html5");

//        JEditorPane jep = new JEditorPane();
        jep.setEditable(false);
        try {
            URL url= new File("C:\\Users\\rnary\\IdeaProjects\\UMLDrawer\\src\\main\\java\\com\\example\\generated-html5\\CppSampleModel\\html\\_all-classifiers.html").toURI().toURL();
            jep.setPage(url);
        }
        catch (IOException e) {
            jep.setContentType("text/html");
            jep.setText("<html>Could not load webpage\n" + e.toString() + "</html>");
        }

//        JScrollPane scrollPane = new JScrollPane(jep);
//        JFrame f = new JFrame("C:\\Users\\rnary\\IdeaProjects\\UMLDrawer\\src\\main\\java\\com\\example\\generated-html5\\index.html");
//        f.getContentPane().add(scrollPane);
//        f.setSize(512, 342);
//        f.show();

//        hideToolWindowButton.addActionListener(e -> toolWindow.hide(null));
//        refreshToolWindowButton.addActionListener(e -> currentDateTime());
//
//        this.currentDateTime();
    }

    public JPanel getContent() {
        return umlToolWindowContent;
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
