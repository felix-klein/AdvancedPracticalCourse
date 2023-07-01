package edu.terminal;

import java.io.File;

/**
 * The Terminal class is meant to be like the terminal at an airport, to coordinate a static and ordered transfer.
 * In our case the Terminal class orders the incoming requests and coordinates them to the corresponding classes and
 * methods to operate them. This approach is slightly inefficient in terms of runtime, because of the extra node, but
 * does increase the overview and transparency immense.
 */
public class Terminal {
    private File userProcess;
    final private String setupProcess = "/adminFiles/setup.bpmn";
    private boolean initialized;

    public Terminal() {
        initialized = false;
    }

    /**
     * From AdminController.
     * This method got coled from the AdminController to initialise the blueprint data via the blueprint engine.
     */
    public void initializeBlueprint() {
        //TODO: Initialise the blueprint, including the adminFiles.
        initialized = true;
    }

    /**
     * From UserController.
     * This method got coled from the UserController to activate the process.
     *
     * @param userProcess   is the file including the BPMN notation to analyse.
     * @param accuracyLevel is the level of accuracy for the sensor data.
     * @param loopCount     is the number of loop passes, if there is one at all.
     */
    public void startUserProcess(File userProcess, String accuracyLevel, int loopCount) {
        this.userProcess = userProcess;
    }
}
