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
     * @param accInterval is the "accuracy Interval", which describes the Sensor accuracy.
     */
    private void dataModeling(ArrayList<String> purifiedData, Short accInterval) {
        this.preparedData.add("<*STI:" + accInterval + "#>");
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
                i = i + 6;
            } else if (purifiedData.get(i).contains("endpoint=\"motor_stop\"")) {
                /* Investigation of a Motor Stop. */
                currentInLines.add("<*EST:0#>");
                i = i + 6;
            } else if (purifiedData.get(i).contains("endpoint=\"motor_hull_sensor\"")) {
                /* Investigation of a Motor Hull Sensor. */
                /* HDA Part -> The Hall Degree Angle of the motor.*/
                String fullLine = "<*HDA:";
                int degree = patternMatcher("<degree>(\\d+)</degree>", purifiedData.get(i+4));
                fullLine = fullLine + (degree == -1 ? fullLine + "30#" : fullLine + degree + "#");
                /* TMD Part -> The Time Duration of the action. */
                int duration = patternMatcher("<duration>(\\d+)</duration>", purifiedData.get(i+5));
                fullLine = fullLine + "*TMD:" + (duration == -1 ? fullLine + "0#>" : fullLine + duration + "#>");
                i = i + 8;
                currentInLines.add(fullLine);
            } else if (purifiedData.get(i).contains("endpoint=\"motor_gear\"")) {
                /* Investigation of a Motor Gear. */
                /* RPM Part -> The Rotations Per Minutes of the motor.*/
                String fullLine = "<*RPM:";
                int level = patternMatcher("<level>(\\d+)</level>", purifiedData.get(i+4));
                fullLine = fullLine + (level == -1 ? fullLine + "500#" : fullLine + level + "#");
                /* TMD Part -> The Time Duration of the action. */
                int duration = patternMatcher("<duration>(\\d+)</duration>", purifiedData.get(i+5));
                fullLine = fullLine + "*TMD:" + (duration == -1 ? fullLine + "0#>" : fullLine + duration + "#>");
                i = i + 8;
                currentInLines.add(fullLine);
            } else if (purifiedData.get(i).contains("<loop ")) {
                /* Investigation of a Loop. */
                /* How many loop runs does this loop do. */
                int loopCount = patternMatcher("<_probability>(\\d+)</_probability>", purifiedData.get(i+2));
                /* ArrayList to save the commands of this loop. */
                ArrayList<String> recursivePart = new ArrayList<>();
                /* Part to find the last index of this loop. */
                int loopEnd = purifiedData.size() - 1;
                while (loopEnd >= 0 && recursivePart.isEmpty()) {
                    if (purifiedData.get(loopEnd).contains("</loop>")) {
                        recursivePart = (ArrayList<String>) purifiedData.subList(i+1, loopEnd);
                    }
                    loopEnd--;
                }
                /* Recursive call of this method to investigate the lines of this loop sections at its own. */
                ArrayList<String> loopInLines = recursiveDataBuilder(recursivePart);
                while (loopCount > 0) {
                    currentInLines.addAll(loopInLines);
                    loopCount--;
                }
                i = loopEnd + 1; /* Continue at the end of the loop */
            }
        }
        return currentInLines;
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
