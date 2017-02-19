package alice.tuprolog.interfaces;

import alice.tuprolog.ClauseInfo;
import alice.tuprolog.InvalidTheoryException;
import alice.tuprolog.Struct;
import alice.tuprolog.Term;
import alice.tuprolog.json.AbstractEngineState;
import alice.tuprolog.json.FullEngineState;

import java.io.Serializable;
import java.util.List;

/**
 * Created on 19/02/2017.
 */
public interface ITheoryManager extends Serializable {

    /**
     * inserting of a clause at the head of the dbase
     */
    void assertA(Struct clause, boolean dyn, String libName, boolean backtrackable);

    /**
     * inserting of a clause at the end of the dbase
     */
    void assertZ(Struct clause, boolean dyn, String libName, boolean backtrackable);

    /**
     * removing from dbase the first clause with head unifying with clause
     */
    ClauseInfo retract(Struct cl);

    /**
     * removing from dbase all the clauses corresponding to the
     * predicate indicator passed as a parameter
     */
    boolean abolish(Struct pi);

    /**
     * Returns a family of clauses with functor and arity equals
     * to the functor and arity of the term passed as a parameter
     *
     * Reviewed by Paolo Contessi: modified according to new ClauseDatabase
     * implementation
     */
    List<ClauseInfo> find(Term headt);

    /**
     * Consults a theory.
     *
     * @param theory        theory to add
     * @param dynamicTheory if it is true, then the clauses are marked as dynamic
     * @param libName       if it not null, then the clauses are marked to belong to the specified library
     */
    void consult(ITheory theory, boolean dynamicTheory, String libName) throws InvalidTheoryException;

    /**
     * Binds clauses in the database with the corresponding
     * primitive predicate, if any
     */
    void rebindPrimitives();

    /**
     * Clears the clause dbase.
     */
    void clear();

    /**
     * remove all the clauses of lib theory
     */
    void removeLibraryTheory(String libName);

    void solveTheoryGoal();

    /**
     * add a goal eventually defined by last parsed theory.
     */
    void addStartGoal(Struct g);

    /**
     * Gets current theory
     *
     * @param onlyDynamic if true, fetches only dynamic clauses
     */
    String getTheory(boolean onlyDynamic);

    /**
     * Gets last consulted theory
     * @return  last theory
     */
    ITheory getLastConsultedTheory();

    void clearRetractDB();

    //Alberto
    boolean checkExistence(String predicateIndicator);

    //Alberto
    void serializeLibraries(FullEngineState brain);

    //Alberto
    void serializeTimestamp(AbstractEngineState brain);

    //Alberto
    void serializeDynDataBase(FullEngineState brain);
}
