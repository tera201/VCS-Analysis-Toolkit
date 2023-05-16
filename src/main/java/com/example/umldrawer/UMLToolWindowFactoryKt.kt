package com.example.umldrawer

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBTabbedPane
import cpp.console.CppParserRunner
import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.embed.swing.JFXPanel
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.SceneAntialiasing
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.layout.Pane
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.example.MainSubScene
import org.example.elements.Building
import org.example.elements.City
import org.example.elements.Quarter
import umlgraph.containers.GraphDemoContainer
import umlgraph.graph.Digraph
import umlgraph.graph.DigraphEdgeList
import umlgraph.graph.Graph
import umlgraph.graphview.GraphPanel
import umlgraph.graphview.arrows.ArrowTypes
import umlgraph.graphview.edges.Edge
import umlgraph.graphview.strategy.CircularSortedPlacementStrategy
import umlgraph.graphview.strategy.PlacementStrategy
import umlgraph.graphview.vertices.GraphVertex
import umlgraph.graphview.vertices.elements.ElementTypes
import java.awt.BorderLayout
import javax.swing.JPanel


class UMLToolWindowFactoryKt : ToolWindowFactory {

    private val log: Logger = LogManager.getLogger(UMLToolWindowFactoryKt::class.java)
//    private val LOG: Logger = Logger.getInstance(MyTask::class.java)
//    private val logger = KotlinLogging.logger {}

    /**
     * Create the tool window content.
     *
     * @param project    current project
     * @param toolWindow current tool windowA
     */
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindowContent = JPanel()
        myToolWindowContent.autoscrolls = true
        val component = toolWindow.component

        val jtp = JBTabbedPane()
        jtp.add("FX Graph", createFXGraph())
        jtp.add("FX City", createFXCity())

//        val buildModel = BuildModel()
//        val repo = buildModel.createClone("https://github.com/microsoft/cpprestsdk.git")

        val cppParserRunner = CppParserRunner()
        val cppFiles = cppParserRunner.collectFiles("C:\\Users\\rnaryshkin\\IdeaProjects\\cpp2uml/CppToUMLSamples/src")
        val model = cppParserRunner.buildModel("CppParserRunnerSampleModel", cppFiles)

//        repo.scm.delete()

        myToolWindowContent.add(jtp, BorderLayout.CENTER)
        myToolWindowContent.isVisible = true
        component.parent.add(myToolWindowContent)
    }

    private fun createFXCity(): JFXPanel? {
        val fxPanel: JFXPanel = object : JFXPanel() {}
        Platform.runLater { initFXCity(fxPanel) }
        return fxPanel
    }

    private fun createFXGraph(): JFXPanel? {
        val fxPanel: JFXPanel = object : JFXPanel() {}
        Platform.runLater { initFXGraph(fxPanel) }
        return fxPanel
    }

    private fun initFXCity(fxPanel: JFXPanel) {
        val sceneWidth = 800.0
        val sceneHeight = 600.0
        val city = City(8000.0, 20.0, 8000.0)
        val quarter = Quarter(500.0, 10.0, 500.0, 50.0)
        quarter.setPosition(0.0, 0.0)
        val building1 = Building(100.0, 900.0, 100.0)
        val building2 = Building(100.0, 700.0, 100.0)
        val building3 = Building(100.0, 600.0, 100.0)
        val building4 = Building(100.0, 600.0, 100.0)
        val building5 = Building(200.0, 600.0, 200.0)
        val building6 = Building(50.0, 600.0, 50.0)
        val building7 = Building(100.0, 600.0, 100.0)
        val building8 = Building(100.0, 600.0, 100.0)
        building1.setNotes("dawfawfwa")
        //        building1.setPosition(200,200);
        quarter.addAllBuildings(building1, building2, building3, building4, building5, building6, building7, building8)
        city.addQuarter(quarter)
        val group = Group()
        group.children.add(MainSubScene(city, sceneWidth, sceneHeight, true, SceneAntialiasing.BALANCED))
        val tabPane = TabPane()
        val mainTab = Tab("City", group)
        mainTab.closableProperty().set(false)
        tabPane.tabs.add(mainTab)
        tabPane.tabs.add(Tab("2", Pane()))
        val scene2 = Scene(group, sceneWidth, sceneHeight, true, SceneAntialiasing.BALANCED)
        scene2.focusOwnerProperty().addListener(object : ChangeListener<Node?> {
            override fun changed(observable: ObservableValue<out Node?>?, oldValue: Node?, newValue: Node?) {
                println("focus owner: $newValue")
            }
        })
        fxPanel.scene = scene2
    }

    private fun initFXGraph(fxPanel: JFXPanel) {
        val sceneWidth = 800.0
        val sceneHeight = 600.0
        val g = build_sample_digraph()
        val strategy: PlacementStrategy = CircularSortedPlacementStrategy()
        val graphView = GraphPanel(g, strategy)
        if (g.numVertices() > 0) {
            graphView.getStylableVertex("A").setStyle("-fx-fill: gold; -fx-stroke: brown;")
        }
        val scene = Scene(GraphDemoContainer(graphView), sceneWidth, sceneHeight)
        graphView.setVertexDoubleClickAction { graphVertex: GraphVertex<String> ->
            println("Vertex contains element: " + graphVertex.underlyingVertex.element())
            if (!graphVertex.removeStyleClass("myVertex")) {
                graphVertex.addStyleClass("myVertex")
            }
        }
        graphView.setEdgeDoubleClickAction { graphEdge: Edge<String, String> ->
            println("Edge contains element: " + graphEdge.underlyingEdge.element())
            graphEdge.setStyle("-fx-stroke: black; -fx-stroke-width: 3;")
            graphEdge.stylableArrow.setStyle("-fx-stroke: black; -fx-stroke-width: 3;")
        }
        fxPanel.scene = scene
    }

    private fun build_sample_digraph(): Graph<String, String> {
        val g: Digraph<String, String> = DigraphEdgeList()
        g.insertVertex("A", ElementTypes.PACKAGE, "<<package>> A\n included: B, C, D")
        g.insertVertex("B", ElementTypes.INTERFACE)
        g.insertVertex("C", ElementTypes.COMPONENT)
        g.insertVertex("D", ElementTypes.ENUM)
        g.insertVertex("E", ElementTypes.CLASS)
        g.insertVertex("F")
        g.insertVertex("main")
        g.insertEdge("A", "B", "AB", ArrowTypes.AGGREGATION)
        g.insertEdge("B", "A", "AB2", ArrowTypes.DEPENDENCY)
        g.insertEdge("A", "C", "AC", ArrowTypes.COMPOSITION)
        g.insertEdge("A", "D", "AD")
        g.insertEdge("B", "C", "BC")
        g.insertEdge("C", "D", "CD", ArrowTypes.REALIZATION)
        g.insertEdge("B", "E", "BE")
        g.insertEdge("F", "D", "DF")
        g.insertEdge("F", "D", "DF2")

        //yep, it's a loop!
        g.insertEdge("A", "A", "Loop")
        return g
    }
}