package edu.ground.datapreparation;

/**
 * The object Flow is containing its id, sourceRef and targetRef.
 */
public class Flow extends EFT {
    private final String id;
    private final String sourceRef;
    private final String targetRef;

    public Flow(String flowLine) {
        this.id = findID(flowLine);
        this.sourceRef = findSource(flowLine);
        this.targetRef = findTarget(flowLine);
    }

    /**
     * Getter to make the Flow ID public.
     *
     * @return the ID as String.
     */
    public String getId() {
        return id;
    }

    /**
     * Getter to make the Flow Source Reference public.
     *
     * @return the Source as String.
     */
    public String getSourceRef() {
        return sourceRef;
    }

    /**
     * Getter to make the Flow Target Reference public.
     *
     * @return the Target as String.
     */
    public String getTargetRef() {
        return targetRef;
    }
}
