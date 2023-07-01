package edu.ground.datapreparation;

/**
 * The object Task is containing its id and name.
 */
public class Task extends EFT {
    private final String id;
    private final String name;

    public Task(String taskLine) {
        this.id = findID(taskLine);
        this.name = findName(taskLine);
    }

    /**
     * Getter to make the Task ID public.
     *
     * @return the ID as String.
     */
    public String getId() {
        return id;
    }

    /**
     * Getter to make the Task Name public.
     *
     * @return the Name as String.
     */
    public String getName() {
        return name;
    }
}
