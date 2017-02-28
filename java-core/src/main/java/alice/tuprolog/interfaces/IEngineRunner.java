package alice.tuprolog.interfaces;

import alice.tuprolog.*;
import alice.tuprolog.interfaces.IEngineManager;

import java.util.List;

/**
 * Created on 27/02/2017.
 */
public interface IEngineRunner extends java.io.Serializable, Runnable {

    void spy(String action, IEngine env);

    void detach();

    boolean isDetached();

    SolveInfo solve();

    SolveInfo solveNext() throws NoMoreSolutionException;

    /**
     * Halts current solve computation
     */
    void solveHalt();

    /**
     * Accepts current solution
     */
    void solveEnd();

    void identify(Term t);

    void pushSubGoal(SubGoalTree goals);

    void cut();

    ExecutionContext getCurrentContext();

    /**
     * Asks for the presence of open alternatives to be explored
     * in current demostration process.
     *
     * @return true if open alternatives are present
     */
    boolean hasOpenAlternatives();

    /**
     * Checks if the demonstration process was stopped by an halt command.
     *
     * @return true if the demonstration was stopped
     */
    boolean isHalted();

    @Override
    void run();

    int getId();

    int getPid();

    SolveInfo getSolution();

    void setGoal(Term goal);

    boolean nextSolution();

    SolveInfo read();

    void setSolving(boolean solved);

    void sendMsg(Term t);

    boolean getMsg(Term t);

    boolean peekMsg(Term t);

    boolean removeMsg(Term t);

    boolean waitMsg(Term msg);

    int msgQSize();

    //Alberto
    IEngineManager getEngineMan();

    //Alberto
    Term getQuery();

    public ITheoryManager getTheoryManager();

    public List<ClauseInfo> find(Term t);

    public void warn(String message);

    public void exception(String message);

    public Prolog getMediator();
}
