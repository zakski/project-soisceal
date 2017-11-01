/*
 * Int.java
 *
 * Created on March 8, 2007, 5:25 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package alice.tuprologx.pj.model;

/**
 * @author maurizio
 */
public class Int extends Term<Int> {
    private final Integer _theInt;

    public Int(Integer i) {
        _theInt = i;
    }

    static Int unmarshal(alice.tuprolog.Int i) {
        if (!matches(i))
            throw new UnsupportedOperationException();
        return new Int(i.intValue());
    }

    static boolean matches(alice.tuprolog.Term t) {
        return (t instanceof alice.tuprolog.Int);
    }

    public <Z> Z/*Integer*/ toJava() {
        //return (Z)_theInt;
        return uncheckedCast(_theInt);
    }

    public alice.tuprolog.Int marshal() {
        return new alice.tuprolog.Int(_theInt);
    }

    public String toString() {
        return "Int(" + _theInt + ")";
    }

}