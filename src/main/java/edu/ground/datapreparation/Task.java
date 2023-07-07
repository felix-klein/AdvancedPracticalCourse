package edu.ground.datapreparation;

/**
 * The object Task is containing its id and name.
 */
public class Task extends EFT {
    private final String id;
    private final String name;
    private final OperationTable operations;
    private boolean inAcceptance;
    private String breachStatement;

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

    /**
     * Getter to make the compliance acceptance public.
     *
     * @return a boolean with true if the Task is accepted and false otherwise.
     */
    public boolean isInAcceptance() {
        return inAcceptance;
    }

    /**
     * Setter to let the ccp process if the Task is in the rules.
     *
     * @param inAcceptance is a boolean which is true for correct sensor comparison.
     */
    public void setInAcceptance(boolean inAcceptance) {
        this.inAcceptance = inAcceptance;
    }

    /**
     * Getter to make the breach statement readable.
     *
     * @return a String which is the breach declaration.
     */
    public String getBreachStatement() {
        return breachStatement;
    }

    /**
     * Setter to initialise the breach statement if there is one.
     *
     * @param breachStatement is a String with a declaration why the inAcceptance statement is false.
     */
    public void setBreachStatement(String breachStatement) {
        this.breachStatement = breachStatement;
    }
}
