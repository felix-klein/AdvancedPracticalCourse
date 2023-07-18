package edu.ground.pm;

import edu.ground.datapreparation.*;
import guru.nidi.graphviz.attribute.*;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Graph;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static guru.nidi.graphviz.model.Factory.graph;
import static guru.nidi.graphviz.model.Factory.node;

public class GraphvizModeler {
    private Graph graph;
    private ArrayList<Flow> flows;

    public GraphvizModeler(Triad processData) {
        /* initialise the Triad elements individually for easier to read addressing. */
        ArrayList<Event> events = processData.events();
        this.flows = processData.flows();
        ArrayList<Task> tasks = processData.tasks();
        ArrayList<Gateway> gateways = processData.gateways();

        this.graph = createGraph();
        addTasks(tasks);
        addEvents(events);
        addGateways(gateways);

        try {
            Graphviz.fromGraph(graph).height(100).render(Format.PNG).toFile(new File("/ProcessMining/PN.png"));
        } catch (IOException e) {
            System.out.println("The new PetriNet image, couldn't be saved");
            e.getStackTrace();
        }
    }

    /**
     * Helper method to create the graph in a visible manner.
     *
     * @return a graph object.
     */
    private Graph createGraph() {
        return graph("Compliance Overview").directed()
                .graphAttr().with(Rank.dir(Rank.RankDir.LEFT_TO_RIGHT))
                .nodeAttr().with(Font.name("Helvetica"))
                .linkAttr().with("class", "link-class");
    }

    /**
     * Method to add the nodes including there respective color to the graph.
     *
     * @param tasks is the list of Tasks from the Camunda Modeler.
     */
    private void addTasks(ArrayList<Task> tasks) {
        for (int i = 0; i < tasks.size(); i++) {
            String id = tasks.get(i).getId();
            Color color = tasks.get(i).isInAcceptance() ?
                    Color.rgb(1, 135, 134) : Color.rgb(176, 0, 32);

            graph.with(node(id).with(color, Shape.RECTANGLE, Style.FILLED, Color.WHITE.font(), Label.of("T" + i)));
        }
    }

    /**
     * Method to add the node of Events including their respective color and form.
     *
     * @param events is the list of Events from the Camunda Modeler.
     */
    private void addEvents(ArrayList<Event> events) {
        for (Event event : events) {
            String id = event.getId();
            Color color = Color.rgb(32, 37, 42);
            Style style = switch (event.getType()) {
                case "start" -> Style.SOLID;
                case "end" -> Style.BOLD;
                default -> Style.DASHED;
            };

            graph.with(node(id).with(color, Shape.CIRCLE, style, Label.of(event.getName())));
        }
    }

    /**
     * Method to add the node of Gateway including their respective form.
     *
     * @param gateways is the list of Gateways from the Camunda Modeler
     */
    private void addGateways(ArrayList<Gateway> gateways) {
        for (Gateway gateway : gateways) {
            String id = gateway.getId();
            Label label = gateway.getType() ? Label.of("+") : Label.of("x");
            Color color = Color.rgb(32, 37, 42);

            graph.with(node(id).with(color, Shape.DIAMOND, label, Style.BOLD));
        }
    }
}
