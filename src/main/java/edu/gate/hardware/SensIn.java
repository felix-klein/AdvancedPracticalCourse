package edu.gate.hardware;

import java.util.ArrayList;

/**
 * SensIn is a Record for storing the sensor data of the Temperature, Vibration, Microphone and Current sensors,
 * corresponding to their time of sensing.
 *
 * @param TMP_perMission is a list of all the Temperature gathered.
 * @param VIB_perMission is a list of all the Vibration gathered.
 * @param MIC_perMission is a list of all the Microphone levels of loudness gathered.
 * @param CP1_perMission is a list of all the sensor values at the Current-Phase-1.
 * @param CP2_perMission is a list of all the sensor values at the Current-Phase-2.
 * @param CP3_perMission is a list of all the sensor values at the Current-Phase-3.
 * @param TSP_perMission is a list of all the respective Time-Stamps gathered.
 * @param MIS            is a list of all the Mission-Initialisation-Stamps (not corresponding to the rest).
 */
public record SensIn(ArrayList<ArrayList<Float>> TMP_perMission, ArrayList<ArrayList<Float>> VIB_perMission,
                     ArrayList<ArrayList<Float>> MIC_perMission, ArrayList<ArrayList<Float>> CP1_perMission,
                     ArrayList<ArrayList<Float>> CP2_perMission, ArrayList<ArrayList<Float>> CP3_perMission,
                     ArrayList<ArrayList<Integer>> TSP_perMission, ArrayList<String> MIS) {
}