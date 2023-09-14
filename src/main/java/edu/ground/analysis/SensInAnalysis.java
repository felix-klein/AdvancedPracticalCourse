package edu.ground.analysis;

import edu.gate.hardware.SensIn;

public class SensInAnalysis {
    /**
     * This is the Gate between the receiving sensor data of the hardware and the analysis in teh backend.
     * The data from the SensIn has the syntax:
     *          *  1. Missions-Order:
     *          *  2. EGS=1&TMD=5000&TSP=345999
     *          *  3. Sensor-Data:
     *          *  4. TSP=3422&TMP=28.23&VIB=120&MIC=20&CP1=2.5&CP2=2.5&CP3=2.5
     *
     * @param sensorData is the sensor data from the sensors of the hardware connection.
     */
    public SensInAnalysis(SensIn sensorData) {
    }
}
