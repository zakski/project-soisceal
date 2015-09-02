package com.szadowsz.gospel.core.lib;

import com.szadowsz.gospel.core.Prolog;
import com.szadowsz.gospel.core.engine.Solution;
import com.szadowsz.gospel.core.exception.interpreter.InvalidTheoryException;
import com.szadowsz.gospel.core.theory.Theory;
import com.szadowsz.gospel.util.exception.solution.InvalidSolutionException;
import junit.framework.TestCase;

public class BuiltInTestCase extends TestCase {
	
//	public void testConvertTermToGoal() throws InvalidTermException {
//		Term t = new Var("T");
//		Struct result = Struct.build("call", t);
//		assertEquals(result, BuiltIn.convertTermToGoal(t));
//		assertEquals(result, BuiltIn.convertTermToGoal(Struct.build("call", t)));
//
//		t = new Int(2);
//		assertNull(BuiltIn.convertTermToGoal(t));
//
//		t = Struct.build("p", Struct.build("a"), new Var("B"), Struct.build("c"));
//		result = (Struct) t;
//		assertEquals(result, BuiltIn.convertTermToGoal(t));
//
//		Var linked = new Var("X");
//		linked.setLink(Struct.build("!"));
//		Term[] arguments = new Term[] { linked, new Var("Y") };
//		Term[] results = new Term[] { Struct.build("!"), Struct.build("call", new Var("Y")) };
//		assertEquals(Struct.build(";", results), BuiltIn.convertTermToGoal(Struct.build(";", arguments)));
//		assertEquals(Struct.build(",", results), BuiltIn.convertTermToGoal(Struct.build(",", arguments)));
//		assertEquals(Struct.build("->", results), BuiltIn.convertTermToGoal(Struct.build("->", arguments)));
//	}
	
	//Based on the bug #59 Grouping conjunctions in () changes result on sourceforge
	public void testGroupingConjunctions() throws InvalidTheoryException, InvalidSolutionException {
		Prolog engine = new Prolog();
		engine.setTheory(new Theory("g1. g2."));
		Solution info = engine.solve("(g1, g2), (g3, g4).");
		assertFalse(info.isSuccess());
		engine.setTheory(new Theory("g1. g2. g3. g4."));
		info = engine.solve("(g1, g2), (g3, g4).");
		assertTrue(info.isSuccess());
	}

}
