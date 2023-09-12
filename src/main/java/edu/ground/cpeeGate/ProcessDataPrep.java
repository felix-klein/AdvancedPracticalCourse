package edu.ground.cpeeGate;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProcessDataPrep {
    private final ArrayList<String> preparedData;
    public ProcessDataPrep(String xmlResponse, String accuracyLevel) {
        ArrayList<String> purifiedData = responsePurification(xmlResponse);
        Short accuracyInterval = accuracyAdaption(accuracyLevel);
        this.preparedData = new ArrayList<>();
        dataModeling(purifiedData, accuracyInterval);
    }

    private void dataModeling(ArrayList<String> purifiedData, Short accInterval) {
        this.preparedData.add("<*STI:" + accInterval + "#>");
        this.preparedData.addAll(recursiveDataBuilder(purifiedData));
        this.preparedData.set(preparedData.size() - 1, preparedData.get(preparedData.size() - 1) + "?");
    }

    private ArrayList<String> recursiveDataBuilder(ArrayList<String> purifiedData) {
        ArrayList<String> currentInLines = new ArrayList<>();
        
        for (int i = 0; i < purifiedData.size(); i++) {
            if (purifiedData.get(i).contains("endpoint=\"motor_start\"")) {
                currentInLines.add("<*EST:1#>");
                i = i + 6;
            } else if (purifiedData.get(i).contains("endpoint=\"motor_stop\"")) {
                currentInLines.add("<*EST:0#>");
                i = i + 6;
            } else if (purifiedData.get(i).contains("endpoint=\"motor_hull_sensor\"")) {
                /* HDA Part. */
                String fullLine = "<*HDA:";
                int degree = patternMatcher("<degree>(\\d+)</degree>", purifiedData.get(i+4));
                fullLine = fullLine + (degree == -1 ? fullLine + "30#" : fullLine + degree + "#");
                /* TMD Part. */
                int duration = patternMatcher("<duration>(\\d+)</duration>", purifiedData.get(i+5));
                fullLine = fullLine + "*TMD:" + (duration == -1 ? fullLine + "0#>" : fullLine + duration + "#>");
                i = i + 8;
                currentInLines.add(fullLine);
            } else if (purifiedData.get(i).contains("endpoint=\"motor_gear\"")) {
                /* RPM Part */
                String fullLine = "<*RPM:";
                int level = patternMatcher("<level>(\\d+)</level>", purifiedData.get(i+4));
                fullLine = fullLine + (level == -1 ? fullLine + "500#" : fullLine + level + "#");
                /* TMD Part. */
                int duration = patternMatcher("<duration>(\\d+)</duration>", purifiedData.get(i+5));
                fullLine = fullLine + "*TMD:" + (duration == -1 ? fullLine + "0#>" : fullLine + duration + "#>");
                i = i + 8;
                currentInLines.add(fullLine);
            } else if (purifiedData.get(i).contains("<loop ")) {
                int loopCount = patternMatcher("<_probability>(\\d+)</_probability>", purifiedData.get(i+2));
                ArrayList<String> recursivePart = new ArrayList<>();
                int loopEnd = purifiedData.size() - 1;
                while (loopEnd >= 0 && recursivePart.isEmpty()) {
                    if (purifiedData.get(loopEnd).contains("</loop>")) {
                        recursivePart = (ArrayList<String>) purifiedData.subList(i+1, loopEnd);
                    }
                    loopEnd--;
                }
                ArrayList<String> loopInLines = recursiveDataBuilder(recursivePart);
                while (loopCount > 0) {
                    currentInLines.addAll(loopInLines);
                    loopCount--;
                }
                i = loopEnd + 1;
            }
        }
        return currentInLines;
    }

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
