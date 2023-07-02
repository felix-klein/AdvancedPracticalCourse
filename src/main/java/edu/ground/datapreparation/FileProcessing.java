package edu.ground.datapreparation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * The class FileProcessing is the entrance and outcome of the data preparation from the XML files.
 */
public class FileProcessing {
    private final Triad processData;

    public FileProcessing(File file) {
        this.processData = xmlSelection(file);
    }

    /**
     * This method converts the XML file into an Object containing all Events, Tasks and Flows.
     *
     * @param file is the XML file.
     * @return a Triad object which contains a list of Events, Tasks and Flows.
     */
    private Triad xmlSelection(File file) {
        /* Initializing the three array lists for the Triad object. */
        ArrayList<Event> events = new ArrayList<>();
        ArrayList<Task> tasks = new ArrayList<>();
        ArrayList<Flow> flows = new ArrayList<>();
        /* Parameter indicating if we are in the process area of the file. */
        boolean inProcessArea = false;

        /* Try to read the file and each line of it as a String. */
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            while (bufferedReader.ready()) {
                /* Convert each line to lower case, for easier search. */
                String currentLine = bufferedReader.readLine();
                /* Just investigate lines within the process area. */
                if (inProcessArea) {
                    if (currentLine.contains("Task id=") || currentLine.contains("task id=")) { /* Task line */
                        tasks.add(new Task(currentLine));
                    } else if (currentLine.contains("<bpmn:sequenceFlow id=")) { /* Flow line */
                        flows.add(new Flow(currentLine));
                    } else if (currentLine.contains("Event id=")) { /* Event line */
                        events.add(new Event(currentLine));
                    } else if (currentLine.contains("</bpmn:process>")) { /* End of Process line */
                        return new Triad(events, tasks, flows);
                    }
                }
                /* The start of the process is declared in the parameter inProcessArea. To get declared as inProcessArea
                 *   true, we need to be outside the process and the line needs to contain the process start.
                 */
                if (!inProcessArea && currentLine.contains("<bpmn:process id=")) {
                    inProcessArea = true;
                }
            }
        } catch (IOException e) {
            System.out.println("There is an error with the file path or the file does not exist in the provided path");
        }
        return null; /* without extraordinary incidents this is never needed */
    }

    /**
     * Getter of Triad (a Record class) to make the ProcessData public.
     *
     * @return a Triad object.
     */
    public Triad getProcessData() {
        return processData;
    }
}
