package edu.ground.datapreparation;

/**
 * The object Task is containing its id and name.
 */
public class Task extends EFT {
    private final String id;
    private final String name;
    private final OperationTable operations;

    public Task(String taskLine) {
        this.id = findID(taskLine);
        this.name = findName(taskLine);
        this.operations = new OperationTable(name);
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

    /**
     * Getter to make the Operations (including values) public.
     *
     * @return an object with two lists including the operation name and value.
     */
    public OperationTable getOperations() {
        return operations;
    }
}
