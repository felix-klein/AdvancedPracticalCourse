package edu.gate.hardware;

import java.util.ArrayList;

/**
 * SensIn is a Record for storing the sensor data of the Temperature, Vibration, Microphone and Current sensors.
 *
 * @param temperatures is a list of all temperatures gathered.
 * @param vibrations   is a list of all vibrations gathered.
 * @param loudness     is a list of all the level of loudness gathered.
 * @param currents     is a list of all the current gathered.
 */
/* TODO for test purposes.
public record SensIn(ArrayList<Temp> temperatures, ArrayList<Vib> vibrations, ArrayList<Mic> loudness,
                     ArrayList<Current> currents) {

}

 */

public record SensIn(ArrayList<Temp> temperatures) {}