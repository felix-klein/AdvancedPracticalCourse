package edu.ground.pm;

import edu.gate.hardware.SensIn;

public class ProcessFlow {
    /**
     * This class is the gate to the Terminal and to the process mining analyses and visualisation.
     *
     * @param sensorData is the sensor data of the latest user process as a SensIn object.
     */
    public ProcessFlow(SensIn sensorData) {
        /* The data from the blueprint has the syntax:
         *
         *  1. Missions-Order:
         *  2. EGS=1&TMD=5000&TSP=345999
         *  3. Sensor-Data:
         *  4. TSP=3422&TMP=28.23&VIB=120&MIC=20&CP1=2.5&CP2=2.5&CP3=2.5
         */
    }
}
