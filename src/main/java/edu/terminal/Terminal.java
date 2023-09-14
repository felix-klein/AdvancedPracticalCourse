package edu.terminal;

import edu.gate.hardware.HardwareGate;
import edu.gate.hardware.SensIn;
import edu.ground.cpeeGate.Gateway;
import edu.ground.pm.ProcessFlow;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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

    public Terminal() {
        /* If there is a blueprint text file, we consider this file as the correct file for this motor of control. */
        initialized = getClass().getResource("/data/blueprint.txt") != null;
    }

    /**
     * From gate/frontend/AdminController.java
     * This method got called from the AdminController to initialise the blueprint data via the blueprint engine.
     */
    public void initializeBlueprint() {
        /* Convert the XML data of the blueprint into the data command list for the Hardware. */
        ArrayList<String> blueprintData = new Gateway("blueprint", "extreme").getPreparedData();

        /* Using this process data to initialise and start the Hardware, which does save the blueprint results in a
         * txt file for further investigations. */
        HardwareGate hardwareInitialiseGate = new HardwareGate(blueprintData);
        /* Get the sensor data back and save it in a txt file for later analyses. */
        saveBlueprintData(hardwareInitialiseGate.getSENS());
        initialized = true;
    }

    /**
     * From gate/frontend/UserController.java.
     * This method got coled from the UserController to activate the process.
     * To ground/datapreparation/FileProcessing.java.
     *
     * @param accuracyLevel is the level of accuracy for the sensor data.
     * @return a boolean to indicate if the process could start of if the admin needs to initialise first.
     */
    public boolean startUserProcess(String type, String accuracyLevel) {
        if (!initialized) {
            return false; /* False if there is no blueprint data already from the admin */
        }
        /* Get and convert the xml data of the modeller to the String List for the Hardware.*/
        ArrayList<String> preparedData = new Gateway(type, accuracyLevel).getPreparedData();
        /* Start the process on the hardware. */
        HardwareGate hardwareUserGate = new HardwareGate(preparedData);
        /* Get the sensor data back and use it for the process flow drawing. */
        new ProcessFlow(hardwareUserGate.getSENS());
        return true;
    }

    /**
     * This helper method saves the blueprint generated data for long term into a txt file. Essential to compare
     * new test data with the ideal blueprint data from the admin initialization after reloading the project.
     *
     * @param sensorData is the sensor data from the admin initialisation of the blueprint.
     */
    private void saveBlueprintData(SensIn sensorData) {
        try {
            /* Indicate the right path in the correct format. */
            Path fullPath = Paths.get(Objects.requireNonNull(getClass().getResource("/data/blueprintData.txt"))
                    .toURI());

            /* Iterate through the data and write all the lines of the document. */
            ArrayList<String> blueprintSheet = new ArrayList<>();
            blueprintSheet.add("Missions-Order:");
            sensorData.MIS().replaceAll(String::toUpperCase);
            blueprintSheet.addAll(sensorData.MIS());
            blueprintSheet.add("Sensor-Data:");
            for (int i = 0; i < sensorData.TSP().size(); i++) {
                String allElements = getAllElements(sensorData, i);
                blueprintSheet.add(allElements);
            }
            Files.write(fullPath, blueprintSheet, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (URISyntaxException | IOException e) {
            System.out.println("The saving or writing of the blueprint data does not work as expected!");
            e.getStackTrace();
        }
    }

    /**
     * Construct a String including all the sensor data which is correlated to a specific time. Helper to create easier
     * to read code.
     *
     * @param sensorData is the SensIn object including all the sensor data of the hardware.
     * @param i is the specific object line, which will be combined.
     * @return a String which is all the sensor data of a line in one object.
     */
    private static String getAllElements(SensIn sensorData, int i) {
        String element0 = "TSP=" + sensorData.TSP().get(i) + "&";
        String element1 = "TMP=" + sensorData.TMP().get(i); //+ "&";
        /*
         * String element2 = "VIB=" + sensorData.VIB().get(i) + "&";
         * String element3 = "MIC=" + sensorData.MIC().get(i) + "&";
         * String element4 = "CP1=" + sensorData.CP1().get(i) + "&";
         * String element5 = "CP2=" + sensorData.CP2().get(i) + "&";
         * String element6 = "CP3=" + sensorData.CP3().get(i);
         */
        return element0 + element1; // + element2 + element3 + element4 + element5 + element6
    }
}
