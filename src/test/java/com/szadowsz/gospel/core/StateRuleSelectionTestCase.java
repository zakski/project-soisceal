package com.szadowsz.gospel.core;

import com.szadowsz.gospel.util.exception.theory.InvalidTheoryException;
import com.szadowsz.gospel.core.db.theory.Theory;
import com.szadowsz.gospel.util.exception.solution.InvalidSolutionException;
import junit.framework.TestCase;

/** TODO fix tests */
public class StateRuleSelectionTestCase extends TestCase {
	
	public void testUnknownPredicateInQuery() throws InvalidSolutionException {
		Prolog engine = new Prolog();
//		TestWarningListener warningListener = new TestWarningListener();
		//engine.addWarningListener(warningListener);
		String query = "p(X).";
		engine.solve(query);
//		assertTrue(warningListener.warning.indexOf("p/1") > 0);
//		assertTrue(warningListener.warning.indexOf("is unknown") > 0);
	}
	
	public void testUnknownPredicateInTheory() throws InvalidTheoryException, InvalidSolutionException {
		Prolog engine = new Prolog();
//		TestWarningListener warningListener = new TestWarningListener();
	//	engine.addWarningListener(warningListener);
		String theory = "p(X) :- a, b. \nb.";
		engine.setTheory(new Theory(theory));
		String query = "p(X).";
		engine.solve(query);
	//	assertTrue(warningListener.warning.indexOf("a/0") > 0);
	//	assertTrue(warningListener.warning.indexOf("is unknown") > 0);
	}

}
