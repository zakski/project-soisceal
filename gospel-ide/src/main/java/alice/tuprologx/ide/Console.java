package alice.tuprologx.ide;

/**
 * @author ale
 */

interface Console {
    boolean hasOpenAlternatives();

    void enableTheoryCommands(boolean flag);

    void getNextSolution();

    void acceptSolution();

    void stopEngine();

    /**
     * @uml.property name="goal"
     */
    String getGoal();
}