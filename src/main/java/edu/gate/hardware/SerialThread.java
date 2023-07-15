package edu.gate.hardware;

import com.fazecast.jSerialComm.SerialPort;
import edu.ground.datapreparation.Triad;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * In this class the thread handling is archived for the parallel sending and receiving of the Hardware data via Serial
 * communication.
 */
public class SerialThread extends Thread {
    private final SerialPort port;
    private final Triad processData;
    private final Short sensorInterval;
    private final ArrayList<Float> TMP;
    private final ArrayList<Float> VIB;
    private final ArrayList<Short> MIC;
    private final ArrayList<Long> CP1;
    private final ArrayList<Long> CP2;
    private final ArrayList<Long> CP3;
    private final ArrayList<Long> TSP;
    private SensIn SENS;
    private ArrayList<String> processInstructions;
    private ArrayList<Integer> processInstructionsTimeDelay;

    public SerialThread(SerialPort port, Triad processFlow, Short accuracyLevel) {
        this.port = port;
        this.processData = processFlow;
        this.sensorInterval = accuracyLevel;

        this.TMP = new ArrayList<>();
        this.VIB = new ArrayList<>();
        this.MIC = new ArrayList<>();
        this.CP1 = new ArrayList<>();
        this.CP2 = new ArrayList<>();
        this.CP3 = new ArrayList<>();
        this.TSP = new ArrayList<>();

        /* Self initialisation for the Thread. */
        SerialThread byThread = new SerialThread(port, processData, sensorInterval);

        byThread.start();
        receivingSerial();
    }

    /**
     * This method is for the Thread to run in parallel.
     */
    @Override
    public void run() {
        this.processInstructions = new ArrayList<>();
        this.processInstructionsTimeDelay = new ArrayList<>();
        prepareProcessInstructions();
        sendingSerial();
    }

    /**
     * Thread runner method: Does send the constructions to the arduino.
     * Sending Example: "<*RPM:3600#*TMD:5000#>" or at the end "<*RPM:3600#*TMD:5000#>?"
     */
    private void sendingSerial() {
        OutputStream outputStream = port.getOutputStream();
        for (int i = 0; i < processInstructions.size(); i++) {
            try {
                outputStream.write(processInstructions.get(i).getBytes());
                outputStream.flush();
            } catch (IOException e) {
                System.out.println("Error in the output stream of the serial communication:");
                e.getStackTrace();
            }
        }
    }

    private void prepareProcessInstructions() {
        /* Initializing the START of the Flow. */
        String latestID = null;
        for (int i = 0; i < processData.events().size(); i++) {
            if (processData.events().get(i).getType().equals("startEvent")) {
                processInstructions.add("<*EST:1#>");
                processInstructionsTimeDelay.add(0);
                latestID = processData.events().get(i).getId();
            }
        }

        /* Initializing the MIDDLE of the Flow. */
        /* Searching as long everything as there are flows left. Just a counter. */
        for (int counter = 0; counter < processData.flows().size(); counter++) {
            /* Searching literally in each flow if it does match with the latest ID.*/
            for (int i = 0; i < processData.flows().size(); i++) {
                /* We are just processing if we found the correct flow to our ID. */
                if (processData.flows().get(i).getSourceRef().equals(latestID)) {
                    boolean foundObject = false;
                    int searchCounter = 0;
                    /* Searching until the object is found or no element is left. */
                    while (!foundObject) {
                        if (searchCounter < processData.tasks().size() && processData.tasks().
                                get(searchCounter).getId().equals(processData.flows().get(i).getTargetRef())) {
                            /* Searching in all Tasks. */
                            processInstructions.add(processData.tasks().get(searchCounter).getOperationLine());
                            processInstructionsTimeDelay.add(processData.tasks().get(searchCounter).getTimeDelay());
                            foundObject = true;
                        } else if (searchCounter < processData.events().size() && processData.events().
                                get(searchCounter).getId().equals(processData.flows().get(i).getTargetRef())) {
                            /* Search in all Events. */
                            /* If it is in the event, it is the final end event. */
                            processInstructions.add("<*EST:0#>?");
                            processInstructionsTimeDelay.add(0);
                            foundObject = true;
                        } else if (searchCounter < processData.gateways().size() && processData.gateways().
                                get(searchCounter).getId().equals(processData.flows().get(i).getTargetRef())) {
                            /* Search in all Gateways. */
                            // TODO: At loop one.
                        }
                    }

                    latestID = processData.flows().get(i).getTargetRef();
                }
            }
        }
    }

    /**
     * Home runner method: Does receive the sensor data from the arduino.
     * Receiving Example: "TSP:27.34*TMP:654000" or at the end "TSP:27.34*TMP:654000*?".
     */
    private void receivingSerial() {
        StringBuilder dataBuilder = new StringBuilder();
        /* Defining an end signal will help us to deal with phases of no data transmission. */
        boolean receivedEndSignal = false;

        while (!receivedEndSignal) {
            if (port.bytesAvailable() > 0) { // Check if there are bytes available
                byte[] buffer = new byte[port.bytesAvailable()];
                int numRead = port.readBytes(buffer, buffer.length);
                if (numRead > 0) {
                    String data = new String(buffer);
                    dataBuilder.append(data);
                    int packetEndIndex = dataBuilder.indexOf("\n");
                    if (packetEndIndex != -1) {
                        String packet = dataBuilder.substring(0, packetEndIndex);
                        System.out.println(packet); //TODO: for testing.
                        receivedEndSignal = analyseSerialData(packet);
                        dataBuilder.delete(0, packetEndIndex + 1);
                    }
                }
            }
        }
        this.SENS = new SensIn(TMP, VIB, MIC, CP1, CP2, CP3, TSP);
    }

    /**
     * Helper method to analyse the received lines for its sensor values.
     *
     * @param dataLine is the received data line as a String.
     * @return a boolean to indicate if the received string contains the end signal.
     */
    private boolean analyseSerialData(String dataLine) {
        String[] splitMission = dataLine.split("/*");

        for (String s : splitMission) {
            if (s.equals("?")) {
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
            }
        }
        return false;
    }

    /**
     * Getter for the gathered sensor data for further analyses.
     *
     * @return a SensIn object, which is a collection of all the sensor data.
     */
    public SensIn getSENS() {
        return SENS;
    }
}
