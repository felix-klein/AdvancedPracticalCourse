package edu.ground.datapreparation;

public class Gateway extends EFT{
    private final String id;
    private final boolean type;

    public Gateway(String eventLine) {
        this.id = findID(eventLine);
        this.type = typeMatching(findType(eventLine, false));
    }

    /**
     * This  typeMatching method converts the String of parallel/exclusive into a boolean value, for easier processing.
     * @param type is the found type as a String text.
     * @return a boolean, which is true for a Parallel Gateway and false for an Exclusive (and rest) Gateway.
     */
    private boolean typeMatching(String type) {
        if (type.equals("parallel")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Getter to make the Gateway ID public.
     *
     * @return the ID as String.
     */
    public String getId() {
        return id;
    }

    /**
     * Getter to make the Gateway Type public.
     *
     * @return a boolean, which is true for a Parallel Gateway and false for an Exclusive (and rest) Gateway.
     */
    public boolean getType() {
        return type;
    }
}
