package alice.tuprolog.data;

import alice.tuprolog.core.data.Struct;
import alice.tuprolog.core.data.Var;
import alice.tuprolog.core.exception.interpreter.InvalidTermException;
import junit.framework.TestCase;

public class DoubleTestCase extends TestCase {
	
	public void testIsAtomic() {
		assertTrue(new alice.tuprolog.core.data.numeric.Double(0).isAtomic());
	}
	
	public void testIsAtom() {
		assertFalse(new alice.tuprolog.core.data.numeric.Double(0).isAtom());
	}
	
	public void testIsCompound() {
		assertFalse(new alice.tuprolog.core.data.numeric.Double(0).isCompound());
	}
	
	public void testEqualsToStruct() {
		alice.tuprolog.core.data.numeric.Double zero = new alice.tuprolog.core.data.numeric.Double(0);
		Struct s = new Struct();
		assertFalse(zero.equals(s));
	}
	
	public void testEqualsToVar() throws InvalidTermException {
		alice.tuprolog.core.data.numeric.Double one = new alice.tuprolog.core.data.numeric.Double(1);
		Var x = new Var("X");
		assertFalse(one.equals(x));
	}
	
	public void testEqualsToDouble() {
		alice.tuprolog.core.data.numeric.Double zero = new alice.tuprolog.core.data.numeric.Double(0);
		alice.tuprolog.core.data.numeric.Double one = new alice.tuprolog.core.data.numeric.Double(1);
		assertFalse(zero.equals(one));
		alice.tuprolog.core.data.numeric.Double anotherZero = new alice.tuprolog.core.data.numeric.Double(0.0);
		assertTrue(anotherZero.equals(zero));
	}
	
	public void testEqualsToFloat() {
		// TODO Test Double numbers for equality with Float numbers
	}
	
	public void testEqualsToInt() {
		alice.tuprolog.core.data.numeric.Double doubleOne = new alice.tuprolog.core.data.numeric.Double(1.0);
		alice.tuprolog.core.data.numeric.Int integerOne = new alice.tuprolog.core.data.numeric.Int(1);
		assertFalse(doubleOne.equals(integerOne));
	}
	
	public void testEqualsToLong() {
		// TODO Test Double numbers for equality with Long numbers
	}

}
