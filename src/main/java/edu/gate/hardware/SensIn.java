package edu.gate.hardware;

import java.util.ArrayList;

/**
 * SensIn is a Record for storing the sensor data of the Temperature, Vibration, Microphone and Current sensors,
 * corresponding to their time of sensing.
 *
 * @param TMP is a list of all the Temperature gathered.
 * @param VIB is a list of all the Vibration gathered.
 * @param MIC is a list of all the Microphone levels of loudness gathered.
 * @param CP1 is a list of all the sensor values at the Current-Phase-1.
 * @param CP2 is a list of all the sensor values at the Current-Phase-2.
 * @param CP3 is a list of all the sensor values at the Current-Phase-3.
 * @param TSP is a list of all the respective Time-Stamps gathered.
 * @param MIS is a list of all the Mission-Initialisation-Stamps (not corresponding to the rest).
 */
public record SensIn(ArrayList<Float> TMP, ArrayList<Float> VIB, ArrayList<Short> MIC, ArrayList<Double> CP1,
                     ArrayList<Double> CP2, ArrayList<Double> CP3, ArrayList<Long> TSP, ArrayList<String> MIS) {

}