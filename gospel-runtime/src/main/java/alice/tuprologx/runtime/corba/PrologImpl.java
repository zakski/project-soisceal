package alice.tuprologx.runtime.corba;


import com.szadowsz.gospel.core.PrologEngine;
import com.szadowsz.gospel.core.Solution;
import com.szadowsz.gospel.core.Theory;
import com.szadowsz.gospel.core.error.InvalidLibraryException;

import java.io.ByteArrayInputStream;

@SuppressWarnings("serial")
public class PrologImpl extends _PrologImplBase {

    PrologEngine imp;

    /**
     * construction with default libraries: ISO + Meta
     */
    public PrologImpl() {
        try {
            imp = new PrologEngine(new String[]{"alice.tuprolog.lib.MetaLibrary", "alice.tuprolog.lib.ISOLibrary"});
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * construction speciying (eventually) libraries
     */
    public PrologImpl(String[] libs) throws InvalidLibraryException {
        imp = new PrologEngine(libs);
    }

    static alice.tuprologx.runtime.corba.SolveInfo toSolveInfo(Solution info) {
        try {
            alice.tuprologx.runtime.corba.SolveInfo cinfo =
                    new alice.tuprologx.runtime.corba.SolveInfo();
            cinfo.success = info.isSuccess();
            if (info.isSuccess()) {
                cinfo.solution = info.getSolution().toString();
            } else {
                cinfo.solution = "";
            }
            return cinfo;
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * managing theory theory
     */
    public void clearTheory() {
        imp.clearTheory();
    }

    public String getTheory() {
        Theory th = imp.getTheory();
        return th.toString();
    }

    public void setTheory(String st) { //throws InvalidTheoryException {
        try {
            imp.setTheory(new Theory(new ByteArrayInputStream(st.getBytes())));
        } catch (Exception e) {
        }
    }

    public alice.tuprologx.runtime.corba.SolveInfo solve(String g) { //throws MalformedGoalException{
        try {
            return toSolveInfo(imp.solve(g));
        } catch (Exception e) {
            return null;
        }
    }

    public boolean hasOpenAlternatives() {
        //try {
        return imp.hasOpenAlternatives();
        //} catch (Exception e){
        //    return null;
        //}
    }

    public alice.tuprologx.runtime.corba.SolveInfo solveNext() { //throws NoMoreSolutionException{
        try {
            return toSolveInfo(imp.solveNext());
        } catch (Exception e) {
            return null;
        }
    }

    public void solveHalt() {
        imp.solveHalt();
    }

    public void solveEnd() {
        imp.solveEnd();
    }

    public void loadLibrary(String className) { //throws InvalidLibraryException {
        try {
            imp.loadLibrary(className);
        } catch (Exception e) {
        }
    }

    public void unloadLibrary(String className) { //throws InvalidLibraryException {
        try {
            imp.unloadLibrary(className);
        } catch (Exception e) {
        }
    }

}
