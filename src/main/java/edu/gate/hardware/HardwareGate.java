package edu.gate.hardware;

import edu.ground.datapreparation.Triad;

/**
 * This class is the Hardware Gate to the Arduino board, as well as the transfer area to the Terminal.
 * The result is a dual function as transmitter and outside gate.
 */
public class HardwareGate {

    public HardwareGate(Triad processFlow) {
        new BoardConnection();
    }


}
