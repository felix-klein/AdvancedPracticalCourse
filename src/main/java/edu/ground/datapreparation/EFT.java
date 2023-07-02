package edu.ground.datapreparation;

/**
 * This class is the mother class of Event, Flow and Task to provide with easy to access functionality.
 */
public class EFT {

    /**
     * This method finds and reads the Name out of the String line.
     *
     * @param line is the String line to investigate.
     * @return a String which is the Name.
     */
    protected String findName(String line) {
        return findAttribute(line, " name=\"");
    }

    /**
     * This method finds and reads the ID out of the String line.
     *
     * @param line is the String line to investigate.
     * @return a String which is the ID.
     */
    protected String findID(String line) {
        return findAttribute(line, " id=\"");
    }


    /**
     * This method finds and reads the Type of the Event out of the String line. Because of the location of this
     * information in the element naming.
     *
     * @param line is the String line to investigate.
     * @return a String which is the Type.
     */
    protected String findType(String line) {
        String startString = "<bpmn:";
        int startType = line.indexOf(startString) + startString.length();
        line = line.substring(startType);
        int endType = line.indexOf("Event id=");
        return line.substring(0, endType);
    }

    /**
     * This method finds and reads the Source out of the String line.
     *
     * @param line is the String line to investigate.
     * @return a String which is the Source.
     */
    protected String findSource(String line) {
        return findAttribute(line, " sourceRef=\"");
    }

    /**
     * This method finds and reads the Target out of the String line.
     *
     * @param line is the String line to investigate.
     * @return a String which is the Target.
     */
    protected String findTarget(String line) {
        return findAttribute(line, " targetRef=\"");
    }

    /**
     * This method is a helper for the findXX methods to reduce code duplication.
     *
     * @param line      is the String line to investigate.
     * @param attribute is the string indicator to find the attribute.
     * @returna String which is the searched attribute value.
     */
    private String findAttribute(String line, String attribute) {
        int startAttribute = line.indexOf(attribute) + attribute.length();
        line = line.substring(startAttribute);
        int endAttribute = line.indexOf("\"");
        return line.substring(0, endAttribute);
    }
}
