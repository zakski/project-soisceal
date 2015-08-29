package alice.tuprolog.data;

import alice.tuprolog.core.data.Struct;
import alice.tuprolog.core.data.Var;
import alice.tuprolog.core.exception.interpreter.InvalidTermException;
import junit.framework.TestCase;

public class IntTestCase extends TestCase {
	
	public void testIsAtomic() {
		assertTrue(new alice.tuprolog.core.data.numeric.Int(0).isAtomic());
	}
	
	public void testIsAtom() {
		assertFalse(new alice.tuprolog.core.data.numeric.Int(0).isAtom());
	}
	
	public void testIsCompound() {
		assertFalse(new alice.tuprolog.core.data.numeric.Int(0).isCompound());
	}
	
	public void testEqualsToStruct() {
		Struct s = new Struct();
		alice.tuprolog.core.data.numeric.Int zero = new alice.tuprolog.core.data.numeric.Int(0);
		assertFalse(zero.equals(s));
	}
	
	public void testEqualsToVar() throws InvalidTermException {
		Var x = new Var("X");
		alice.tuprolog.core.data.numeric.Int one = new alice.tuprolog.core.data.numeric.Int(1);
		assertFalse(one.equals(x));
	}
	
	public void testEqualsToInt() {
		alice.tuprolog.core.data.numeric.Int zero = new alice.tuprolog.core.data.numeric.Int(0);
		alice.tuprolog.core.data.numeric.Int one = new alice.tuprolog.core.data.numeric.Int(1);
		assertFalse(zero.equals(one));
		alice.tuprolog.core.data.numeric.Int anotherZero = new alice.tuprolog.core.data.numeric.Int(1-1);
		assertTrue(anotherZero.equals(zero));
	}
	
	public void testEqualsToLong() {
		// TODO Test Int numbers for equality with Long numbers
	}
	
	public void testEqualsToDouble() {
		alice.tuprolog.core.data.numeric.Int integerOne = new alice.tuprolog.core.data.numeric.Int(1);
		alice.tuprolog.core.data.numeric.Double doubleOne = new alice.tuprolog.core.data.numeric.Double(1);
		assertFalse(integerOne.equals(doubleOne));
	}
	
	public void testEqualsToFloat() {
		// TODO Test Int numbers for equality with Float numbers
	}

}
