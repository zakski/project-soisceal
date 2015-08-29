/*
 * Solution.java
 *
 * Created on 13 marzo 2007, 12.00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package alice.tuprologx.pj.engine;

import java.util.List;
import java.util.Vector;

import alice.tuprolog.core.engine.Solution;
import alice.tuprolog.core.exception.interpreter.UnknownVarException;
import alice.tuprolog.util.exception.solve.NoSolutionException;
import alice.tuprologx.pj.model.Term;

/**
 *
 * @author Maurizio
 */
public class PrologSolution<Q extends Term<?>, S extends Term<?>> /*implements ISolution<Q,S,Term<?>>*/ {
    
    private Solution _solveInfo;
    
    /** Creates a new instance of Solution */
    public PrologSolution(Solution si) {
        _solveInfo = si;
    }

    public <Z extends Term<?>> Z getVarValue(String varName) throws NoSolutionException {
        alice.tuprolog.core.data.Term retValue;
        retValue = _solveInfo.getVarValue(varName);
        return Term.<Z>unmarshal(retValue);
    }

    public <Z extends Term<?>> Z getTerm(String varName) throws NoSolutionException, UnknownVarException {
        alice.tuprolog.core.data.Term retValue;
        retValue = _solveInfo.getTerm(varName);
        return Term.<Z>unmarshal(retValue);
    }

    public boolean isSuccess() {        
        return _solveInfo.isSuccess();
    }

    public boolean isHalted() {        
        return _solveInfo.isHalted();
    }

    public boolean hasOpenAlternatives() {        
        return _solveInfo.hasOpenAlternatives();
    }

    public S getSolution() throws NoSolutionException {
        alice.tuprolog.core.data.Term retValue;
        retValue = _solveInfo.getSolution();
        return Term.<S>unmarshal(retValue);
    }

    public Q getQuery() {
        alice.tuprolog.core.data.Term retValue;
        retValue = _solveInfo.getQuery();
        return Term.<Q>unmarshal(retValue);
    }

    public List<Term<?>> getBindingVars() throws NoSolutionException {
        List<alice.tuprolog.core.data.Var> retValue;
        retValue = scala.collection.JavaConversions.seqAsJavaList(_solveInfo.getBindingVars());
        Vector<Term<?>> bindings = new Vector<Term<?>>();
        for (alice.tuprolog.core.data.Term t : retValue) {
            bindings.add(Term.unmarshal(t));
        }
        return bindings;
    }
}
