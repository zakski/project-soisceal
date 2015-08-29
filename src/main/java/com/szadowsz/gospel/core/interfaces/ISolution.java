package com.szadowsz.gospel.core.interfaces;

import com.szadowsz.gospel.core.exception.interpreter.UnknownVarException;
import com.szadowsz.gospel.util.exception.solve.NoSolutionException;

public interface ISolution<Q,S,T> {
    
    public <Z extends T> Z agetVarValue(String varName) throws NoSolutionException;

    public <Z extends T> Z getTerm(String varName) throws NoSolutionException, UnknownVarException;

    public boolean isSuccess();

    public boolean isHalted();

    public boolean hasOpenAlternatives();

    public S getSolution() throws NoSolutionException;

    public Q getQuery();

    public java.util.List<? extends T> getBindingVars() throws NoSolutionException;
}
