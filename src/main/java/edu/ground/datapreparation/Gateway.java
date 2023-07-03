package edu.ground.datapreparation;

public class Gateway extends EFT{
    private final String id;
    private final boolean type;

    public Gateway(String eventLine) {
        this.id = findID(eventLine);
        this.type = typeMatching(findType(eventLine, false));
    }

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
     * @return true if the Gateway is PARALLEL and false if it is any other type, which we do not support.
     */
    public boolean getType() {
        return type;
    }
}
