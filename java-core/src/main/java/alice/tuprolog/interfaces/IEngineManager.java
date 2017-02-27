package alice.tuprolog.interfaces;

import alice.tuprolog.*;
import alice.tuprolog.json.AbstractEngineState;

/**
 * Created on 26/02/2017.
 */
public interface IEngineManager extends java.io.Serializable {
    boolean threadCreate(Term threadID, Term goal);

    SolveInfo join(int id);

    SolveInfo read(int id);

    boolean hasNext(int id);

    boolean nextSolution(int id);

    void detach(int id);

    boolean sendMsg(int dest, Term msg);

    boolean sendMsg(String name, Term msg);

    boolean getMsg(int id, Term msg);

    boolean getMsg(String name, Term msg);

    boolean waitMsg(int id, Term msg);

    boolean waitMsg(String name, Term msg);

    boolean peekMsg(int id, Term msg);

    boolean peekMsg(String name, Term msg);

    boolean removeMsg(int id, Term msg);

    boolean removeMsg(String name, Term msg);

    void cut();

    ExecutionContext getCurrentContext();

    void pushSubGoal(SubGoalTree goals);

    SolveInfo solve(Term query);

    void solveEnd();

    void solveHalt();

    SolveInfo solveNext() throws NoMoreSolutionException;

    //Ritorna l'identificativo del thread corrente
    int runnerId();

    boolean createQueue(String name);

    void destroyQueue(String name);

    int queueSize(int id);

    int queueSize(String name);

    boolean createLock(String name);

    void destroyLock(String name);

    boolean mutexLock(String name);

    boolean mutexTryLock(String name);

    boolean mutexUnlock(String name);

    boolean isLocked(String name);

    void unlockAll();

    Engine getEnv();

    void identify(Term t);

    //Alberto
    void serializeQueryState(AbstractEngineState brain);
}
