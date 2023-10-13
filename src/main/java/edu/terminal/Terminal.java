package edu.terminal;

import edu.gate.hardware.HardwareGate;
import edu.ground.analysis.*;
import edu.ground.blueprintData.BlueprintDataAnalysis;
import edu.ground.cpeeGate.Gateway;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;

/**
 * The Terminal class is meant to be like the terminal at an airport, to coordinate a static and ordered transfer.
 * In our case the Terminal class orders the incoming requests and coordinates them to the corresponding classes and
 * methods to operate them. This approach is slightly inefficient in terms of runtime, because of the extra node, but
 * does increase the overview and transparency immense.
 */
public class Terminal {
    private boolean initialized;
    private ComplianceResults complianceResults;

    public Terminal() {
        /* If there is a blueprint text file, we consider this file as the correct file for this motor of control. */
        initialized = getClass().getResource("/data/blueprintAnalysedData.txt") != null;
    }

    /**
     * From gate/frontend/AdminController.java
     * This method got called from the AdminController to initialise the blueprint data via the blueprint engine.
     */
    public void initializeBlueprint() {
        /* Get the blueprint commands which are saved in a txt file. */
        try {
            ArrayList<String> blueprintData = (ArrayList<String>) Files.
                    readAllLines(Paths.get(Objects.requireNonNull(getClass()
                            .getResource("/data/BlueprintCommandFlowHardware.txt")).getPath()));
            /* Using this process data to initialise and start the Hardware, which does save the blueprint results in a
             * txt file for further investigations. */
            HardwareGate hardwareInitialiseGate = new HardwareGate(blueprintData);
            /* Get the sensor data back and save it in a txt file for later analyses. */
            new BlueprintDataAnalysis(hardwareInitialiseGate.getSENS());
            initialized = true;
        } catch (IOException e) {
            e.getStackTrace();
            initialized = false;
        }
    }

    /**
     * From gate/frontend/UserController.java.
     * This method got coled from the UserController to activate the process.
     *
     *
     * @param accuracyLevel is the level of accuracy for the sensor data.
     * @return a boolean to indicate if the process could start of if the admin needs to initialise first.
     */
    public boolean startUserProcess(int type, String accuracyLevel, int deviationPercentage,
                                    int acceptancePercentage) {
        if (!initialized) {
            return false; /* False if there is no blueprint data already from the admin */
        }
        /* Get and convert the xml data of the modeller to the String List for the Hardware.*/
        ArrayList<String> preparedData = new Gateway(type, accuracyLevel).getPreparedData();
        /* Start the process on the hardware. */
        HardwareGate hardwareUserGate = new HardwareGate(preparedData);
        ComplianceChecking complianceCheck = new ComplianceChecking(hardwareUserGate.getSENS(), deviationPercentage,
                acceptancePercentage, accuracyLevel);
        this.complianceResults = complianceCheck.getComplianceResults();
        return true;
    }

    public ComplianceResults getComplianceResults() {
        return complianceResults;
    }
}
