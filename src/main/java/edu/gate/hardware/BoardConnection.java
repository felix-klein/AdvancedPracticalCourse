package edu.gate.hardware;

import com.fazecast.jSerialComm.SerialPort;

public class BoardConnection {
    private final SerialPort serialPort = SerialPort.getCommPort("/dev/cu.usbmodem141201");

    public BoardConnection() {
        //TODO: need to update this test and modify and improve.
        serialPort.setBaudRate(115200);
        serialPort.openPort();

        StringBuilder dataBuilder = new StringBuilder();

        while (true) {
            if (serialPort.bytesAvailable() > 0) { // Check if there are bytes available
                byte[] buffer = new byte[serialPort.bytesAvailable()];
                int numRead = serialPort.readBytes(buffer, buffer.length);
                if (numRead > 0) {
                    String data = new String(buffer);
                    dataBuilder.append(data);
                    int packetEndIndex = dataBuilder.indexOf("\n");
                    if (packetEndIndex != -1) {
                        String packet = dataBuilder.substring(0, packetEndIndex);
                        System.out.println(packet);
                        dataBuilder.delete(0, packetEndIndex + 1);
                    }
                }
            }
        }
        //Source more or less: https://github.com/Fazecast/jSerialComm
    }
}

//Terminal:
// screen /dev/cu.usbmodem141201 115200
