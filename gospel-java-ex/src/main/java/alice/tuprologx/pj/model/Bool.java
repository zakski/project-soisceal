/*
 * Bool.java
 *
 * Created on March 8, 2007, 5:24 PM
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
public class Bool extends Term<Bool> {
    private final Boolean _theBool;

    public Bool(Boolean b) {
        _theBool = b;
    }

    static Bool unmarshal(Struct b) {
        if (!matches(b))
            throw new UnsupportedOperationException();
        if (b.isEqual(Struct.TRUE))
            return new Bool(Boolean.TRUE);
        else
            return new Bool(Boolean.FALSE);
    }

    static boolean matches(com.szadowsz.gospel.core.data.Term t) {
        return (!(t instanceof Var) && (t.isEqual(Struct.TRUE) || t.isEqual(Struct.FALSE)));
    }

    // public Boolean toJava() { return _theBool; } // ED 2013-05-12
    public <Z> Z toJava() {
        return uncheckedCast(_theBool);
    }

    public com.szadowsz.gospel.core.data.Term marshal() {
        return _theBool ? Struct.TRUE : Struct.FALSE;
    }

    public String toString() {
        return "Bool(" + _theBool + ")";
    }

}