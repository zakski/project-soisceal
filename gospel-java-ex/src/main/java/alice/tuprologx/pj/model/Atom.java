/*
 * Atom.java
 *
 * Created on March 8, 2007, 5:10 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package alice.tuprologx.pj.model;

import com.szadowsz.gospel.core.data.Struct;
import com.szadowsz.gospel.core.data.Var;

/**
 * @author maurizio
 */
public class Atom extends Term<Atom> {
    private final String _theAtom;

    public Atom(String s) {
        _theAtom = s;
    }

    static Atom unmarshal(Struct a) {
        if (!matches(a))
            throw new UnsupportedOperationException();
        return new Atom(a.getName());
    }

    static boolean matches(com.szadowsz.gospel.core.data.Term t) {
        return (!(t instanceof Var) && t.isAtom() && !t.isList() && !Bool.matches(t));
    }

    public <Z> Z toJava() {
        //return (Z)_theAtom;
        return uncheckedCast(_theAtom);
    }

    public String toString() {
        return "Atom(" + _theAtom + ")";
    }

    public Struct marshal() {
        return new Struct(_theAtom);
    }

    public List<Atom> toCharList() {
        char[] carr = _theAtom.toCharArray();
        java.util.Vector<String> vs = new java.util.Vector<>();
        for (char c : carr) {
            vs.add(c + "");
        }
        return new List<>(vs);
    }

    public List<Atom> split(String regexp) {
        java.util.Vector<String> vs = new java.util.Vector<>();
        for (String s : _theAtom.split(regexp)) {
            vs.add(s);
        }
        return new List<>(vs);
    }
}