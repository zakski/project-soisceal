/*
 * OldVar.java
 *
 * Created on March 8, 2007, 5:06 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package alice.tuprologx.pj.model;

/**
 * @author maurizio
 */
public class Var<X extends Term<?>> extends Term<X> {
    private static java.lang.reflect.Method _setLink = null;

    static {
        try {
            _setLink = com.szadowsz.gospel.core.data.Var.class.getDeclaredMethod("setLink", com.szadowsz.gospel.core.data.Term.class);
            _setLink.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private X _theValue;
    private String _theName;

    public Var(String name) {
        _theName = name;
    }

    //private Var(String name, Term<?> val) {_theName = name; _theValue= (X) val;}
    private Var(String name, Term<?> val) {
        _theName = name;
        _theValue = uncheckedCast(val);
    }

    static Term<?> unmarshal(com.szadowsz.gospel.core.data.Var a) {
        if (!matches(a))
            throw new UnsupportedOperationException();
        //return new Var<Term<?>>(a.getName(),a.isBound() ? Term.unmarshal(a.getTerm()) : null);
        return a.isBound() ? Term.unmarshal(a.getTerm()) : new Var<>(a.getName(), null);
    }

    static boolean matches(com.szadowsz.gospel.core.data.Term t) {
        return (t instanceof com.szadowsz.gospel.core.data.Var);
    }

    private static void setLink(com.szadowsz.gospel.core.data.Var v, Object o) {
        try {
            _setLink.invoke(v, o);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public <Z> Z toJava() {
        // return _theValue != null ? (Z)_theValue.toJava() : (Z)this;
        return uncheckedCast(_theValue != null ? _theValue.toJava() : this);
    }

    public X getValue() {
        return _theValue;
    }

    public com.szadowsz.gospel.core.data.Var marshal() {
        try {
            com.szadowsz.gospel.core.data.Var v = new com.szadowsz.gospel.core.data.Var(_theName);
            if (_theValue != null) {
                setLink(v, _theValue.marshal());
            }
            return v;
        } catch (Exception e) {
            throw new UnsupportedOperationException(e);
            //return null;
        }
    }

    public String getName() {
        return _theName;
    }

    public String toString() {
        return "Var(" + _theName + (_theValue != null ? "/" + _theValue : "") + ")";
    }
}