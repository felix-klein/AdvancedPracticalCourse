package edu.gate.hardware;

/**
 * Data type to save a current object.
 *
 * @param CP1 is the sensor value at the Current-Phase-1.
 * @param CP2 is the sensor value at the Current-Phase-2.
 * @param CP3 is the sensor value at the Current-Phase-3.
 * @param TSP is the Time-Stamp.
 */
public record Current(Long CP1, Long CP2, Long CP3, Long TSP) {

}
