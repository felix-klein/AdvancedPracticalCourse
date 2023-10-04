package edu.ground.analysis;

import edu.gate.hardware.SensIn;

import java.util.ArrayList;

public class SensInAnalysis {
    private final SensIn sensorData;
    private int timeDataPointer = 0;
    private int startElement = 0;

    ArrayList<ArrayList<Float>> TMP_perMission = new ArrayList<>();
    ArrayList<ArrayList<Double>> CP1_perMission = new ArrayList<>();
    ArrayList<ArrayList<Double>> CP2_perMission = new ArrayList<>();
    ArrayList<ArrayList<Double>> CP3_perMission = new ArrayList<>();
    ArrayList<ArrayList<Float>> VIB_perMission = new ArrayList<>();
    ArrayList<ArrayList<Float>> MIC_perMission = new ArrayList<>();

    /**
     * This is the Gate between the receiving sensor data of the hardware and the analysis in the backend.
     * The data from the SensIn has the syntax:
     *          *  1. Missions-Order:
     *          *  2. EGS=1&TMD=5000&TSP=345999
     *          *  3. Sensor-Data:
     *          *  4. TSP=3422&TMP=28.23&VIB=120&MIC=20&CP1=2.5&CP2=2.5&CP3=2.5
     *
     * @param sensorData is the sensor data from the sensors of the hardware connection.
     */
    public SensInAnalysis(SensIn sensorData) {
        this.sensorData = sensorData;
        /* Get the start of the real sensor data with the first command as the indicator. */
        int startDigit = sensorData.MIS().get(1).indexOf("tsp=") + 4;
        int commandGroupTimeStart = Integer.parseInt(sensorData.MIS().get(1).substring(startDigit));

        /* We loop through each mission individually and take its data. With the first real MIS at i = 2. */
        for (int i = 2; i < sensorData.MIS().size(); i++) {
            int startDigitNext = sensorData.MIS().get(i).indexOf("tsp=") + 4;
            int commandGroupTimeEnd = Integer.parseInt(sensorData.MIS().get(i).substring(startDigitNext));

            int amount = timeDataSearch(commandGroupTimeStart, commandGroupTimeEnd);
            /*
            All Mission_Analysers are taking the sensor data of one mission and calculate it
            truncated (arithmetic) mean for a per second grouping.
             */
            TMP_perMission.add(TMP_Mission_Analyser(amount));
            CP1_perMission.add(CP1_Mission_Analyser(amount));
            CP2_perMission.add(CP2_Mission_Analyser(amount));
            CP3_perMission.add(CP3_Mission_Analyser(amount));
            VIB_perMission.add(VIB_Mission_Analyser(amount));
            MIC_perMission.add(MIC_Mission_Analyser(amount));
        }
    }

    /**
     * This helper method finds the correct area/slots in the sensor data, corresponding to the time of the last
     * command.
     * @param startTime is the start time of the command for which the sensor data area should be selected.
     * @param endTime is the end time of the command for which the sensor data area should be selected.
     * @return a counter which represents the amount of sensor data of the command section.
     */
    private int timeDataSearch(int startTime, int endTime) {
        int counter = 0;
        /* As long as there is sensor data to investigate. */
        while(timeDataPointer < sensorData.TSP().size()) {
            /* Just the sensor data in this specific section. */
            if(sensorData.TSP().get(timeDataPointer) >= startTime && sensorData.TSP().get(timeDataPointer) <= endTime) {
                if (counter == 0) {
                    /* Saving the start element as a global variable for the further data analysis. */
                    startElement = timeDataPointer;
                }
                counter++;
            } else if (sensorData.TSP().get(timeDataPointer) > endTime) {
                return counter;
            }
            timeDataPointer++;
        }
        return counter;
    }

    /**
     * This Temperature analyser is analysing all the sensor data of the given time period in a per-second interval.
     *
     * @param amount is the amount of sensor data in the time period of the current mission.
     * @return an ArrayList of als the per second sensor results of the mission part.
     */
    private ArrayList<Float> TMP_Mission_Analyser(int amount) {
        ArrayList<Float> temperatures = new ArrayList<>();
        Float current = 0F;
        int counter = 0;
        Long timePassed = 0L;
        Long startTime = 0L;
        for (int i = startElement; i < (startElement + amount); i++) {
            if (counter == 0) {
                startTime = sensorData.TSP().get(i);
                current += sensorData.TMP().get(i);
                counter++;
            } else if ((timePassed-startTime) > 1000) {
                temperatures.add((1/counter) * current);
                counter = 0;
            } else {
                current += sensorData.TMP().get(i);
                counter++;
            }
        }
        return temperatures;
    }

    /**
     * This CP1 analyser is analysing all the sensor data of the given time period in a per-second interval.
     *
     * @param amount is the amount of sensor data in the time period of the current mission.
     * @return an ArrayList of als the per second sensor results of the mission part.
     */
    private ArrayList<Double> CP1_Mission_Analyser(int amount) {
        ArrayList<Double> cpOnes = new ArrayList<>();
        Double current = 0.0;
        int counter = 0;
        Long timePassed = 0L;
        Long startTime = 0L;
        for (int i = startElement; i < (startElement + amount); i++) {
            if (counter == 0) {
                startTime = sensorData.TSP().get(i);
                current += sensorData.CP1().get(i);
                counter++;
            } else if ((timePassed-startTime) > 1000) {
                cpOnes.add((1/counter) * current);
                counter = 0;
            } else {
                current += sensorData.CP1().get(i);
                counter++;
            }
        }
        return cpOnes;
    }

