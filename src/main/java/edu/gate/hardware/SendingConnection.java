package edu.gate.hardware;

import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;
import java.io.OutputStream;

public class SendingConnection { //TODO: Kill this class.
    private final SerialPort serialPort = SerialPort.getCommPort("/dev/cu.usbmodem143201");

    public SendingConnection() {
        serialPort.setBaudRate(115200);
        serialPort.openPort();

        OutputStream outputStream = serialPort.getOutputStream();
        String command = "start"; // Replace with your desired command
        try {
            outputStream.write(command.getBytes());
            outputStream.flush();
        } catch (IOException e) {
            System.out.println("Error in the output stream of the serial communication:");
            e.getStackTrace();
        }
        serialPort.closePort();
    }


}
