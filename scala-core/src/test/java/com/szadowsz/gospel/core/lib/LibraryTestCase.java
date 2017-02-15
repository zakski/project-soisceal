package com.szadowsz.gospel.core.lib;

import com.szadowsz.gospel.core.Prolog;
import com.szadowsz.gospel.core.data.numeric.Int;
import com.szadowsz.gospel.core.engine.Solution;
import com.szadowsz.gospel.util.exception.engine.PrologException;
import com.szadowsz.gospel.util.event.TestOutputListener;
import com.szadowsz.gospel.util.exception.lib.InvalidLibraryException;
import com.szadowsz.gospel.util.exception.solution.InvalidSolutionException;
import com.szadowsz.gospel.util.lib.TestLibrary;
import junit.framework.TestCase;

public class LibraryTestCase extends TestCase {
	
	public void testLibraryFunctor() throws PrologException, InvalidLibraryException, InvalidSolutionException {
		Prolog engine = new Prolog();
		engine.loadLibrary(new TestLibrary());
		Solution goal = engine.solve("N is sum(1, 3).");
		assertTrue(goal.isSuccess());
		assertEquals(new Int(4), goal.getVarValue("N"));
	}
	
	public void testLibraryPredicate() throws PrologException, InvalidLibraryException, InvalidSolutionException {
		Prolog engine = new Prolog();
		engine.loadLibrary(new TestLibrary());
		TestOutputListener l = new TestOutputListener();
		engine.addOutputListener(l);
		engine.solve("println(sum(5)).");
		assertEquals("sum(5)", l.output);
	}

}
