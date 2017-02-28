package alice.tuprolog.interfaces;

import alice.tuprolog.ChoicePointStore;
import alice.tuprolog.ExecutionContext;
import alice.tuprolog.StateEnd;
import alice.tuprolog.Term;

import java.util.List;

/**
 * Created on 28/02/2017.
 */
public interface IEngine {
    //Alberto
    int getNDemoSteps();

    //Alberto
    int getNResultAsked();

    //Alberto
    boolean hasOpenAlternatives();

    String toString();

    void requestStop();

    Term getQuery();

    int getNumDemoSteps();

    List<ExecutionContext> getExecutionStack();

    ChoicePointStore getChoicePointStore();

    String getNextStateName();

    void initialize(ExecutionContext eCtx);

    void prepareGoal();

    ExecutionContext getContext();
}
