package edu.ground.datapreparation;

/**
 * The operationTable is a class for gathering, ordering and saving the instructions/operations from a Task.
 */
public class OperationTable {
    private String[] operationName;
    private short[] operationValue;

    /**
     * To initialise an operation table it does need the name of the task, which is the information pool.
     *
     * @param taskName is the name of the task including all the information.
     */
    public OperationTable(String taskName) {
        String[] operationList = splitOperations(taskName);
        analyseList(operationList);
    }

    /**
     * This method splits the Task name into its operations line by line.
     *
     * @param fullLine is the Name of the Task including the raw data.
     * @return a String Array with each line to be a separate operation.
     */
    private String[] splitOperations(String fullLine) {
        String[] operationList = fullLine.split("&#10;");
        for (int i = 0; i < operationList.length; i++) {
            operationList[i] = operationList[i].trim();
        }
        return operationList;
    }

    /**
     * This method analyses the list values and set them correctly for further processing and input control.
     *
     * @param operationList is the array of operations.
     */
    private void analyseList(String[] operationList) {
        /* Initializing the two arrays with the length of the new liners. */
        operationName = new String[operationList.length];
        operationValue = new short[operationList.length];

        /* Go through each element and analyze its values. */
        for (int i = 0; i < operationList.length; i++) {
            /* For the case there is a syntax error, we will just ignore the operation. */
            if (!operationList[i].contains(":")) {
                operationName[i] = null;
                operationValue[i] = -11111;
            }
            /* We separate the operation into its name and value. */
            /* The name part got added to the operationName without anny filtering at first. */
            String namePart = operationList[i].substring(0, operationList[i].indexOf(":")).trim();
            operationName[i] = nameAnalytics(namePart); /* specify the name or a name group */
            /* The value part does need filtering for the case there is a value in not short format. */
            String valuePart = operationList[i].substring(operationList[i].indexOf(":") + 1).trim();
            operationValue[i] = numberAnalytics(valuePart); /* specify the number or a number code */
        }
    }

    /**
     * The number analyzer is for the correct initialization of the value or its number code.
     *
     * @param number is a String containing the number itself or an indication.
     * @return a short which is the number or a number code.
     */
    private short numberAnalytics(String number) {
        try {
            /* If the 'number' is directly a number, what should be the normal case, we can use it. */
            short numberValue = Short.parseShort(number);
            if (numberValue < 0) { return -11111; } /* accept just numbers in the positive spectrum */
            return Short.parseShort(number);
        } catch (NumberFormatException e) {
            /* If the number is a String, we can consider a few possible options, what the user could have meant. */
            if (number.toLowerCase().contains("stop") ||
                    number.toLowerCase().contains("end") ||
                    number.toLowerCase().contains("terminate") ||
                    number.toLowerCase().contains("finish") ||
                    number.toLowerCase().contains("off") ||
                    number.toLowerCase().contains("exit")) {
                /* If the 'number' is kind of an end signal, its number should be 0. */
                return 0;
            } else if (number.toLowerCase().contains("start") ||
                    number.toLowerCase().contains("go") ||
                    number.toLowerCase().contains("run") ||
                    number.toLowerCase().contains("launch") ||
                    number.toLowerCase().contains("begin") ||
                    number.toLowerCase().contains("on")) {
                /* If the 'number' is kind of a start signal, its number should be 1. */
                return 1;
            } else if (number.toLowerCase().contains("increase") ||
                    number.toLowerCase().contains("rise") ||
                    number.toLowerCase().contains("grow") ||
                    number.toLowerCase().contains("up") ||
                    number.toLowerCase().contains("jump")) {
                /* If the 'number' is kind of an increase indication, its number should be -101. */
                return -101;
            } else if (number.toLowerCase().contains("decrease") ||
                    number.toLowerCase().contains("reduce") ||
                    number.toLowerCase().contains("low") ||
                    number.toLowerCase().contains("les") ||
                    number.toLowerCase().contains("ease")) {
                /* If the 'number' is kind of a decrease indication, its number should be -90. */
                return -90;
            }
            /* The default error code for indicating a mismatch or an error. */
            return -11111;
        }
    }

    /**
     * The name analyzer is for the correct initialization/grouping of the operation.
     *
     * @param name is a String containing the name itself or an indication to its group.
     * @return a String which is the name of the operation.
     */
    private String nameAnalytics(String name) {
        try {
            if (name.toLowerCase().contains("gear") ||
                    name.toLowerCase().contains("shift") ||
                    name.toLowerCase().contains("throttle") ||
                    name.toLowerCase().contains("level") ||
                    name.toLowerCase().contains("nivo")) {
                /* If the 'name' is kind of a gear call, the name should be Gear. */
                return "Gear";
            } else if (name.toLowerCase().contains("time") ||
                    name.toLowerCase().contains("duration") ||
                    name.toLowerCase().contains("span") ||
                    name.toLowerCase().contains("period") ||
                    name.toLowerCase().contains("term") ||
                    name.toLowerCase().contains("run") ||
                    name.toLowerCase().contains("length")) {
                /* If the 'name' is kind of a time call, the name should be Runtime. */
                return "Runtime";
            } else if (name.toLowerCase().contains("engine") ||
                    name.toLowerCase().contains("motor") ||
                    name.toLowerCase().contains("machine") ||
                    name.toLowerCase().contains("unit") ||
                    name.toLowerCase().contains("main")) {
                /* If the 'name' is kind of an engine call, the name should be Engine. */
                return "Engine";
            }
            /* Additions could be added as soon as it is obvious which settings are addable. */
            // TODO: Add future modification parameters for the process into the naming list.
            /* The rest which is not an exception but also not identifiable will be handled as null values. */
            return null;
        } catch (Exception e) {
            /* All exceptions and not identifiable names are handled as null values and got ignored for the moment. */
            return null;
        }
    }

    /**
     * Getter to make the operation name accessible.
     *
     * @return the array including the operations.
     */
    public String[] getOperationName() {
        return operationName;
    }

    /**
     * Getter to make the operation value accessible.
     *
     * @return the array including the operation values.
     */
    public short[] getOperationValue() {
        return operationValue;
    }
}
