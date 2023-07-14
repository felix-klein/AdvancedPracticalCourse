package edu.gate.hardware;

import com.fazecast.jSerialComm.SerialPort;

/**
 * In this class the thread handling is archived for the parallel sending and receiving of the Hardware data via Serial
 * communication.
 */
public class SerialThread extends Thread {

    public SerialThread(SerialPort port) {
        SerialThread byThread = new SerialThread(port);

        byThread.start();

    }

    /**
     * This method is for the Thread to run in parallel.
     */
    @Override
    public void run() {
    }

    /**
     * Thread runner method: Does send the constructions to the arduino.
     */
    private void sendingSerial() {

    }

    /**
     * Home runner method: Does receive the sensor data from the arduino.
     */
    private void receivingSerial() {

    }
}
