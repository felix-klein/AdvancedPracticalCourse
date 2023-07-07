package edu.gate.hardware;

/**
 * This class is the Hardware Gate to the Arduino board, as well as the transfer area to the Terminal.
 * The result is a dual function as transmitter and outside gate.
 */
public class HardwareGate {

    public HardwareGate() {
        new BoardConnection();
    }


}
