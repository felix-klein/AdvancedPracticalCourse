package edu.gate.hardware;

import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

/**
 * This class is the communication gate between this program and the board. It establishes a serial communication which
 * is used to send instructions and to receive sensor data and results.
 */
public class BoardConnection {

    public BoardConnection() {
        SerialPort port = SerialPort.getCommPort(Objects.requireNonNull(getSetupData("Port:")));
        int baudRate = Integer.parseInt(Objects.requireNonNull(getSetupData("BaudRate:")));

        port.setBaudRate(baudRate);
        port.openPort();
        new SerialThread(port);
    }


    /**
     * This helper method, reads the data from the setup datasheet and returns the corresponding String to the
     * given data header line.
     *
     * @param dataHeader is a String which indicates, which line data is needed.
     * @return a String with the data of the called line.
     */
    private String getSetupData(String dataHeader) {
        try {
            List<String> setupSheet = Files.readAllLines(Paths.get(
                    Objects.requireNonNull(getClass().getResource("/data/setupData.txt")).toURI()));
            for (String s : setupSheet) {
                if (s.contains(dataHeader)) {
                    return s.substring(dataHeader.length(), s.indexOf('*'));
                }
            }
        } catch (URISyntaxException | IOException e) {
            System.out.println("There is an error with the file setupData.txt");
            e.getStackTrace();
        }
        return null;
    }
}