    /**
     * This CP2 analyser is analysing all the sensor data of the given time period in a per-second interval.
     *
     * @param amount is the amount of sensor data in the time period of the current mission.
     * @return an ArrayList of als the per second sensor results of the mission part.
     */
    private ArrayList<Double> CP2_Mission_Analyser(int amount) {
        ArrayList<Double> cpTwos = new ArrayList<>();
        Double current = 0.0;
        int counter = 0;
        Long timePassed = 0L;
        Long startTime = 0L;
        for (int i = startElement; i < (startElement + amount); i++) {
            if (counter == 0) {
                startTime = sensorData.TSP().get(i);
                current += sensorData.CP2().get(i);
                counter++;
            } else if ((timePassed-startTime) > 1000) {
                cpTwos.add((1/counter) * current);
                counter = 0;
            } else {
                current += sensorData.CP2().get(i);
                counter++;
            }
        }
        return cpTwos;
    }

    /**
     * This CP3 analyser is analysing all the sensor data of the given time period in a per-second interval.
     *
     * @param amount is the amount of sensor data in the time period of the current mission.
     * @return an ArrayList of als the per second sensor results of the mission part.
     */
    private ArrayList<Double> CP3_Mission_Analyser(int amount) {
        ArrayList<Double> cpThrees = new ArrayList<>();
        Double current = 0.0;
        int counter = 0;
        Long timePassed = 0L;
        Long startTime = 0L;
        for (int i = startElement; i < (startElement + amount); i++) {
            if (counter == 0) {
                startTime = sensorData.TSP().get(i);
                current += sensorData.CP3().get(i);
                counter++;
            } else if ((timePassed-startTime) > 1000) {
                cpThrees.add((1/counter) * current);
                counter = 0;
            } else {
                current += sensorData.CP3().get(i);
                counter++;
            }
        }
        return cpThrees;
    }

    /**
     * This Vibrations analyser is analysing all the sensor data of the given time period in a per-second interval.
     *
     * @param amount is the amount of sensor data in the time period of the current mission.
     * @return an ArrayList of als the per second sensor results of the mission part.
     */
    private ArrayList<Float> VIB_Mission_Analyser(int amount) {
        ArrayList<Float> vibrations = new ArrayList<>();
        Float current = 0F;
        int counter = 0;
        Long timePassed = 0L;
        Long startTime = 0L;
        for (int i = startElement; i < (startElement + amount); i++) {
            if (counter == 0) {
                startTime = sensorData.TSP().get(i);
                current += sensorData.VIB().get(i);
                counter++;
            } else if ((timePassed-startTime) > 1000) {
                vibrations.add((1/counter) * current);
                counter = 0;
            } else {
                current += sensorData.VIB().get(i);
                counter++;
            }
        }
        return vibrations;
    }

    /**
     * This Microphone analyser is analysing all the sensor data of the given time period in a per-second interval.
     *
     * @param amount is the amount of sensor data in the time period of the current mission.
     * @return an ArrayList of als the per second sensor results of the mission part.
     */
    private ArrayList<Float> MIC_Mission_Analyser(int amount) {
        ArrayList<Float> sounds = new ArrayList<>();
        Float current = 0F;
        int counter = 0;
        Long timePassed = 0L;
        Long startTime = 0L;
        for (int i = startElement; i < (startElement + amount); i++) {
            if (counter == 0) {
                startTime = sensorData.TSP().get(i);
                current += sensorData.MIC().get(i);
                counter++;
            } else if ((timePassed-startTime) > 1000) {
                sounds.add((1/counter) * current);
                counter = 0;
            } else {
                current += sensorData.MIC().get(i);
                counter++;
            }
        }
        return sounds;
    }

    /**
     * Getter for the per-second sensor data per-mission of the Temperature.
     *
     * @return a list of a list with each list element (a mission) and each list-list element the sensor data.
     */
    public ArrayList<ArrayList<Float>> getTMP_perMission() {
        return TMP_perMission;
    }

    /**
     * Getter for the per-second sensor data per-mission of CP1.
     *
     * @return a list of a list with each list element (a mission) and each list-list element the sensor data.
     */
    public ArrayList<ArrayList<Double>> getCP1_perMission() {
        return CP1_perMission;
    }

    /**
     * Getter for the per-second sensor data per-mission of CP2.
     *
     * @return a list of a list with each list element (a mission) and each list-list element the sensor data.
     */
    public ArrayList<ArrayList<Double>> getCP2_perMission() {
        return CP2_perMission;
    }

    /**
     * Getter for the per-second sensor data per-mission of CP3.
     *
     * @return a list of a list with each list element (a mission) and each list-list element the sensor data.
     */
    public ArrayList<ArrayList<Double>> getCP3_perMission() {
        return CP3_perMission;
    }

    /**
     * Getter for the per-second sensor data per-mission of the Vibration.
     *
     * @return a list of a list with each list element (a mission) and each list-list element the sensor data.
     */
    public ArrayList<ArrayList<Float>> getVIB_perMission() {
        return VIB_perMission;
    }

    /**
     * Getter for the per-second sensor data per-mission of the Microphone.
     *
     * @return a list of a list with each list element (a mission) and each list-list element the sensor data.
     */
    public ArrayList<ArrayList<Float>> getMIC_perMission() {
        return MIC_perMission;
    }

    /**
     * Getter for the missions of the original sensor data.
     *
     * @return the sensor data object as original.
     */
    public SensIn getSensorData() {
        return sensorData;
    }
}
