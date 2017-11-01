/*
 * Prolog.java
 *
 * Created on March 12, 2007, 2:41 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package alice.tuprologx.pj.engine;


import alice.tuprologx.pj.model.Term;
import alice.tuprologx.pj.model.Theory;
import com.szadowsz.gospel.core.PrologEngine;
import com.szadowsz.gospel.core.Solution;
import com.szadowsz.gospel.core.data.Struct;
import com.szadowsz.gospel.core.error.InvalidTheoryException;
import com.szadowsz.gospel.core.error.NoMoreSolutionException;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author maurizio
 */
public class PJProlog /*extends alice.tuprolog.Prolog*/ {

    final PrologEngine engine;

    public PJProlog() {
        engine = new PrologEngine();
        try {
            engine.unloadLibrary("alice.tuprolog.lib.OOLibrary");
            engine.loadLibrary("alice.tuprologx.pj.lib.PJLibraryNew");
            engine.loadLibrary("alice.tuprolog.lib.DCGLibrary");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public <G extends Term<?>, S extends Term<?>> Iterable<PrologSolution<G, S>> solveAll(final G query) {
        class SolutionProxy implements Iterable<PrologSolution<G, S>> {
            public Iterator<PrologSolution<G, S>> iterator() {
                PrologSolution<G, S> first = PJProlog.this.solve(query);
                return new SolutionIterator<>(first);
            }
        }
        return new SolutionProxy();
    }

    public <G extends Term<?>, S extends Term<?>> PrologSolution<G, S> solve(G g) {
        Solution retValue;
        retValue = engine.solve(g.marshal());
        return new PrologSolution<>(retValue);
    }

    public <G extends Term<?>, S extends Term<?>> PrologSolution<G, S> solveNext() throws NoSolutionException {
        Solution retValue;
        try {
            retValue = engine.solveNext();
        } catch (Exception e) {
            throw new NoSolutionException();
        }
        return new PrologSolution<>(retValue);
    }

    public void addTheory(Theory theory) throws InvalidTheoryException {
        engine.addTheory(new com.szadowsz.gospel.core.Theory(theory.marshal()));
    }

    public Theory getTheory() throws InvalidTheoryException {
        return Theory.unmarshal(engine.getTheory());
    }

    public void setTheory(Theory theory) throws InvalidTheoryException {
        engine.setTheory(new com.szadowsz.gospel.core.Theory(theory.marshal()));
    }

    public Struct registerJavaObject(Object o) {
        return ((alice.tuprolog.lib.OOLibrary) engine.getLibrary("alice.tuprologx.pj.lib.PJLibraryNew")).register(o);
    }

    public Object getJavaObject(Struct t) {
        try {
            return ((alice.tuprolog.lib.OOLibrary) engine.getLibrary("alice.tuprologx.pj.lib.PJLibraryNew")).getRegisteredObject(t);
        } catch (Exception e) {
            return null;
        }
    }

    public void loadLibrary(alice.tuprolog.Library library) {
        try {
            engine.loadLibrary(library);
        } catch (Exception e) {
            throw new UnsupportedOperationException(e);
        }
    }

    public alice.tuprologx.pj.lib.PJLibraryNew getPJLibrary() {
        try {
            return (alice.tuprologx.pj.lib.PJLibraryNew) engine.getLibrary("alice.tuprologx.pj.lib.PJLibraryNew");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @author ale
     */
    class SolutionIterator<G extends Term<?>, S extends Term<?>> implements Iterator<PrologSolution<G, S>> {

        PrologSolution<G, S> current = null;
        PrologSolution<G, S> next = null;

        SolutionIterator(PrologSolution<G, S> first) {
            this.current = first;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        public PrologSolution<G, S> next() {
            if (current != null) {
                hasNext();
                PrologSolution<G, S> temp = current;
                current = (next != null && next.isSuccess()) ? next : null;
                next = null;
                return temp;
            } else {
                throw new NoSuchElementException();
            }
        }

        public boolean hasNext() {
            if (next == null) {
                try {
                    next = new PrologSolution<>(engine.solveNext());
                } catch (NoMoreSolutionException e) {
                    next = null;
                }
            }
            return current != null && current.isSuccess();// && next.isSuccess();
        }
    }
}
