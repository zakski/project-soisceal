package alice.tuprolog;

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import alice.tuprolog.interfaces.*;

/**
 * @author Alex Benini
 *         <p>
 *         Core engine
 */
public class EngineRunner implements IEngineRunner {
    final static int HALT = -1;
    final static int FALSE = 0;
    final static int TRUE = 1;
    final static int TRUE_CP = 2;

    private static final long serialVersionUID = 1L;

    private Prolog mediator;
    private ITheoryManager theoryManager;
    private IPrimitiveManager primitiveManager;
    private ILibraryManager ILibraryManager;
    private IEngineManager engineManager;

    private int id;
    private int pid;
    private boolean detached;
    private boolean solving;
    private Term query;
    private TermQueue msgs;
    private ArrayList<Boolean> next;
    private int countNext;
    private Lock lockVar;
    private Condition cond;
    private Object semaphore;

    /* Current environment */
    public Engine env;

    /* Last environment used */
    private Engine last_env;

    /* Stack environments of nidicate solving */
    private LinkedList<IEngine> stackEnv = new LinkedList<IEngine>();

    private SolveInfo sinfo;

    final State INIT;
    final State GOAL_EVALUATION;
    final State EXCEPTION;
    final State RULE_SELECTION;
    final State GOAL_SELECTION;
    final State BACKTRACK;
    final State END_FALSE;
    final State END_TRUE;
    final State END_TRUE_CP;
    final State END_HALT;

    public EngineRunner(int id) {

        INIT = new StateInit(this);
        GOAL_EVALUATION = new StateGoalEvaluation(this);
        EXCEPTION = new StateException(this);
        RULE_SELECTION = new StateRuleSelection(this);
        GOAL_SELECTION = new StateGoalSelection(this);
        BACKTRACK = new StateBacktrack(this);
        END_FALSE = new StateEnd(this, FALSE);
        END_TRUE = new StateEnd(this, TRUE);
        END_TRUE_CP = new StateEnd(this, TRUE_CP);
        END_HALT = new StateEnd(this, HALT);

        this.id = id;
    }

    /**
     * Config this Manager
     */
    public void initialize(Prolog vm) {
        mediator = vm;
        theoryManager = vm.getTheoryManager();
        primitiveManager = vm.getPrimitiveManager();
        ILibraryManager = vm.getLibraryManager();
        engineManager = vm.getEngineManager();

        detached = false;
        solving = false;
        sinfo = null;
        msgs = new TermQueue();
        next = new ArrayList<Boolean>();
        countNext = 0;
        lockVar = new ReentrantLock();
        cond = lockVar.newCondition();
        semaphore = new Object();
    }

    @Override
    public void spy(String action, IEngine env) {
        mediator.spy(action, env);
    }

    @Override
    public void warn(String message) {
        mediator.warn(message);
    }

    /*Castagna 06/2011*/
    @Override
    public void exception(String message) {
        mediator.exception(message);
    }

    @Override
    public void detach() {
        detached = true;
    }

    @Override
    public boolean isDetached() {
        return detached;
    }

