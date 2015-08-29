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
 *
 * @author maurizio
 */
public class Double extends Term<Double> {
	java.lang.Double _theDouble;

	public <Z> Z/*java.lang.Double*/ toJava() {
		//return (Z)_theDouble;
		return uncheckedCast(_theDouble);
	}
	
	public Double (java.lang.Double d) {_theDouble = d;}
           
        public alice.tuprolog.core.data.numeric.Double marshal() {
            return new alice.tuprolog.core.data.numeric.Double(_theDouble);
        }
        
        static Double unmarshal(alice.tuprolog.core.data.numeric.Double d) {
            if (!matches(d))
                throw new UnsupportedOperationException();
            return new Double(d.doubleValue());
        }
        
        static boolean matches(alice.tuprolog.core.data.Term t) {
            return (t instanceof alice.tuprolog.core.data.numeric.Double);
        }
        
	public String toString() {
		return "Double("+_theDouble+")";
	}

}