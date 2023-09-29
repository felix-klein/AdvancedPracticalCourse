package edu.ground.blueprintData;

import edu.gate.hardware.SensIn;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Objects;

public class BlueprintSaver {
    public BlueprintSaver(SensIn sensorData) {
        saveBlueprintData(sensorData);
        new BlueprintDataAnalysis(sensorData);
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
        String element1 = "TMP=" + sensorData.TMP().get(i) + "&";
        String element2 = "VIB=" + sensorData.VIB().get(i) + "&";
        String element3 = "MIC=" + sensorData.MIC().get(i) + "&";
        String element4 = "CP1=" + sensorData.CP1().get(i) + "&";
        String element5 = "CP2=" + sensorData.CP2().get(i) + "&";
        String element6 = "CP3=" + sensorData.CP3().get(i);
        return element0 + element1 + element2 + element3 + element4 + element5 + element6;
    }
}
