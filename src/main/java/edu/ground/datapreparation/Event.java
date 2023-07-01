package edu.ground.datapreparation;

/**
 * The object Event is containing its id, name and type.
 */
public class Event extends EFT {
    private final String id;
    private final String name;
    private final String type;

    public Event(String eventLine) {
        this.id = findID(eventLine);
        this.name = findName(eventLine);
        this.type = findType(eventLine);
    }

    /**
     * Getter to make the Event ID public.
     *
     * @return the ID as String.
     */
    public String getId() {
        return id;
    }

    /**
     * Getter to make the Event Name public.
     *
     * @return the Name as String.
     */
    public String getName() {
        return name;
    }

    /**
     * Getter to make the Event Type public.
     *
     * @return the Type as String.
     */
    public String getType() {
        return type;
    }
}
