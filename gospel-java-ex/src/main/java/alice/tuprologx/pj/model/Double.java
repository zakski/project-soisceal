/*
 * Double.java
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
public class Double extends Term<Double> {
    private final java.lang.Double _theDouble;

    public Double(java.lang.Double d) {
        _theDouble = d;
    }

    static Double unmarshal(com.szadowsz.gospel.core.data.Double d) {
        if (!matches(d))
            throw new UnsupportedOperationException();
        return new Double(d.doubleValue());
    }

    static boolean matches(com.szadowsz.gospel.core.data.Term t) {
        return (t instanceof com.szadowsz.gospel.core.data.Double);
    }

    public <Z> Z/*java.lang.Double*/ toJava() {
        //return (Z)_theDouble;
        return uncheckedCast(_theDouble);
    }

    public com.szadowsz.gospel.core.data.Double marshal() {
        return new com.szadowsz.gospel.core.data.Double(_theDouble);
    }

    public String toString() {
        return "Double(" + _theDouble + ")";
    }

}