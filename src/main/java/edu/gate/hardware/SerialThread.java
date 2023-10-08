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
    private ArrayList<String> MIS; /* Mission-Initialisation-Stamps */

    private ArrayList<ArrayList<Float>> TMP_perMission = new ArrayList<>(); /* Temperature */
    private ArrayList<ArrayList<Float>> CP1_perMission = new ArrayList<>(); /* Vibration */
    private ArrayList<ArrayList<Float>> CP2_perMission = new ArrayList<>(); /* Microphone-Volume */
    private ArrayList<ArrayList<Float>> CP3_perMission = new ArrayList<>(); /* Current-Port-1 */
    private ArrayList<ArrayList<Float>> VIB_perMission = new ArrayList<>(); /* Current-Port-2 */
    private ArrayList<ArrayList<Float>> MIC_perMission = new ArrayList<>(); /* Current-Port-3 */
    private ArrayList<ArrayList<Integer>> TSP_perMission = new ArrayList<>(); /* Time-Stamp */
    private SensIn SENS;
    private ArrayList<String> processInstructions;
    private ArrayList<Integer> processInstructionsTimeDelay;
    private boolean realStart = false;

    /**
     * byThread: This Constructor is for the by-Thread call with the main work of RECEIVING-DATA.
     *
     * @param port is the actual running port from the HardwareGate via main-Thread.
     */
    public SerialThread(SerialPort port) {
        this.port = port;
        this.TMP_perMission = new ArrayList<>();
        this.VIB_perMission = new ArrayList<>();
        this.MIC_perMission = new ArrayList<>();
        this.CP1_perMission = new ArrayList<>();
        this.CP2_perMission = new ArrayList<>();
        this.CP3_perMission = new ArrayList<>();
        this.TSP_perMission = new ArrayList<>();
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
                if (s > 0 && processInstructionsTimeDelay.get(s - 1) >= 5) {
                    try {
                        Thread.sleep((processInstructionsTimeDelay.get(s - 1) * 1000) - 100);
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
        perSecondTransformation();
        this.SENS = new SensIn(TMP_perMission, VIB_perMission, MIC_perMission, CP1_perMission,
                CP2_perMission, CP3_perMission, TSP_perMission, MIS);
    }

    /**
     * Helper method to analyse the received lines for its sensor values.
     *
     * @param dataLine is the received data line as a String.
     * @return a boolean to indicate if the received string contains the end signal.
     */
    private boolean analyseSerialData(String dataLine) {
        /* Safety for the real start of the sensor data. */
        if (!realStart && dataLine.contains("sti=")) {
            realStart = true;
        }

        String[] splitMission = dataLine.split("#");

        if (realStart) {
            for (String s : splitMission) {
                if (s.contains("?")) {
                    return true;
                } else if (s.contains("TMP")) {
                    String[] split = s.split(":");
                    TMP_perMission.get(MIS.size() - 1).add(Float.valueOf(split[1]));
                } else if (s.contains("VIB")) {
                    String[] split = s.split(":");
                    VIB_perMission.get(MIS.size() - 1).add(Float.valueOf(split[1]));
                } else if (s.contains("MIC")) {
                    String[] split = s.split(":");
                    MIC_perMission.get(MIS.size() - 1).add(Float.valueOf(split[1]));
                } else if (s.contains("CP1")) {
                    String[] split = s.split(":");
                    CP1_perMission.get(MIS.size() - 1).add(Float.valueOf(split[1]));
                } else if (s.contains("CP2")) {
                    String[] split = s.split(":");
                    CP2_perMission.get(MIS.size() - 1).add(Float.valueOf(split[1]));
                } else if (s.contains("CP3")) {
                    String[] split = s.split(":");
                    CP3_perMission.get(MIS.size() - 1).add(Float.valueOf(split[1]));
                } else if (s.contains("TSP")) {
                    String[] split = s.split(":");
                    TSP_perMission.get(MIS.size() - 1).add(Integer.parseInt(split[1]));
                } else if (s.contains("MIS")) {
                    /* MIS got a new syntax, which looks like: MIS:egs=1&tmd=5000&tsp=345999. */
                    String[] split = s.split(":");
                    MIS.add(split[1]);
                    TMP_perMission.add(new ArrayList<>());
                    VIB_perMission.add(new ArrayList<>());
                    MIC_perMission.add(new ArrayList<>());
                    CP1_perMission.add(new ArrayList<>());
                    CP2_perMission.add(new ArrayList<>());
                    CP3_perMission.add(new ArrayList<>());
                    TSP_perMission.add(new ArrayList<>());
                }
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

    /**
     * This method transforms the sensor data, which has between 1000 - 1 values per second, to a real per second
     * data set.
     */
    private void perSecondTransformation() {
        /* This loop iterates through each mission. */
        for (int mission = 0; mission < TSP_perMission.size(); mission++) {
            int amount = 0;
            int periodStart = TSP_perMission.get(mission).get(0);
            Float TMP = 0F, VIB = 0F, MIC = 0F, CP1 = 0F, CP2 = 0F, CP3 = 0F;
            ArrayList<Float> TMP_perMissionX = new ArrayList<>();
            ArrayList<Float> VIB_perMissionX = new ArrayList<>();
            ArrayList<Float> MIC_perMissionX = new ArrayList<>();
            ArrayList<Float> CP1_perMissionX = new ArrayList<>();
            ArrayList<Float> CP2_perMissionX = new ArrayList<>();
            ArrayList<Float> CP3_perMissionX = new ArrayList<>();

            /* This loop iterates through each value of the current mission. */
            for (int i = 0; i < TSP_perMission.get(mission).size(); i++) {
                int oneSecond = 1000 - (TSP_perMission.get(mission).get(i) - periodStart);

                /* If one second is over, or it is the last element, the median of its values is saved. */
                if (i == TSP_perMission.get(mission).size() - 1 || oneSecond < 0) {
                    TMP_perMissionX.add(TMP/amount);
                    VIB_perMissionX.add(VIB/amount);
                    MIC_perMissionX.add(MIC/amount);
                    CP1_perMissionX.add(CP1/amount);
                    CP2_perMissionX.add(CP2/amount);
                    CP3_perMissionX.add(CP3/amount);
                    amount = 0;
                    TMP =VIB = MIC = CP1 = CP2 = CP3 = 0F;
                    periodStart = TSP_perMission.get(mission).get(i);
                }

                TMP += TMP_perMission.get(mission).get(i);
                VIB += VIB_perMission.get(mission).get(i);
                MIC += MIC_perMission.get(mission).get(i);
                CP1 += CP1_perMission.get(mission).get(i);
                CP2 += CP2_perMission.get(mission).get(i);
                CP3 += CP3_perMission.get(mission).get(i);
                ++amount;
            }

            TMP_perMission.set(mission, TMP_perMissionX);
            VIB_perMission.set(mission, VIB_perMissionX);
            MIC_perMission.set(mission, MIC_perMissionX);
            CP1_perMission.set(mission, CP1_perMissionX);
            CP2_perMission.set(mission, CP2_perMissionX);
            CP3_perMission.set(mission, CP3_perMissionX);
        }
    }
}
