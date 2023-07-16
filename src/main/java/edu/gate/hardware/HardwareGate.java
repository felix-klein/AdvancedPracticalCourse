package edu.gate.hardware;

import com.fazecast.jSerialComm.SerialPort;
import edu.ground.datapreparation.Triad;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

/**
 * This class is the communication gate between this program and the board, as well as the transfer area to the
 * Terminal.
 * The result is a dual function as transmitter and outside gate.
 */
public class HardwareGate {

    public HardwareGate(Triad processFlow, String accuracyLevel, int loopCount) {
        SerialPort port = SerialPort.getCommPort(Objects.requireNonNull(getSetupData("Port:")));
        int baudRate = Integer.parseInt(Objects.requireNonNull(getSetupData("BaudRate:")));

        port.setBaudRate(baudRate);
        port.openPort();
        new SerialThread(port, processFlow, accuracyAdaption(accuracyLevel), loopCount);
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

    /**
     * This helper method, reads the accuracy on a verbal level and transfers it into milliseconds for the sensor
     * data gathering.
     *
     * @param accuracyLevel in a verbal String level.
     * @return a short which is the number of milliseconds in which the sensors should gather data.
     */
    private short accuracyAdaption(String accuracyLevel) {
        return switch (accuracyLevel) {
            case "extreme" -> 9; /* Every 10 milliseconds (10) */
            case "intense" -> 99; /* Every 0.1 second (100) */
            case "high" -> 249; /* Every 0.25 second (250) */
            case "1-for-1" -> 999; /* Every 1 second (1000) */
            default -> 499; /* "basic" is the default, with every 1/2 second (500) */
        };
    }
}
