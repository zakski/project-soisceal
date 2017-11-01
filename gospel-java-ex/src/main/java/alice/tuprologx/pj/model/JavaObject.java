/*
 * JavaObject.java
 *
 * Created on July 19, 2007, 11:23 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package alice.tuprologx.pj.model;

import alice.tuprologx.pj.engine.PJ;
import com.szadowsz.gospel.core.data.Struct;

/**
 * @author maurizio
 */
public class JavaObject<O> extends Term<JavaObject<O>> {

    private final O _theObject;

    /**
     * Creates a new instance of JavaObject
     */
    public JavaObject(O o) {
        _theObject = o;
    }

    static boolean matches(com.szadowsz.gospel.core.data.Term t) {
        return (t instanceof Struct && PJ.getRegisteredJavaObject((Struct) t) != null);
    }

    static <Z> JavaObject<Z> unmarshalObject(Struct s) {
        if (matches(s)) {
            //return new JavaObject<Z>((Z)(PJ.getRegisteredJavaObject(s)));
            Z auxJavaObject = uncheckedCast(PJ.getRegisteredJavaObject(s));
            return new JavaObject<>(auxJavaObject);
        } else
            throw new UnsupportedOperationException();
    }

    public com.szadowsz.gospel.core.data.Term marshal() {
        return PJ.registerJavaObject(_theObject);
    }

    public <Z> Z toJava() {
        //return (Z)_theObject;
        return uncheckedCast(_theObject);
    }

    public String toString() {
        return "JavaObject{" + _theObject + "}";
    }


}
