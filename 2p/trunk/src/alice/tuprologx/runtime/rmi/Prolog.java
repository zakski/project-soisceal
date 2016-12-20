package alice.tuprologx.runtime.rmi;
import alice.tuprolog.*;
import alice.tuprolog.exceptions.InvalidLibraryException;
import alice.tuprolog.exceptions.InvalidTheoryException;
import alice.tuprolog.exceptions.MalformedGoalException;
import alice.tuprolog.exceptions.NoMoreSolutionException;

/**
 * @author  ale
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


    public SolveInfo   solve(Term g) throws java.rmi.RemoteException;

    public SolveInfo   solve(String g) throws MalformedGoalException, java.rmi.RemoteException;

    public boolean   hasOpenAlternatives() throws java.rmi.RemoteException;

    public SolveInfo   solveNext() throws NoMoreSolutionException, java.rmi.RemoteException;

    public void solveHalt() throws java.rmi.RemoteException;

    public void solveEnd() throws java.rmi.RemoteException;


    public void loadLibrary(String className) throws InvalidLibraryException, java.rmi.RemoteException;

    public void unloadLibrary(String className) throws InvalidLibraryException, java.rmi.RemoteException;

}
