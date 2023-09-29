package edu.ground.analysis;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ComplianceChecking {
    private ArrayList<String> idealData;
    private final SensInAnalysis sensorAnalyses;

    /* Test-Structure:
       1. Cold_Start_Fast:
        1. <*EST:1#*RPM:1#*TMD:60#>
        2. <*EST:1#*RPM:500#*TMD:60#>
        3. <*EST:1#*RPM:1000#*TMD:60#>
        4. <*EST:1#*RPM:1500#*TMD:60#>
        5. <*EST:1#*RPM:2000#*TMD:60#>
        6. <*EST:1#*RPM:2500#*TMD:60#>
        7. <*EST:1#*RPM:3000#*TMD:60#>
        8. <*EST:1#*RPM:3500#*TMD:60#>
        9. <*EST:1#*RPM:4000#*TMD:60#>

       2. Cold_Start_Slow&G_C_Up:
        1. <*EST:1#*RPM:1#*TMD:4#>
        2. <*RPM:500#*TMD:37#>
        3. <*RPM:1000#*TMD:37#>
        4. <*RPM:1500#*TMD:37#>
        5. <*RPM:2000#*TMD:37#>
        6. <*RPM:2500#*TMD:37#>
        7. <*RPM:3000#*TMD:37#>
        8. <*RPM:3500#*TMD:37#>
        9. <*RPM:4000#*TMD:37#>

       3. Cool_Down_Fast:
        1. <*EST:0#*RPM:1#*TMD:120#>
        2. <*EST:0#*RPM:500#*TMD:120#>
        3. <*EST:0#*RPM:1000#*TMD:120#>
        4. <*EST:0#*RPM:1500#*TMD:120#>
        5. <*EST:0#*RPM:2000#*TMD:120#>
        6. <*EST:0#*RPM:2500#*TMD:120#>
        7. <*EST:0#*RPM:3000#*TMD:120#>
        8. <*EST:0#*RPM:3500#*TMD:120#>
        9. <*EST:0#*RPM:4000#*TMD:120#>

       4. Cool_Down_Slow&G_C_Down:
        1. <*RPM:4000#*TMD:6#>
        2. <*RPM:3500#*TMD:37#>
        3. <*RPM:3000#*TMD:37#>
        4. <*RPM:2500#*TMD:37#>
        5. <*RPM:2000#*TMD:37#>
        6. <*RPM:1500#*TMD:37#>
        7. <*RPM:1000#*TMD:37#>
        8. <*RPM:500#*TMD:37#>
        9. <*RPM:1#*TMD:37 #>
        10. <*EST:0#*TMD:300#>

       5. Long_Run:
        1. <*RPM:1#*TMD:1200#>
        2. <*RPM:500#*TMD:1200#>
        3. <*RPM:1000#*TMD:1200#>
        4. <*RPM:1500#*TMD:1200#>
        5. <*RPM:2000#*TMD:1200#>
        6. <*RPM:2500#*TMD:1200#>
        7. <*RPM:3000#*TMD:1200#>
        8. <*RPM:3500#*TMD:1200#>
        9. <*RPM:4000#*TMD:1200#>
     */
    public ComplianceChecking(SensInAnalysis sensorAnalyses) {
        this.sensorAnalyses = sensorAnalyses;
        try {
            /* Get the ideal sensor data of the blueprint. */
            this.idealData = (ArrayList<String>) Files.
                    readAllLines(Paths.get(Objects.requireNonNull(getClass()
                            .getResource("/data/blueprintAnalysedData.txt")).getPath()));

            comparison();
        } catch (IOException e) {
            e.getStackTrace();
        }
    }

    private void comparison() {
        int missionRPMFrom;
        int missionRPMTo = 0;

        /* Iterate through all missions. */
        for (int i = 0; i < sensorAnalyses.getMIC_perMission().size(); i++) {
            /* We are looking for missions with a change in rpm. The "To" is under investigation. */
            missionRPMFrom = missionRPMTo;
            if (sensorAnalyses.getSensorData().MIS().get(i + 2).contains("rpm=")) {
                Pattern pattern = Pattern.compile("rpm=(\\d+)&");
                Matcher matcher = pattern.matcher(sensorAnalyses.getSensorData().MIS().get(i + 2));
                missionRPMTo = matcher.find() ?  Integer.parseInt(matcher.group(1)) :  0;
            }
            int startIndexIdeal = fromTo(missionRPMFrom, missionRPMTo);

        }
    }

    /**
     * The From-To method exists to find the start index of the ideal sensor data in the blueprint results. It is
     * searching for the correct part/section of the ideal test results by its ID-Name and a sensible quantisation
     * classification. Each blueprint test was executed at a multiple 500 RPM value. The user RPM is therefor comparable
     * to the closest ideal value.
     * e.g.:
     *  User RPM: 480       ->      corresponding ideal value section: 500 (including the area 250 - 749 RPM)
     *
     * @param missionRPMFrom is the preceding RPM value.
     * @param missionRPMTo is the current RPM value.
     * @return an index which indicates the first sensor data line for this specific comparison.
     */
    private int fromTo(int missionRPMFrom, int missionRPMTo) {
        /* Quantisation classification through rounding. Which the classification defined by the current RPM. */
        int id = (int) Math.round(missionRPMTo / 500.0) + 1;

        /* find the exact appropriate subsection in the blueprint data list. */
        if (missionRPMFrom == 0) { /* 1 */
            return idealData.indexOf(">>1." + id) + 1;
        } else if (missionRPMFrom < missionRPMTo) { /* 2 */
            return idealData.indexOf(">>2." + id) + 1;
        } else if (missionRPMTo == 0) { /* 3 */
            /* For the Cool_Down_Fast, the classification corresponds to the preceding RPM value. */
            id = (int) Math.round(missionRPMFrom / 500.0) + 1;
            return idealData.indexOf(">>3." + id) + 1;
        } else if (missionRPMFrom > missionRPMTo) { /* 4 */
            return idealData.indexOf(">>4." + id) + 1;
        } else { /* missionRPMFrom == missionRPMTo */  /* 5 */
            return idealData.indexOf(">>5." + id) + 1;
        }
    }
}
