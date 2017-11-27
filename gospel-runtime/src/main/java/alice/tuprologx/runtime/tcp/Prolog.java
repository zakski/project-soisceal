package alice.tuprologx.runtime.tcp;

import com.szadowsz.gospel.core.Solution;
import com.szadowsz.gospel.core.Theory;
import com.szadowsz.gospel.core.data.Term;

public interface Prolog {

    public void clearTheory() throws Exception;

    public Theory getTheory() throws Exception;

    /**
     * @param theory
     * @throws Exception
     */
    void setTheory(Theory theory) throws Exception;

    void addTheory(Theory theory) throws Exception;

    public Solution solve(String g) throws Exception;

    public Solution solve(Term th) throws Exception;

    public Solution solveNext() throws Exception;

    public boolean hasOpenAlternatives() throws Exception;

    public void solveHalt() throws Exception;

    public void solveEnd() throws Exception;

    public void loadLibrary(String className) throws Exception;

    public void unloadLibrary(String className) throws Exception;
}
