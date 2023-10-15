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

public class BlueprintDataAnalysis {
    public BlueprintDataAnalysis(SensIn sensorData) {
        saveAnalysedData(sensorData);
    }

    /**
     * This method uses the analysed sensor data to save it into a txt file for further analyses.
     *
     * @param sensorData is the sensor data.
     */
    private void saveAnalysedData(SensIn sensorData) {
        try {
            /* Indicate the right path in the correct format. */
            Path fullPath = Paths.get(Objects.requireNonNull(getClass()
                    .getResource("/data/blueprintAnalysedData.txt"))
                    .toURI());

            ArrayList<String> blueprintSheet = new ArrayList<>();
            int groupCounter = 0;
            int groupID = 1;
            for (int i = 0; i < sensorData.MIS().size(); i++) {
                /* The Switch statement is the naming of each mission for later recognising. */
                groupID = (groupID == 10) ? 1 : groupID;
                switch (groupCounter) {
                    case 0 -> { /* 0 -> 1 */
                        blueprintSheet.add(">>0.0");
                        groupCounter = 1;
                    }
                    case 1 -> { /* 1 -> 5 */
                        blueprintSheet.add(">>1." + groupID);
                        groupCounter = 5;
                    }
                    case 5 -> { /* 5 -> 3 */
                        blueprintSheet.add(">>5." + groupID);
                        groupCounter = 3;
                    }
                    case 3 -> { /* 3 -> 1 (if done 2) */
                        blueprintSheet.add(">>3." + groupID);
                        ++groupID;
                        groupCounter = (groupID == 10) ? 2 : 1;
                    }
                    case 2 -> { /* 2 -> 2 (if done 4) */
                        blueprintSheet.add(">>2." + groupID);
                        ++groupID;
                        groupCounter = (groupID == 10) ? 4 : 2;
                    }
                    case 4 -> { /* 4 -> 4 (if done -) */
                        blueprintSheet.add(">>4." + groupID);
                        ++groupID;
                    }
                }
                /* Iterate through all the data for the same second of the same mission. */
                for (int ii = 0; ii < sensorData.TMP_perMission().get(i).size(); ii++) {
                    String line = String.valueOf(sensorData.TMP_perMission().get(i).get(ii)) + '*' +
                            sensorData.MIC_perMission().get(i).get(ii) + '*' +
                            sensorData.VIB_perMission().get(i).get(ii) + '*' +
                            sensorData.CP1_perMission().get(i).get(ii) + '*' +
                            sensorData.CP2_perMission().get(i).get(ii) + '*' +
                            sensorData.CP3_perMission().get(i).get(ii);
                    /* The pattern will be: TMP*MIC*VIB*CP1*CP2*CP3 */
                    blueprintSheet.add(line);
                }
            }
            Files.write(fullPath, blueprintSheet, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (URISyntaxException | IOException e) {
            System.out.println("The saving or writing of the blueprint data does not work as expected!");
            e.getStackTrace();
        }
    }
}
