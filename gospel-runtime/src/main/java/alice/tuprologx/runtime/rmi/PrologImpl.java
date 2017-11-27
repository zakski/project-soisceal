package alice.tuprologx.runtime.rmi;

import com.szadowsz.gospel.core.PrologEngine;
import com.szadowsz.gospel.core.Solution;
import com.szadowsz.gospel.core.Theory;
import com.szadowsz.gospel.core.data.Term;
import com.szadowsz.gospel.core.error.InvalidLibraryException;
import com.szadowsz.gospel.core.error.InvalidTheoryException;
import com.szadowsz.gospel.core.error.MalformedGoalException;
import com.szadowsz.gospel.core.error.NoMoreSolutionException;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

@SuppressWarnings("serial")
public class PrologImpl extends UnicastRemoteObject implements alice.tuprologx.runtime.rmi.Prolog, Serializable {

    private PrologEngine imp;

    public PrologImpl() throws RemoteException {
        try {
            imp = new PrologEngine();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void clearTheory() throws RemoteException {
        imp.clearTheory();
    }

    public Theory getTheory() throws RemoteException {
        return imp.getTheory();
    }

    public void setTheory(Theory theory) throws InvalidTheoryException, RemoteException {
        imp.setTheory(theory);
    }

    public void addTheory(Theory theory) throws InvalidTheoryException, RemoteException {
        imp.addTheory(theory);
    }


    public Solution solve(Term g) throws RemoteException {
        return imp.solve(g);
    }

    public Solution solve(String g) throws MalformedGoalException, RemoteException {
        return imp.solve(g);
    }

    public boolean hasOpenAlternatives() throws java.rmi.RemoteException {
        return imp.hasOpenAlternatives();
    }

    public Solution solveNext() throws NoMoreSolutionException, RemoteException {
        return imp.solveNext();
    }

    public void solveHalt() throws RemoteException {
        imp.solveHalt();
    }

    public void solveEnd() throws RemoteException {
        imp.solveEnd();
    }


    public void loadLibrary(String className) throws InvalidLibraryException, RemoteException {
        imp.loadLibrary(className);
    }

    public void unloadLibrary(String className) throws InvalidLibraryException, RemoteException {
        imp.unloadLibrary(className);
    }

}
