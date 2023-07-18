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
    private Triad processData;
    private Short sensorInterval;
    private int loopCount;
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
    private String latestLoopBranch = null; /* ID to now which way NOT to take for the further process. */

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
     * @param processFlow   is the process data to send.
     * @param accuracyLevel is the number of sensor interval.
     * @param loopCount     is the number of loop runs.
     */
    public SerialThread(SerialPort port, Triad processFlow, Short accuracyLevel, int loopCount) {
        this.port = port;
        this.processData = processFlow;
        this.sensorInterval = accuracyLevel;
        this.loopCount = loopCount;

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
        this.processInstructions = new ArrayList<>();
        this.processInstructionsTimeDelay = new ArrayList<>();
        /* Define the sensor Interval as the first instance in the instructions list. */
        processInstructions.add("<*STI:" + sensorInterval + "#>");
        processInstructionsTimeDelay.add(0);

        prepareProcessInstructions();
        sendingSerial();
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
                e.getStackTrace();
            }
        }
    }

    /**
     * This method orders the instructions into their correct order and builds the strings which will be sent to the
     * board in its correct formatting.
     */
    private void prepareProcessInstructions() {
        /* The loop entrance have to be marked to know where the loop trace have to go.
         * In this case the entrance is the first X1 and the exit is the X2. The tasks in between X1 and X2
         * which are on the main road are passed loopCounter+1 times, so practically the loop starts at X2
         * and ends in X2. */
        boolean loopEntrancePassed = false; /* X1:Save the id for further usage and as boolean replacement. */
        /* The false ID is the source of the flow which calls the entrance gateway the first time. */
        String falseBranchID = null;

        /* Initializing the START of the Flow. */
        String latestID = null;
        for (int i = 0; i < processData.events().size(); i++) {
            if (processData.events().get(i).getType().equals("start")) {
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
                    int searchCounter = 0;
                    /* Searching until the object is found or no element is left. */
                    while (!foundObject) {
                        /* Initialisation for easier readability. */
                        String targetID = processData.flows().get(i).getTargetRef();
                        /* The if else statements to find the match. */
                        if (searchCounter < processData.tasks().size()
                                && processData.tasks().get(searchCounter).getId().equals(targetID)) {
                            /* Searching in all Tasks. */
                            processInstructions.add(processData.tasks().get(searchCounter).getOperationLine());
                            processInstructionsTimeDelay.add(processData.tasks().get(searchCounter).getTimeDelay());
                            foundObject = true;
                        } else if (searchCounter < processData.events().size()
                                && processData.events().get(searchCounter).getId().equals(targetID)) {
                            /* Search in all Events. */
                            /* If it is in the event, it is the final end event. */
                            processInstructions.add("<*EST:0#>?");
                            processInstructionsTimeDelay.add(0);
                            foundObject = true;
                        } else if (searchCounter < processData.gateways().size()
                                && processData.gateways().get(searchCounter).getId().equals(targetID)) {
                            /* Search in all Gateways. */
                            if (loopEntrancePassed) {
                                loopEntrancePassed = false;
                                /* Find and initialise the loop via reverse engineering. */
                                counter += reverseLoopTrace(falseBranchID,
                                        processData.gateways().get(searchCounter).getId());
                            } else {
                                falseBranchID = processData.flows().get(i).getSourceRef();
                                loopEntrancePassed = true;
                            }
                            foundObject = true;
                        }
                        searchCounter++;
                    }
                    latestID = processData.flows().get(i).getTargetRef();
                }
            }
        }
    }

    /**
     * This helper method handles loops by tracing them via reverse engineering. Helper for prepareProcessInstructions.
     *
     * @param falseBranchID is the ID of the last element before the entrance gateway.
     * @param exitID        is the ID of the exit gateway, where the tracing starts and ends.
     * @return an int with the number of flows passed in the loop, except the ones of the main trace.
     */
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
                    latestID = processData.flows().get(flow).getSourceRef(); /* ID to search for */
                    boolean match = false; /* Indicator if the corresponding ID has been found. */
                    for (int o = 0; !match /* (o) stands for object */
                            && o < Math.max(processData.tasks().size(), processData.gateways().size()); o++) {
                        /* Compare for a match. */
                        if (o < processData.tasks().size()
                                && processData.tasks().get(o).getId().equals(latestID)) {
                            currentLoop.add(processData.tasks().get(o).getOperationLine());
                            currentLoopTimeDelay.add(processData.tasks().get(o).getTimeDelay());
                            match = true;
                        } else if (o < processData.gateways().size()
                                && processData.gateways().get(o).getId().equals(latestID)) {
                            if (processData.gateways().get(o).getId().equals(exitID)) {
                                latestLoopBranch = processData.flows().get(flow).getTargetRef();
                                loopCompleted = true;
                            } else {
                                passingEntrance = true;
                            }
                            match = true;
                        }
                    }
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
            for (int i = loop.size() - 1; i >= 0; i--) {
                processInstructions.add(loop.get(i));
                processInstructionsTimeDelay.add(loopTimeDelay.get(i));
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
