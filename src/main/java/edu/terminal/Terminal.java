package edu.terminal;

import edu.gate.hardware.HardwareGate;
import edu.ground.datapreparation.FileProcessing;
import edu.ground.datapreparation.Triad;

import java.io.File;

/**
 * The Terminal class is meant to be like the terminal at an airport, to coordinate a static and ordered transfer.
 * In our case the Terminal class orders the incoming requests and coordinates them to the corresponding classes and
 * methods to operate them. This approach is slightly inefficient in terms of runtime, because of the extra node, but
 * does increase the overview and transparency immense.
 */
public class Terminal {
    private boolean initialized;
    private edu.ground.datapreparation.Triad processData;
    private edu.ground.datapreparation.Triad blueprintData;

    public Terminal() {
        /* If there is a blueprint text file, we consider this file as the correct file for this motor of control. */
        initialized = getClass().getResource("/data/blueprint.txt") != null;
    }

    /**
     * From AdminController.
     * This method got coled from the AdminController to initialise the blueprint data via the blueprint engine.
     *
     * @param blueprintProcess is a file containing the Process of the blueprint.
     */
    public void initializeBlueprint(File blueprintProcess) {
        /* Convert the XML data of the process and structure it in a Triad object. */
        FileProcessing fileProcessing = new FileProcessing(blueprintProcess);
        /* Saves the Triad object locally. */
        this.blueprintData = fileProcessing.getProcessData();
        /* Using this process data to initialise and start the Hardware, which does save the blueprint results in
            a txt file for further investigations.
         */
        new HardwareGate(blueprintData, "extreme", 1);
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
        new HardwareGate(processData, accuracyLevel, loopCount);
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
