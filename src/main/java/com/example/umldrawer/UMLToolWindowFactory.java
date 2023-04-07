package com.example.umldrawer;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;
import org.example.MainSubScene;
import org.example.elements.Building;
import org.example.elements.City;
import org.example.elements.Quarter;
import org.jetbrains.annotations.NotNull;
import umlgraph.containers.GraphDemoContainer;
import umlgraph.graph.Digraph;
import umlgraph.graph.DigraphEdgeList;
import umlgraph.graph.Graph;
import umlgraph.graphview.GraphPanel;
import umlgraph.graphview.arrows.ArrowTypes;
import umlgraph.graphview.strategy.CircularSortedPlacementStrategy;
import umlgraph.graphview.strategy.PlacementStrategy;
import umlgraph.graphview.vertices.GraphVertex;
import umlgraph.graphview.vertices.elements.ElementTypes;

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

        JPanel myToolWindowContent = new JPanel();
        myToolWindowContent.setAutoscrolls(true);

        JComponent component = toolWindow.getComponent();
        JTabbedPane jtp = new JTabbedPane();
        jtp.add("FX City", createFXCity());
        jtp.add("FX Graph", createFXGraph());

//        myToolWindowContent.add(createTab(""));
//        myToolWindowContent.add(createTab("http://www.example.com"));
        myToolWindowContent.add(jtp, BorderLayout.CENTER);
        myToolWindowContent.setVisible(true);
        component.getParent().add(myToolWindowContent);
    }

    private JFXPanel createFXCity() {
        final JFXPanel fxPanel = new JFXPanel() {};
        Platform.runLater(() -> {
            initFXCity(fxPanel);
        });
        return fxPanel;
    }

    private JFXPanel createFXGraph() {
        final JFXPanel fxPanel = new JFXPanel() {};
        Platform.runLater(() -> {
            initFXGraph(fxPanel);
        });
        return fxPanel;
    }

    private void initFXCity(JFXPanel fxPanel) {
        double sceneWidth = 800;
        double sceneHeight = 600;
        City city = new City(8000, 20, 8000);

        Quarter quarter = new Quarter(500, 10, 500, 50);
        quarter.setPosition(0, 0);

        Building building1 = new Building(100, 900, 100);
        Building building2 = new Building(100, 700, 100);
        Building building3 = new Building(100, 600, 100);
        Building building4 = new Building(100, 600, 100);
        Building building5 = new Building(200, 600, 200);
        Building building6 = new Building(50, 600, 50);
        Building building7 = new Building(100, 600, 100);
        Building building8 = new Building(100, 600, 100);
        building1.setNotes("dawfawfwa");
//        building1.setPosition(200,200);

        quarter.addAllBuildings(building1, building2, building3, building4, building5, building6, building7, building8 );

        city.addQuarter(quarter);

        Group group = new Group();
        group.getChildren().add(new MainSubScene(city, sceneWidth, sceneHeight, true, SceneAntialiasing.BALANCED));

        TabPane tabPane = new TabPane();
        Tab mainTab = new Tab("City", group);
        mainTab.closableProperty().set(false);
        tabPane.getTabs().add(mainTab);
        tabPane.getTabs().add(new Tab("2", new Pane()));

        Scene scene2 = new Scene(group, sceneWidth, sceneHeight, true, SceneAntialiasing.BALANCED);

        scene2.focusOwnerProperty().addListener(new ChangeListener<Node>() {
            @Override public void changed(ObservableValue<? extends Node> observable, Node oldValue, Node newValue) {
                System.out.println("focus owner: " + newValue);
            }
        });
        fxPanel.setScene(scene2);
    }

    private void initFXGraph(JFXPanel fxPanel) {
        double sceneWidth = 800;
        double sceneHeight = 600;
        Graph<String, String> g = build_sample_digraph();
        PlacementStrategy strategy = new CircularSortedPlacementStrategy();
        GraphPanel<String, String> graphView = new GraphPanel<>(g, strategy);

        if (g.numVertices() > 0) {
            graphView.getStylableVertex("A").setStyle("-fx-fill: gold; -fx-stroke: brown;");
        }
        Scene scene = new Scene(new GraphDemoContainer(graphView), sceneWidth, sceneHeight);

        graphView.setVertexDoubleClickAction((GraphVertex<String> graphVertex) -> {
            System.out.println("Vertex contains element: " + graphVertex.getUnderlyingVertex().element());

            if( !graphVertex.removeStyleClass("myVertex") ) {
                graphVertex.addStyleClass("myVertex");
            }
        });

        graphView.setEdgeDoubleClickAction(graphEdge -> {
            System.out.println("Edge contains element: " + graphEdge.getUnderlyingEdge().element());
            graphEdge.setStyle("-fx-stroke: black; -fx-stroke-width: 3;");
            graphEdge.getStylableArrow().setStyle("-fx-stroke: black; -fx-stroke-width: 3;");
        });
        fxPanel.setScene(scene);
    }

    private Graph<String, String> build_sample_digraph() {

        Digraph<String, String> g = new DigraphEdgeList<>();

        g.insertVertex("A", ElementTypes.PACKAGE, "<<package>> A\n included: B, C, D");
        g.insertVertex("B", ElementTypes.INTERFACE);
        g.insertVertex("C", ElementTypes.COMPONENT);
        g.insertVertex("D", ElementTypes.ENUM);
        g.insertVertex("E", ElementTypes.CLASS);
        g.insertVertex("F");
        g.insertVertex("main");

        g.insertEdge("A", "B", "AB", ArrowTypes.AGGREGATION);
        g.insertEdge("B", "A", "AB2", ArrowTypes.DEPENDENCY);
        g.insertEdge("A", "C", "AC", ArrowTypes.COMPOSITION);
        g.insertEdge("A", "D", "AD");
        g.insertEdge("B", "C", "BC");
        g.insertEdge("C", "D", "CD", ArrowTypes.REALIZATION);
        g.insertEdge("B", "E", "BE");
        g.insertEdge("F", "D", "DF");
        g.insertEdge("F", "D", "DF2");

        //yep, its a loop!
        g.insertEdge("A", "A", "Loop");

        return g;
    }
}
