package edu.ground.datapreparation;

import java.util.ArrayList;

/**
 * Triad is a Record for storing the data of all Events, Tasks and Flows of the process.
 *
 * @param events is the list of all included Events.
 * @param tasks  is the list of all included Tasks.
 * @param flows  is the list of all included Flows.
 */
public record Triad(ArrayList<Event> events, ArrayList<Task> tasks, ArrayList<Flow> flows,
                    ArrayList<Gateway> gateways) {
}
