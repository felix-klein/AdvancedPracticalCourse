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
    private ComplianceResults complianceResults;

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
        this.complianceResults = new ComplianceResults();
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

    /**
     * The comparison method, starts and proceeds the main compliance checking comparison by iterating through all
     * missions and its sensor data.
     *
     * First we analyse the surround300 sensor data for the default environment sensor data. Secondly we iterate
     * through all missions and see if there is a change in RPM (which indicates a change in the comparison section).
     * If we do have a mission without a change in RPM or if the current mission is a very long one, the change to
     * a long run mission in the comparison is fulfilled.
     */
    private void comparison() {
        int missionRPMFrom;
        int missionRPMTo = 0;

        /* Analyse at first the surround data of the 300. */
        noiseComparison300();


        /* Iterate through all missions, except the surround initialisation 300. */
        for (int i = 1; i < sensorAnalyses.getMIC_perMission().size(); i++) {
            /* We are looking for missions with a change in rpm. The "To" is under investigation. */
            missionRPMFrom = missionRPMTo;
            /* Special treatment for missions with change in RPM for the comparison. */
            if (sensorAnalyses.getSensorData().MIS().get(i).contains("rpm=")) {
                Pattern pattern = Pattern.compile("rpm=(\\d+)&");
                Matcher matcher = pattern.matcher(sensorAnalyses.getSensorData().MIS().get(i));
                missionRPMTo = matcher.find() ?  Integer.parseInt(matcher.group(1)) :  0;
            }
            int startIndexIdeal = fromTo(missionRPMFrom, missionRPMTo);

            comparePerSecond(i, startIndexIdeal);
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

    /**
     * To compare each second of a missions sensor data, the ideal values with the test values.
     *
     * @param mission is the index which indicates the current mission.
     * @param startIndexIdeal is the index inside the ideal data array where the comparison should start.
     */
    private void comparePerSecond(int mission, int startIndexIdeal) {
        /* Add the current mission to the missions of the Compliance Results. */
        complianceResults.setMissions(sensorAnalyses.getSensorData().MIS().get(mission));

        ArrayList<ComplianceResults.DataList> dataTMP = new ArrayList<>();
        ArrayList<ComplianceResults.DataList> dataVIB = new ArrayList<>();
        ArrayList<ComplianceResults.DataList> dataMIC = new ArrayList<>();
        ArrayList<ComplianceResults.DataList> dataCP1 = new ArrayList<>();
        ArrayList<ComplianceResults.DataList> dataCP2 = new ArrayList<>();
        ArrayList<ComplianceResults.DataList> dataCP3 = new ArrayList<>();

        for (int i = 0; i < sensorAnalyses.getMIC_perMission().get(mission).size(); i++) {
            /* The pattern will be: TMP*MIC*VIB*CP1*CP2*CP3 */
            String[] idealLine = idealData.get(startIndexIdeal).split(String.valueOf('*'));

            /* TMP: Sensor analyses. */
            double idealA = Double.parseDouble(idealLine[0]);
            double testA = sensorAnalyses.getTMP_perMission().get(mission).get(i);
            double testNoise = complianceResults.getTestNoise().TMP();
            double idealNoise = complianceResults.getIdealNoise().TMP();
            dataTMP.add(dataListCreation(idealA, testA, testNoise, idealNoise));

            /* MIC: Sensor analyses. */
            idealA = Double.parseDouble(idealLine[1]);
            testA = sensorAnalyses.getMIC_perMission().get(mission).get(i);
            testNoise = complianceResults.getTestNoise().MIC();
            idealNoise = complianceResults.getIdealNoise().MIC();
            dataMIC.add(dataListCreation(idealA, testA, testNoise, idealNoise));

            /* VIB: Sensor analyses. */
            idealA = Double.parseDouble(idealLine[2]);
            testA = sensorAnalyses.getVIB_perMission().get(mission).get(i);
            testNoise = complianceResults.getTestNoise().VIB();
            idealNoise = complianceResults.getIdealNoise().VIB();
            dataVIB.add(dataListCreation(idealA, testA, testNoise, idealNoise));

            /* CP1: Sensor analyses. */
            idealA = Double.parseDouble(idealLine[3]);
            testA = sensorAnalyses.getCP1_perMission().get(mission).get(i);
            testNoise = complianceResults.getTestNoise().CP1();
            idealNoise = complianceResults.getIdealNoise().CP1();
            dataCP1.add(dataListCreation(idealA, testA, testNoise, idealNoise));

            /* CP2: Sensor analyses. */
            idealA = Double.parseDouble(idealLine[4]);
            testA = sensorAnalyses.getCP2_perMission().get(mission).get(i);
            testNoise = complianceResults.getTestNoise().CP2();
            idealNoise = complianceResults.getIdealNoise().CP2();
            dataCP2.add(dataListCreation(idealA, testA, testNoise, idealNoise));

            /* CP3: Sensor analyses. */
            idealA = Double.parseDouble(idealLine[5]);
            testA = sensorAnalyses.getCP3_perMission().get(mission).get(i);
            testNoise = complianceResults.getTestNoise().CP3();
            idealNoise = complianceResults.getIdealNoise().CP3();
            dataCP3.add(dataListCreation(idealA, testA, testNoise, idealNoise));
        }

        complianceResults.setDataTMP(dataTMP);
        complianceResults.setDataMIC(dataMIC);
        complianceResults.setDataVIB(dataVIB);
        complianceResults.setDataCP1(dataCP1);
        complianceResults.setDataCP2(dataCP2);
        complianceResults.setDataCP3(dataCP3);

        // Compare each corresponding second and create a list of true or false to generate the total true or false.
        // Adjust the ComplianceResults.DataList object with a boolean.
    }

    /**
     * Helper method for creation of DataList objects.
     *
     * @param idealA is the absolute value of the ideal data.
     * @param testA is the absolute value of the test data.
     * @param testNoise is the noise of the test data.
     * @param idealNoise is the noise of the ideal data.
     * @return a single DataList object.
     */
    private ComplianceResults.DataList dataListCreation(double idealA, double testA, double testNoise, double idealNoise) {
        double idealR = idealA - idealNoise;
        double testR = testA - testNoise;
        double diffRelative = idealR - testR;
        double diffAbsolute = idealA - testA;
        double deviation = (Math.abs(diffRelative)/idealR) * 100;/* deviation in percent */
        return new ComplianceResults.DataList(idealR, testR, diffRelative, diffAbsolute, idealA, testA, deviation);
    }

    /**
     * Compare the surrounding noise level for later canceling out. It is called 300 because of its duration of 300
     * seconds.
     *
     * It was planed to use a truncated (arithmetic) mean, but the sorting of the data and the corresponding calculation
     * would increase the runtime significantly, and so the construction of a good and reliable test area is crucial.
     */
    private void noiseComparison300() {
        /* Noise analyses of the ideal run. */
        int counter = 1;
        double sumTMP = 0;
        double sumMIC = 0;
        double sumVIB = 0;
        double sumCP1 = 0;
        double sumCP2 = 0;
        double sumCP3 = 0;
        /* Loop to analyse each data string. */
        while (!idealData.get(counter).contains(">>")) {
            /* The pattern will be: TMP*MIC*VIB*CP1*CP2*CP3 */
            String[] individual = idealData.get(counter).split(String.valueOf('*'));
            sumTMP += Double.parseDouble(individual[0]);
            sumMIC += Double.parseDouble(individual[1]);
            sumVIB += Double.parseDouble(individual[2]);
            sumCP1 += Double.parseDouble(individual[3]);
            sumCP2 += Double.parseDouble(individual[4]);
            sumCP3 += Double.parseDouble(individual[5]);
            counter++;
        }
        complianceResults.setIdealNoise(new ComplianceResults.Noise(
                (sumTMP /counter - 1),
                (sumMIC /counter - 1),
                (sumVIB /counter - 1),
                (sumCP1 /counter - 1),
                (sumCP2 /counter - 1),
                (sumCP3 /counter - 1)));

        /* Noise analyses of the test run. */
        counter = 0;
        sumTMP = sumMIC = sumVIB = sumCP1 = sumCP2 = sumCP3 = 0.0;
        /* Loop to analyse each sensor element for the mission 0 which corresponds to the 300. */
        while (counter < sensorAnalyses.getMIC_perMission().get(0).size()) {
            sumTMP += sensorAnalyses.getTMP_perMission().get(0).get(counter);
            sumMIC += sensorAnalyses.getMIC_perMission().get(0).get(counter);
            sumVIB += sensorAnalyses.getVIB_perMission().get(0).get(counter);
            sumCP1 += sensorAnalyses.getCP1_perMission().get(0).get(counter);
            sumCP2 += sensorAnalyses.getCP2_perMission().get(0).get(counter);
            sumCP3 += sensorAnalyses.getCP3_perMission().get(0).get(counter);
            counter++;
        }
        complianceResults.setTestNoise(new ComplianceResults.Noise(
                (sumTMP/counter),
                (sumMIC/counter),
                (sumVIB/counter),
                (sumCP1/counter),
                (sumCP2/counter),
                (sumCP3/counter)));
    }
}
