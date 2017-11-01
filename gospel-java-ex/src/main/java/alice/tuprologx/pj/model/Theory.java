/*
 * Theory.java
 *
 * Created on April 4, 2007, 10:00 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package alice.tuprologx.pj.model;

import com.szadowsz.gospel.core.PrologEngine;
import com.szadowsz.gospel.core.data.Struct;
import com.szadowsz.gospel.core.data.Term;

import java.util.Collection;
import java.util.Vector;

/**
 * @author maurizio
 */
public class Theory extends List<Clause<?, ?>> {

    private static final PrologEngine engine;

    static {
        engine = new PrologEngine();
        try {
            engine.unloadLibrary("alice.tuprolog.lib.OOLibrary");
            engine.loadLibrary("alice.tuprologx.pj.lib.PJLibrary");
            engine.loadLibrary("alice.tuprolog.lib.DCGLibrary");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /*  
        static {
            try {
                alice.tuprolog.Prolog p = new alice.tuprolog.Prolog();
                java.lang.reflect.Method m = p.getClass().getDeclaredMethod("getTheoryManager");
                m.setAccessible(true);             
                _tm = (alice.tuprolog.TheoryManager)m.invoke(p);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    */

    /**
     * Creates a new instance of Theory
     */
    public Theory(Collection<Clause<?, ?>> clauses) {
        super(clauses);
    }

    public Theory(String s) {
        this(parseTheory(s));
        //System.out.println(this);     
    }

    public Theory(String[] s) {
        this(parseTheoryArray(s));
    }

    public static Theory unmarshal(com.szadowsz.gospel.core.Theory t) {
        Vector<Clause<?, ?>> clauses = new Vector<>();
        for (java.util.Iterator<? extends Term> it = t.iterator(engine); it.hasNext(); ) {
            Struct st = (Struct) it.next();
            //Clause<?,?> clause = new Clause(Term.unmarshal(st.getArg(0)),Term.unmarshal(st.getArg(1)));
            Clause<?, ?> clause = new Clause<>(st);
            clauses.add(clause);
        }
        return new Theory(clauses);
    }


    /* This method should be removed or deprecated when (hepefully) one day tuProlog
     * will expose the clause list view over a Prolog theory. Currently this method has to deal with all
     * the strange formattings that are carried out by the tuProlog's TheoryManager!!
     */
    private static Collection<Clause<?, ?>> parseTheory(String s) {
        Vector<Clause<?, ?>> clauses = new Vector<>();
        com.szadowsz.gospel.core.Theory t;
        try {
            t = new com.szadowsz.gospel.core.Theory(s);
        } catch (Exception e) {
            throw new UnsupportedOperationException(e);
        }
        for (java.util.Iterator<? extends Term> it = t.iterator(engine); it.hasNext(); ) {
            Struct st = (Struct) it.next();
            //Clause<?,?> clause = new Clause(Term.unmarshal(st.getArg(0)),Term.unmarshal(st.getArg(1)));
            Clause<?, ?> clause = new Clause<>(st);
            clauses.add(clause);
        }
        return clauses;
    }

    private static Collection<Clause<?, ?>> parseTheoryArray(String[] arr) {
        String temp = "";
        for (String s : arr) {
            temp += s + "\n";
        }
        return parseTheory(temp);
    }

    public Clause<?, ?>[] find(String name, int arity) {
        Vector<Clause<?, ?>> temp = new Vector<>();
        for (Clause<?, ?> c : this) {
            if (c.match(name, arity))
                temp.add(c);
        }
        return temp.toArray(new Clause<?, ?>[temp.size()]);
    }

    public Struct marshal() {
        Struct s = super.marshal();
        java.util.Iterator<? extends Term> listIterator = s.listIterator();
        while (listIterator.hasNext()) {
            listIterator.next().resolveTerm();
        }
        return s;
    }

    public void appendTheory(Theory that) {
        for (Clause<?, ?> c : that) {
            _theList.add(c);
        }
    }
}
