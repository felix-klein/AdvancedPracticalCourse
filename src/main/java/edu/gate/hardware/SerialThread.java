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
    private int loopCount;
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
    private String latestLoopBranch = null; /* ID to now which way NOT to take for the further process. */

    public SerialThread(SerialPort port, Triad processFlow, Short accuracyLevel, int loopCount) {
        this.port = port;
        this.processData = processFlow;
        this.sensorInterval = accuracyLevel;
        this.loopCount = loopCount;

        this.TMP = new ArrayList<>();
        this.VIB = new ArrayList<>();
        this.MIC = new ArrayList<>();
        this.CP1 = new ArrayList<>();
        this.CP2 = new ArrayList<>();
        this.CP3 = new ArrayList<>();
        this.TSP = new ArrayList<>();

        /* Self initialisation for the Thread. */
        SerialThread byThread = new SerialThread(port, processData, sensorInterval, loopCount);

        try {
            byThread.start();
            receivingSerial();
            byThread.join();
        } catch (InterruptedException e) {
            System.out.println("An Thread error occurred, especially thrown by the join() operator!");
            e.getStackTrace();
        }

    }

    /**
     * This method is for the Thread to run in parallel.
     */
    @Override
    public void run() {
        this.processInstructions = new ArrayList<>();
        this.processInstructionsTimeDelay = new ArrayList<>();
        /* Define the sensor Interval as the first instance in the instructions list. */
        processInstructions.add("<STI:" + sensorInterval);
        processInstructionsTimeDelay.add(0);

        prepareProcessInstructions();
        sendingSerial();
    }

    /**
     * Thread runner method: Does send the constructions to the arduino.
     * Sending Example: "<*RPM:3600#*TMD:5000#>" or at the end "<*RPM:3600#*TMD:5000#>?"
     */
    private void sendingSerial() {
        OutputStream outputStream = port.getOutputStream();
        for (int s = 0; s < processInstructions.size(); s++) {
            try {
                /* Build the time delay for the serial sending, to reduce the data overhead on the hardware. The
                * sending of all the data at once would result in a possible data loss because of the rather small
                * memory on the arduino board. */
                if (s > 0 && processInstructionsTimeDelay.get(s-1) > 100) {
                    try {
                        Thread.sleep(processInstructionsTimeDelay.get(s-1) - 100);
                    } catch (InterruptedException e) {
                        System.out.println("The Thread sleep operation doesn't work as predicted!");
                        e.getStackTrace();
                    }
                }
                outputStream.write(processInstructions.get(s).getBytes());
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
                /* For gateways which are loop exits, two flow objects do exist. If this element is null or older, the loop
                 * was already detected and processed, therefor do we need to exclude the search of the flow which traces
                 * back into the loop.*/

                /* We are just processing if we found the correct flow to our ID. */
                if (processData.flows().get(i).getSourceRef().equals(latestID) &&
                        !processData.flows().get(i).getTargetRef().equals(latestLoopBranch)) {
                    boolean foundObject = false;
                    /* In this case the entrance is the first X1 and the exit is the X2. The tasks in between X1 and X2
                     * which are on the main road are passed loopCounter+1 times, so practically the loop starts at X2
                     * and ends in X2. */
                    boolean loopEntrancePassed = false; /* X1:Save the id for further usage and as boolean replacement. */

                    int searchCounter = 0;
                    /* Searching until the object is found or no element is left. */
                    while (!foundObject) {
                        /* Initialisation for easier readability. */
                        String taskID = processData.tasks().get(searchCounter).getId();
                        String eventID = processData.events().get(searchCounter).getId();
                        String gatewayID = processData.gateways().get(searchCounter).getId();
                        String targetID = processData.flows().get(i).getTargetRef();
                        /* The if else statements to find the match. */
                        if (searchCounter < processData.tasks().size() && taskID.equals(targetID)) {
                            /* Searching in all Tasks. */
                            processInstructions.add(processData.tasks().get(searchCounter).getOperationLine());
                            processInstructionsTimeDelay.add(processData.tasks().get(searchCounter).getTimeDelay());
                            foundObject = true;
                        } else if (searchCounter < processData.events().size() && eventID.equals(targetID)) {
                            /* Search in all Events. */
                            /* If it is in the event, it is the final end event. */
                            processInstructions.add("<*EST:0#>?");
                            processInstructionsTimeDelay.add(0);
                            foundObject = true;
                        } else if (searchCounter < processData.gateways().size() && gatewayID.equals(targetID)) {
                            /* Search in all Gateways. */
                            /* The idea is to reverse engineer the loop until the entrance Gateway. */
                            if (!loopEntrancePassed) {
                                loopEntrancePassed = true;
                            } else {
                                loopEntrancePassed = false;
                                /* Find and initialise the loop via reverse engineering. */
                                counter += reverseLoopTrace(processData.flows().get(i).getSourceRef(),
                                        processData.gateways().get(searchCounter).getId());
                            }
                            foundObject = true;
                        }
                    }
                    latestID = processData.flows().get(i).getTargetRef();
                }
            }
        }
    }

    private int reverseLoopTrace(String falseBranchID, String exitID) {
        ArrayList<String> currentLoop = new ArrayList<>();
        ArrayList<Integer> currentLoopTimeDelay = new ArrayList<>();
        /* The counter got increased only if the main trace (everything outside the main trace between the two gateways)
         * have been passed, because those are the new flows to take into account.*/
        int counterIncrease = 0;
        boolean passingEntrance = false;
        boolean loopCompleted = false;
        String latestID = exitID;
        /* Loop as long as the process loop is not completed. */
        while (!loopCompleted) {
            /* Loop through all flow objects to find the correct one. */
            for (int flow = 0; flow < processData.flows().size() && !loopCompleted; flow++) {
                /* If statement which is true if the right flow has been found. */
                if (processData.flows().get(flow).getTargetRef().equals(latestID)
                        && !processData.flows().get(flow).getSourceRef().equals(falseBranchID)) {
                    String currentID = processData.flows().get(flow).getSourceRef(); /* ID to search for */
                    boolean match = false; /* Indicator if the corresponding ID has been found. */
                    for (int o = 0; !match &&
                            o < Math.max(processData.tasks().size(), processData.gateways().size()) &&
                            !loopCompleted;
                         o++) { /* (o) stands for object */
                        /* Search in the current objects if they are a match. */
                        String taskID = processData.tasks().get(o).getId();
                        String gatewayID = processData.events().get(o).getId();
                        /* Compare for a match. */
                        if (o < processData.tasks().size() && taskID.equals(currentID)) {
                            currentLoop.add(processData.tasks().get(o).getOperationLine());
                            currentLoopTimeDelay.add(processData.tasks().get(o).getTimeDelay());
                        } else if (o < processData.gateways().size() && gatewayID.equals(currentID)) {
                            if (taskID.equals(exitID)) {
                                latestLoopBranch = processData.flows().get(flow).getTargetRef();
                                loopCompleted = true;
                            } else {
                                passingEntrance = true;
                            }
                        }
                    }
                    latestID = currentID;
                    counterIncrease += (passingEntrance) ? 1 : 0;
                }
            }
        }
        initializeLoop(currentLoop, currentLoopTimeDelay);
        return counterIncrease;
    }

    /**
     * Helper method to initialise the loop results into the process instructions list.
     *
     * @param loop          is a list of all tasks of the loop.
     * @param loopTimeDelay is a list including all corresponding time delay values.
     */
    private void initializeLoop(ArrayList<String> loop, ArrayList<Integer> loopTimeDelay) {
        loopCount = (loopCount <= 0) ? 1 : loopCount;
        for (int counter = 0; counter < loopCount; counter++) {
            for (int i = loop.size()-1; i >= 0; i--) {
                processInstructions.add(loop.get(i));
                processInstructionsTimeDelay.add(loopTimeDelay.get(i));
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
                        System.out.println(packet);
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
