package alice.tuprolog.interfaces;

import alice.tuprolog.util.exception.solve.NoSolutionException;
import alice.tuprolog.core.exception.interpreter.UnknownVarException;

public interface ISolution<Q,S,T> {
    
    public <Z extends T> Z agetVarValue(String varName) throws NoSolutionException;

    public <Z extends T> Z getTerm(String varName) throws NoSolutionException, UnknownVarException ;

    public boolean isSuccess();

    public boolean isHalted();

    public boolean hasOpenAlternatives();

    public S getSolution() throws NoSolutionException;

    public Q getQuery();

    public java.util.List<? extends T> getBindingVars() throws NoSolutionException;
}
