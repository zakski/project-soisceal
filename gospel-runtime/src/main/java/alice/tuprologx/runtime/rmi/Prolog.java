package alice.tuprologx.runtime.rmi;

import com.szadowsz.gospel.core.Solution;
import com.szadowsz.gospel.core.Theory;
import com.szadowsz.gospel.core.data.Term;
import com.szadowsz.gospel.core.error.InvalidLibraryException;
import com.szadowsz.gospel.core.error.InvalidTheoryException;
import com.szadowsz.gospel.core.error.MalformedGoalException;
import com.szadowsz.gospel.core.error.NoMoreSolutionException;

/**
 * @author ale
 */
public interface Prolog extends java.rmi.Remote {


    public void clearTheory() throws java.rmi.RemoteException;

    public Theory getTheory() throws java.rmi.RemoteException;

    /**
     * @param theory
     * @throws InvalidTheoryException
     * @throws java.rmi.RemoteException
     */
    public void setTheory(Theory theory) throws InvalidTheoryException, java.rmi.RemoteException;

    public void addTheory(Theory theory) throws InvalidTheoryException, java.rmi.RemoteException;


    public Solution solve(Term g) throws java.rmi.RemoteException;

    public Solution solve(String g) throws MalformedGoalException, java.rmi.RemoteException;

    public boolean hasOpenAlternatives() throws java.rmi.RemoteException;

    public Solution solveNext() throws NoMoreSolutionException, java.rmi.RemoteException;

    public void solveHalt() throws java.rmi.RemoteException;

    public void solveEnd() throws java.rmi.RemoteException;


    public void loadLibrary(String className) throws InvalidLibraryException, java.rmi.RemoteException;

    public void unloadLibrary(String className) throws InvalidLibraryException, java.rmi.RemoteException;

}
