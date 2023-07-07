package edu.terminal;

import edu.gate.hardware.HardwareGate;
import edu.ground.datapreparation.FileProcessing;
import edu.ground.datapreparation.Triad;
import edu.ground.pm.ProcessFlow;

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
    private edu.ground.datapreparation.Triad processData;
    private edu.ground.datapreparation.Triad blueprintData;

    public Terminal() {
        initialized = false;
    }

    /**
     * From AdminController.
     * This method got coled from the AdminController to initialise the blueprint data via the blueprint engine.
     */
    public void initializeBlueprint(File blueprintProcess) {
        /* Convert the XML data of the process and structure it in a Triad object. */
        FileProcessing fileProcessing = new FileProcessing(blueprintProcess);
        this.blueprintData = fileProcessing.getProcessData();
        initialized = true;

    }

    /**
     * From gate/frontend/UserController.java.
     * This method got coled from the UserController to activate the process.
     * To ground/datapreparation/FileProcessing.java.
     *
     * @param userProcess   is the file including the BPMN notation to analyse.
     * @param accuracyLevel is the level of accuracy for the sensor data.
     * @param loopCount     is the number of loop passes, if there is one at all.
     * @return a boolean to indicate if the process could start of if the admin needs to initialise first.
     */
    public boolean startUserProcess(File userProcess, String accuracyLevel, int loopCount) {
        if (!initialized) {
            return false;
        }
        /* Convert the XML data of the process and structure it in a Triad object. */
        FileProcessing fileProcessing = new FileProcessing(userProcess);
        this.processData = fileProcessing.getProcessData();
        //new ProcessFlow(processData);
        new HardwareGate();
        return true;
    }

    /**
     * Getter to make the processData public.
     *
     * @return the Process Data in a Triad object.
     */
    public Triad getProcessData() {
        return processData;
    }

    /**
     * Getter to make the blueprintData public.
     *
     * @return the Blueprint (Ideal) Data in a Triad object.
     */
    public Triad getBlueprintData() {
        return blueprintData;
    }
}
