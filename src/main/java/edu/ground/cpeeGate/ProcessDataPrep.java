package edu.ground.cpeeGate;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProcessDataPrep {
    private final ArrayList<String> preparedData;

    /**
     * The Class ProcessDataPrep is the real data translate between the ruff xml string and the final List of output
     * Strings for the Hardware.
     *
     * @param xmlResponse is the single String which includes the xml response in its raw nature.
     * @param accuracyLevel is an additional parameter from the user interface for the accuracy of the sensor data.
     */
    public ProcessDataPrep(String xmlResponse, String accuracyLevel) {
        /* First, purify the raw xml data back into a list of Strings, and filter the unnecessary beginning. */
        ArrayList<String> purifiedData = responsePurification(xmlResponse);
        /* Translate the verbal accuracy level into a number. */
        Short accuracyInterval = accuracyAdaption(accuracyLevel);
        /* Last, translate the purified data into the real output for the Hardware. */
        this.preparedData = new ArrayList<>();
        dataModeling(purifiedData, accuracyInterval);
    }

    /**
     * Main data Modeler does just initialise the beginning and the end, with the result, that the common line
     * operations could work in a recursive manner.
     *
     * @param purifiedData is the raw prepared data.
     * @param accInterval is the "accuracy interval", which describes the Sensor accuracy.
     */
    private void dataModeling(ArrayList<String> purifiedData, Short accInterval) {
        this.preparedData.add("<*TMD:5#>");
        this.preparedData.add("<*STI:" + accInterval + "#*TMD:300#>");
        this.preparedData.addAll(recursiveDataBuilder(purifiedData));
        this.preparedData.set(preparedData.size() - 1, preparedData.get(preparedData.size() - 1) + "?");
    }

    /**
     * This helper method with the main operations is separate to enable the use of recursive behavior in order to
     * reduce code duplication in the case of loops (and loops in loops). The method will run through each line and
     * indicate if a Task or Gateway is starting and will investigate its operations.
     *
     * @param purifiedData is the prepared data which includes lines and just the ones with important data.
     * @return a ArrayList of Strings which are the complete lines for the Hardware.
     */
    private ArrayList<String> recursiveDataBuilder(ArrayList<String> purifiedData) {
        ArrayList<String> currentInLines = new ArrayList<>();

        /* Loop to investigate each line of the xml or each line of this loop section. */
        for (int i = 0; i < purifiedData.size(); i++) {
            if (purifiedData.get(i).contains("endpoint=\"motor_start\"")) {
                /* Investigation of a Motor Start. */
                currentInLines.add("<*EST:1#>");
                i = i + 5;
            } else if (purifiedData.get(i).contains("endpoint=\"motor_stop\"")) {
                /* Investigation of a Motor Stop. */
                currentInLines.add("<*EST:0#>");
                i = i + 5;
            } else if (purifiedData.get(i).contains("<stop id=")) {
                /* Investigation of an Emergency Stop. */
                currentInLines.add("<*EMS:1#>");
                break; /* An Emergency Stop will end the whole process immediately if it is introduced */
            } else if (purifiedData.get(i).contains("endpoint=\"motor_hull_sensor\"")) {
                /* Investigation of a Motor Hull Sensor. */
                /* HDA Part -> The Hall Degree Angle of the motor. */
                int degree = patternMatcher("<degree>(\\d+)</degree>", purifiedData.get(i+4));
                /* TMD Part -> The Time Duration of the action. */
                int duration = patternMatcher("<duration>(\\d+)</duration>", purifiedData.get(i+5));
                i += 8;
                currentInLines.add("<*HDA:" + (degree == -1 ? "30" : degree) + "#"
                        + "*TMD:" + (duration == -1 ? "120" : duration) + "#>");
            } else if (purifiedData.get(i).contains("endpoint=\"motor_gear\"")) {
                /* Investigation of a Motor Gear. */
                /* RPM Part -> The Rotations Per Minutes of the motor.*/
                int level = patternMatcher("<level>(\\d+)</level>", purifiedData.get(i+4));
                /* TMD Part -> The Time Duration of the action. */
                int duration = patternMatcher("<duration>(\\d+)</duration>", purifiedData.get(i+5));
                i += 8;
                currentInLines.add("<*RPM:" + (level == -1 ? "500" : level) + "#"
                        + "*TMD:" + (duration == -1 ? "120" : duration) + "#>");
            } else if (purifiedData.get(i).contains("<loop ")) {
                /* Investigation of a Loop. */
                int loopCount = patternMatcher("<loop times=\"(\\d+)\">", purifiedData.get(i));
                int loopEnd = loopHandler(i, purifiedData);
                ArrayList<String> recursiveSubList = new ArrayList<>(purifiedData.subList(i+1, loopEnd));
                /* Recursive call of this method with a sub list including the loop lines. */
                ArrayList<String> loopInLines = recursiveDataBuilder(recursiveSubList);
                for (int loops = 0; loops < loopCount; loops++) {
                    currentInLines.addAll(loopInLines);
                }
                i = loopEnd;
            }
        }
        return currentInLines;
    }

    /**
     * Helper method to find the end index of the current loop.
     *
     * @param currentIndex is the start of the loop.
     * @param purifiedData is the whole purified data list.
     * @return an int which is the end of the current loop.
     */
    private int loopHandler(int currentIndex, ArrayList<String> purifiedData) {
        /* Counter to filter out crossing loops inside of this loop. */
        int loopsOpen = 0;
        for (int i = currentIndex + 1; i < purifiedData.size(); i++) {
            if (purifiedData.get(i).startsWith("<loop times=")) {
                loopsOpen++;
            } else if (loopsOpen > 0 && purifiedData.get(i).contains("</loop>")) {
                loopsOpen--;
            } else if (loopsOpen == 0 && purifiedData.get(i).contains("</loop>")) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Helper for recursiveDataBuilder to find the data in String sections which do have a specific pattern.
     *
     * @param regex is the pattern.
     * @param searchArea is the area in which this pattern could be.
     * @return the value in the pattern or a -1 for no pattern which results in a default value.
     */
    private int patternMatcher(String regex, String searchArea) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchArea);
        return matcher.find() ?  Integer.parseInt(matcher.group(1)) :  -1;
    }

    /**
     * This helper method does trim the lines of the xmlResponse by white space and only for those lines which are
     * helpful and needed for further investigation.
     *
     * @param xmlResponse is the raw xml data from the modeller in form of a single string.
     * @return an ArrayList<String> which includes the important part of the xml file without white space.
     */
    private ArrayList<String> responsePurification(String xmlResponse) {
        /* Split the String into an array by its line brakes. */
        String[] lineBrakes = xmlResponse.split("\\r?\\n|\\r");

        /* Trim each line by removing unnecessary lines and white space. */
        ArrayList<String> lineTrim = new ArrayList<>();
        boolean cutStart = false;
        /* The for loop is just copying (after trimming) the lines which are important for further preparation. */
        for (String lineBrake : lineBrakes) {
            if (lineBrake.contains("</dslx>")) {
                cutStart = true;
            } else if (cutStart && lineBrake.contains("</description>")) {
                return lineTrim;
            } else if (cutStart && !lineBrake.contains("<description")) {
                lineTrim.add(lineBrake.trim());
            }
        }
        return lineTrim;
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

    /**
     * Getter to return the prepared data for the Hardware.
     *
     * @return a ArrayList with each line as a command.
     */
    public ArrayList<String> getPreparedData() {
        return preparedData;
    }
}
