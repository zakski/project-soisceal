/*
 * Var.java
 *
 * Created on March 8, 2007, 5:06 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package alice.tuprologx.pj.model;
        
/**
 *
 * @author maurizio
 */
public class Var<X extends Term<?>> extends Term<X> {
		X _theValue;
        String _theName;
        private static java.lang.reflect.Method _setLink = null;
        
        static {
            try {
                _setLink = alice.tuprolog.core.data.Var.class.getDeclaredMethod("setLink", alice.tuprolog.core.data.Term.class);
                _setLink.setAccessible(true);                
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }

	public Var(String name) {_theName = name;}
        
    //private Var(String name, Term<?> val) {_theName = name; _theValue= (X) val;}
    private Var(String name, Term<?> val) {
    	_theName = name; _theValue = uncheckedCast(val);
    }
    		
	public <Z> Z toJava() {
		// return _theValue != null ? (Z)_theValue.toJava() : (Z)this;
		return uncheckedCast( _theValue != null ? _theValue.toJava() : this );
	}
        
    public X getValue() {return _theValue;}
    
    public alice.tuprolog.core.data.Var marshal() {
        try {
            alice.tuprolog.core.data.Var v= new alice.tuprolog.core.data.Var(_theName);
            if (_theValue != null) {
                setLink(v, _theValue.marshal());
            }
            return v;
        }
        catch(Exception e) {
            throw new UnsupportedOperationException(e);
            //return null;
        }
    }
    
    static Term<?> unmarshal(alice.tuprolog.core.data.Var a) {
        if (!matches(a))
            throw new UnsupportedOperationException();
        //return new Var<Term<?>>(a.getName(),a.isBound() ? Term.unmarshal(a.getTerm()) : null);            
        return a.isBound() ? Term.unmarshal(a.getTerm()) : new Var<Term<?>>(a.getName(), null);
    }
    
    static boolean matches(alice.tuprolog.core.data.Term t) {
        return (t instanceof alice.tuprolog.core.data.Var);
    }
    
    public String getName() {
        return _theName;
    }
    
    public String toString() {
		return "Var("+_theName+(_theValue != null ? "/"+_theValue : "")+")";
	}
        
    private static void setLink(alice.tuprolog.core.data.Var v, Object o) {
        try {                
            _setLink.invoke(v,o);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}