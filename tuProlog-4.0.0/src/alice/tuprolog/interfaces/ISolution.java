package alice.tuprolog.interfaces;

import alice.tuprolog.exceptions.NoSolutionException;
import alice.tuprolog.exceptions.UnknownVarException;

public interface ISolution<Q,S,T> {
    
    public <Z extends T> Z agetVarValue(String varName) throws alice.tuprolog.exceptions.NoSolutionException;

    public <Z extends T> Z getTerm(String varName) throws alice.tuprolog.exceptions.NoSolutionException, UnknownVarException ;

    public boolean isSuccess();

    public boolean isHalted();

    public boolean hasOpenAlternatives();

    public S getSolution() throws NoSolutionException;

    public Q getQuery();

    public java.util.List<? extends T> getBindingVars() throws alice.tuprolog.exceptions.NoSolutionException;
}