    /**
     * Solves a query
     *
     * @param g the term representing the goal to be demonstrated
     * @return the result of the demonstration
     * @see SolveInfo
     **/
    private void threadSolve() {
        sinfo = solve();
        solving = false;

        lockVar.lock();
        try {
            cond.signalAll();
        } finally {
            lockVar.unlock();
        }

        if (sinfo.hasOpenAlternatives()) {
            if (next.isEmpty() || !next.get(countNext)) {
                synchronized (semaphore) {
                    try {
                        semaphore.wait(); //Mi metto in attesa di eventuali altre richieste
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public SolveInfo solve() {
        try {
            query.resolveTerm();

            ILibraryManager.onSolveBegin(query);
            primitiveManager.identifyPredicate(query);

            freeze();
            env = new Engine(this, query);
            StateEnd result = env.run();
            defreeze();

            sinfo = new SolveInfo(
                    query,
                    result.getResultGoal(),
                    result.getResultDemo(),
                    result.getResultVars()
            );

            //Alberto
            env.hasOpenAlternatives = sinfo.hasOpenAlternatives();

            if (!sinfo.hasOpenAlternatives())
                solveEnd();

            //Alberto
            env.nResultAsked = 0;

            return sinfo;
        } catch (Exception ex) {
            ex.printStackTrace();
            return new SolveInfo(query);
        }
    }

    /**
     * Gets next solution
     *
     * @return the result of the demonstration
     * @throws NoMoreSolutionException if no more solutions are present
     * @see SolveInfo
     **/
    private void threadSolveNext() throws NoMoreSolutionException {
        solving = true;
        next.set(countNext, false);
        countNext++;
        sinfo = solveNext();

        solving = false;

        lockVar.lock();
        try {
            cond.signalAll();
        } finally {
            lockVar.unlock();
        }

        if (sinfo.hasOpenAlternatives()) {
            if (countNext > (next.size() - 1) || !next.get(countNext)) {
                try {
                    synchronized (semaphore) {
                        semaphore.wait(); //Mi metto in attesa di eventuali altre richieste
                    }
                } catch (InterruptedException e) {
                }
            }
        }
    }

    @Override
    public SolveInfo solveNext() throws NoMoreSolutionException {
        if (hasOpenAlternatives()) {
            refreeze();
            env.nextState = BACKTRACK;

            StateEnd result = env.run();
            defreeze();
            sinfo = new SolveInfo(
                    env.query,
                    result.getResultGoal(),
                    result.getResultDemo(),
                    result.getResultVars()
            );

            //Alberto
            env.hasOpenAlternatives = sinfo.hasOpenAlternatives();

            if (!sinfo.hasOpenAlternatives()) {
                solveEnd();
            }

            //Alberto
            env.nResultAsked = env.nResultAsked + 1;

            return sinfo;

        } else
            throw new NoMoreSolutionException();
    }


    /**
     * Halts current solve computation
     */
    @Override
    public void solveHalt() {
        env.requestStop();
        ILibraryManager.onSolveHalt();
    }

    /**
     * Accepts current solution
     */
    @Override
    public void solveEnd() {
        ILibraryManager.onSolveEnd();
    }

    private void freeze() {
        if (env == null) return;
        try {
            if (stackEnv.getLast() == env) return;
        } catch (NoSuchElementException e) {
        }
        stackEnv.addLast(env);
    }

    private void refreeze() {
        freeze();
        env = last_env;
    }

    private void defreeze() {
        last_env = env;
        if (stackEnv.isEmpty()) return;
        env = (Engine) (stackEnv.removeLast());
    }

    public List<ClauseInfo> find(Term t) {
        return theoryManager.find(t);
    }

    @Override
    public void identify(Term t) {
        primitiveManager.identifyPredicate(t);
    }

    @Override
    public void pushSubGoal(SubGoalTree goals) {
        env.currentContext.goalsToEval.pushSubGoal(goals);
    }

    @Override
    public void cut() {
        env.choicePointSelector.cut(env.currentContext.choicePointAfterCut);
    }

    @Override
    public ExecutionContext getCurrentContext() {
        return (env == null) ? null : env.currentContext;
    }


    /**
     * Asks for the presence of open alternatives to be explored
     * in current demostration process.
     *
     * @return true if open alternatives are present
     */
    @Override
    public boolean hasOpenAlternatives() {
        if (sinfo == null) return false;
        return sinfo.hasOpenAlternatives();
    }


    /**
     * Checks if the demonstration process was stopped by an halt command.
     *
     * @return true if the demonstration was stopped
     */
    @Override
    public boolean isHalted() {
        if (sinfo == null) return false;
        return sinfo.isHalted();
    }


    @Override
    public void run() {
        solving = true;
        pid = (int) Thread.currentThread().getId();

        if (sinfo == null) {
            threadSolve();
        }
        try {
            while (hasOpenAlternatives())
                if (next.get(countNext))
                    threadSolveNext();
        } catch (NoMoreSolutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public int getPid() {
        return pid;
    }

    @Override
    public SolveInfo getSolution() {
        return sinfo;
    }

    @Override
    public void setGoal(Term goal) {
        this.query = goal;
    }

    @Override
    public boolean nextSolution() {
        solving = true;
        next.add(true);

        synchronized (semaphore) {
            semaphore.notify();
        }
        return true;
    }

    @Override
    public SolveInfo read() {
        lockVar.lock();
        try {
            while (solving || sinfo == null)
                try {
                    cond.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        } finally {
            lockVar.unlock();
        }

        return sinfo;
    }

    @Override
    public void setSolving(boolean solved) {
        solving = solved;
    }


    @Override
    public void sendMsg(Term t) {
        msgs.store(t);
    }


    @Override
    public boolean getMsg(Term t) {
        msgs.get(t, mediator, this);
        return true;
    }


    @Override
    public boolean peekMsg(Term t) {
        return msgs.peek(t, mediator);
    }


    @Override
    public boolean removeMsg(Term t) {
        return msgs.remove(t, mediator);
    }


    @Override
    public boolean waitMsg(Term msg) {
        msgs.wait(msg, mediator, this);
        return true;
    }


    @Override
    public int msgQSize() {
        return msgs.size();
    }

    @Override
    public ITheoryManager getTheoryManager() {
        return theoryManager;
    }

    //Alberto
    @Override
    public IEngineManager getEngineMan() {
        return this.engineManager;
    }

    //Alberto
    public Prolog getMediator() {
        return this.mediator;
    }

    //Alberto
    @Override
    public Term getQuery() {
        return this.query;
    }

}