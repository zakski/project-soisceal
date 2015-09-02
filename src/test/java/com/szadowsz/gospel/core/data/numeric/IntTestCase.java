package com.szadowsz.gospel.core.data.numeric;

import com.szadowsz.gospel.core.data.Struct;
import com.szadowsz.gospel.core.data.Term;
import com.szadowsz.gospel.core.data.Var;
import com.szadowsz.gospel.util.exception.data.InvalidTermException;
import junit.framework.TestCase;

public class IntTestCase extends TestCase {

    public void testIsAtomic() {
        assertTrue(new Int(0).isAtomic());
    }

    public void testIsAtom() {
        assertFalse(new Int(0).isAtom());
    }

    public void testIsCompound() {
        assertFalse(new Int(0).isCompound());
    }

    public void testEqualsToStruct() {
        Term s = new Struct();
        Term zero = new Int(0);
        assertFalse(zero.equals(s));
    }

    public void testEqualsToVar() throws InvalidTermException {
        Term x = new Var("X");
        Term one = new Int(1);
        assertFalse(one.equals(x));
    }

    public void testEqualsToInt() {
        Int zero = new Int(0);
        Int one = new Int(1);
        assertFalse(zero.equals(one));
        Int anotherZero = new Int(0);
        assertTrue(anotherZero.equals(zero));
    }
}
