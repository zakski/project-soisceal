/*
 * Bool.java
 *
 * Created on March 8, 2007, 5:24 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package alice.tuprologx.pj.model;

/**
 *
 * @author maurizio
 */
public class Bool extends Term<Bool> {
	Boolean _theBool;        
        
	// public Boolean toJava() { return _theBool; } // ED 2013-05-12
	public <Z> Z toJava() { return uncheckedCast (_theBool); }
	
	public Bool (Boolean b) {_theBool = b;}
        
        public alice.tuprolog.core.data.Term marshal() {
            return _theBool.booleanValue() ? alice.tuprolog.core.data.Struct.TRUE() : alice.tuprolog.core.data.Struct.FALSE();
        }
        
        static Bool unmarshal(alice.tuprolog.core.data.Struct b) {
            if (!matches(b))
                throw new UnsupportedOperationException();
            if (b.isEqual(alice.tuprolog.core.data.Struct.TRUE()))
                return new Bool(Boolean.TRUE);
            else 
                return new Bool(Boolean.FALSE);
        }
        
        static boolean matches(alice.tuprolog.core.data.Term t) {
            return (!(t instanceof alice.tuprolog.core.data.Var) && (t.isEqual(alice.tuprolog.core.data.Struct.TRUE()) || t.isEqual(alice.tuprolog.core.data.Struct.FALSE())));
        }
        
	public String toString() {
		return "Bool("+_theBool+")";
	}

}