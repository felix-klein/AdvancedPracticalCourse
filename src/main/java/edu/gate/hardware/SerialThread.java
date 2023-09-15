package edu.gate.hardware;

import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * In this class the thread handling is archived for the parallel sending and receiving of the Hardware data via Serial
 * communication.
 */
public class SerialThread extends Thread {
    private final SerialPort port;
    private ArrayList<Float> TMP; /* Temperature */
    private ArrayList<Float> VIB; /* Vibration */
    private ArrayList<Short> MIC; /* Microphone-Volume */
    private ArrayList<Long> CP1; /* Current-Port-1 */
    private ArrayList<Long> CP2; /* Current-Port-2 */
    private ArrayList<Long> CP3; /* Current-Port-3 */
    private ArrayList<Long> TSP; /* Time-Stamp */
    private ArrayList<String> MIS; /* Mission-Initialisation-Stamps */
    private SensIn SENS;
    private ArrayList<String> processInstructions;
    private ArrayList<Integer> processInstructionsTimeDelay;

    /**
     * byThread: This Constructor is for the by-Thread call with the main work of RECEIVING-DATA.
     *
     * @param port is the actual running port from the HardwareGate via main-Thread.
     */
    public SerialThread(SerialPort port) {
        this.port = port;
        this.TMP = new ArrayList<>();
        this.VIB = new ArrayList<>();
        this.MIC = new ArrayList<>();
        this.CP1 = new ArrayList<>();
        this.CP2 = new ArrayList<>();
        this.CP3 = new ArrayList<>();
        this.TSP = new ArrayList<>();
        this.MIS = new ArrayList<>();
    }

    /**
     * mainThread: This Constructor is for the main-Thread (the current) call with the main work of SENDING-DATA.
     *
     * @param port          is the actual running port from the HardwareGate.
     * @param preparedData  is the final data for the hardware from the process model.
     */
    public SerialThread(SerialPort port, ArrayList<String> preparedData) {
        this.port = port;
        this.processInstructions = preparedData;

        /* Self initialisation for the Thread. */
        SerialThread byThread = new SerialThread(port);

        try {
            byThread.start();
            runMain();
            byThread.join();
            this.SENS = byThread.getSENS();
        } catch (InterruptedException e) {
            System.out.println("An Thread error occurred, especially thrown by the join() operator!");
            e.getStackTrace();
        }
        port.closePort();
    }

    /**
     * This method is the like the run method for the byThread, just for the main trace/thread.
     */
    public void runMain() {
        this.processInstructionsTimeDelay = new ArrayList<>();

        prepareProcessInstructionsTimeDelay();
        sendingSerial();
    }

    /**
     * Helper method for the generation of a time delay object for the step by step sending of the instructions to the
     * Hardware.
     */
    private void prepareProcessInstructionsTimeDelay() {
        /* Searching in each line if there is a time delay (by its pattern) and copying it or creating it in a parallel
        objects with a one for one mapping.
         */
        for (int i = 0; i < processInstructions.size(); i++) {
            Pattern pattern = Pattern.compile("TMD:(\\d+)#>");
            Matcher matcher = pattern.matcher(processInstructions.get(i));
            int delay = 0;
            if (matcher.find()) {
                delay = Integer.parseInt(matcher.group(1));
            }
            processInstructionsTimeDelay.add(i, delay);
        }
    }

    /**
     * Main-Thread runner method: Does send the constructions to the arduino.
     * Sending Example: "<*RPM:3600#*TMD:5000#>" or at the end "<*RPM:3600#*TMD:5000#>?"
     */
    @SuppressWarnings("BusyWait")
    private void sendingSerial() {
        OutputStream outputStream = port.getOutputStream();
        for (int s = 0; s < processInstructions.size(); s++) {
            try {
                /* Build the time delay for the serial sending, to reduce the data overhead on the hardware. The
                 * sending of all the data at once would result in a possible data loss because of the rather small
                 * memory on the arduino board. */
                if (s > 0 && processInstructionsTimeDelay.get(s - 1) > 50) {
                    try {
                        Thread.sleep(processInstructionsTimeDelay.get(s - 1) - 50);
                    } catch (InterruptedException e) {
                        System.out.println("The Thread sleep operation doesn't work as predicted!");
                        e.getStackTrace();
                    }
                }
                outputStream.write(processInstructions.get(s).getBytes());
                outputStream.flush();
            } catch (IOException e) {
                System.out.println("Error in the output stream of the serial communication:");
                e.getCause();
                break;
            }
        }
    }

    /**
     * Thread runner method: Does receive the sensor data from the arduino and initializing SENS.
     * Receiving Example: "TSP:27.34*TMP:654000" or at the end "TSP:27.34*TMP:654000*?".
     */
    @Override
    public void run() {
        StringBuilder dataBuilder = new StringBuilder();
        /* Defining an end signal will help us to deal with phases of no data transmission. */
        boolean receivedEndSignal = false;

        while (!receivedEndSignal) {
            if (port.bytesAvailable() > 0) {
                byte[] buffer = new byte[port.bytesAvailable()];
                int numRead = port.readBytes(buffer, buffer.length);
                if (numRead > 0) {
                    String data = new String(buffer);
                    dataBuilder.append(data);
                    int packetEndIndex = dataBuilder.indexOf("\n");
                    if (packetEndIndex != -1) {
                        String packet = dataBuilder.substring(0, packetEndIndex);
                        receivedEndSignal = analyseSerialData(packet);
                        dataBuilder.delete(0, packetEndIndex + 1);
                    }
                }
            }
        }
        this.SENS = new SensIn(TMP, VIB, MIC, CP1, CP2, CP3, TSP, MIS);
    }

    /**
     * Helper method to analyse the received lines for its sensor values.
     *
     * @param dataLine is the received data line as a String.
     * @return a boolean to indicate if the received string contains the end signal.
     */
    private boolean analyseSerialData(String dataLine) {
        String[] splitMission = dataLine.split("#");

        for (String s : splitMission) {
            if (s.contains("?")) {
                return true;
            } else if (s.contains("TMP")) {
                String[] split = s.split(":");
                TMP.add(Float.valueOf(split[1]));
            } else if (s.contains("VIB")) {
                String[] split = s.split(":");
                VIB.add(Float.valueOf(split[1]));
            } else if (s.contains("MIC")) {
                String[] split = s.split(":");
                MIC.add(Short.valueOf(split[1]));
            } else if (s.contains("CP1")) {
                String[] split = s.split(":");
                CP1.add(Long.valueOf(split[1]));
            } else if (s.contains("CP2")) {
                String[] split = s.split(":");
                CP2.add(Long.valueOf(split[1]));
            } else if (s.contains("CP3")) {
                String[] split = s.split(":");
                CP3.add(Long.valueOf(split[1]));
            } else if (s.contains("TSP")) {
                String[] split = s.split(":");
                TSP.add(Long.valueOf(split[1]));
            } else if (s.contains("MIS")) {
                /* MIS got a new syntax, which looks like: MIS:egs=1&tmd=5000&tsp=345999. */
                String[] split = s.split(":");
                MIS.add(split[1]);
            }
        }
        return false;
    }

    /**
     * Getter for the gathered sensor data for further analyses. Can be picked up by the main tread.
     *
     * @return a SensIn object, which is a collection of all the sensor data.
     */
    public SensIn getSENS() {
        return SENS;
    }
}
