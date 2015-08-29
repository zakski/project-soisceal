package alice.tuprolog;

import java.util.List;


import alice.tuprolog.core.data.Struct;
import alice.tuprolog.core.engine.Solution;
import alice.tuprolog.core.theory.*;
import alice.tuprolog.core.theory.clause.ClauseInfo;
import alice.tuprolog.core.exception.PrologException;
import alice.tuprolog.core.exception.interpreter.InvalidTheoryException;
import alice.tuprolog.util.exception.solve.MalformedGoalException;
import alice.tuprolog.util.exception.solve.NoMoreSolutionsException;
import alice.tuprolog.util.exception.solve.NoSolutionException;
import alice.tuprolog.util.TestOutputListener;
import alice.tuprolog.util.TestWarningListener;
import junit.framework.TestCase;

public class TheoryManagerTestCase extends TestCase {

	public void testUnknownDirective() throws InvalidTheoryException {
		String theory = ":- unidentified_directive(unknown_argument).";
		alice.tuprolog.core.Prolog engine = new alice.tuprolog.core.Prolog();
		TestWarningListener warningListener = new TestWarningListener();
		engine.addWarningListener(warningListener);
		engine.setTheory(new Theory(theory));
		assertTrue(warningListener.warning.indexOf("unidentified_directive/1") > 0);
		assertTrue(warningListener.warning.indexOf("is unknown") > 0);
	}

	public void testFailedDirective() throws InvalidTheoryException {
		String theory = ":- load_library('UnknownLibrary').";
		alice.tuprolog.core.Prolog engine = new alice.tuprolog.core.Prolog();
		TestWarningListener warningListener = new TestWarningListener();
		engine.addWarningListener(warningListener);
		engine.setTheory(new Theory(theory));
		assertTrue(warningListener.warning.indexOf("load_library/1") > 0);
		assertTrue(warningListener.warning.indexOf("InvalidLibraryException") > 0);
	}

	public void testAssertNotBacktrackable() throws PrologException, MalformedGoalException {
		alice.tuprolog.core.Prolog engine = new alice.tuprolog.core.Prolog();
		Solution firstSolution = engine.solve("assertz(a(z)).");
		assertTrue(firstSolution.isSuccess());
		assertFalse(firstSolution.hasOpenAlternatives());
	}

	public void testAbolish() throws PrologException, InvalidTheoryException {
		alice.tuprolog.core.Prolog engine = new alice.tuprolog.core.Prolog();
		String theory = "test(A, B) :- A is 1+2, B is 2+3.";
		engine.setTheory(new Theory(theory));
		TheoryManager manager = engine.getTheoryManager();
		Struct testTerm = new Struct("test", new Struct("a"), new Struct("b"));
		List<ClauseInfo> testClauses = scala.collection.JavaConversions.seqAsJavaList(manager.find(testTerm));
		assertEquals(1, testClauses.size());
		manager.abolish(new Struct("/", new Struct("test"), new alice.tuprolog.core.data.numeric.Int(2)));
		testClauses = scala.collection.JavaConversions.seqAsJavaList(manager.find(testTerm));
		// The predicate should also disappear completely from the clause
		// database, i.e. ClauseDatabase#get(f/a) should return null
		assertEquals(0, testClauses.size());
	}

	public void testAbolish2() throws InvalidTheoryException, MalformedGoalException {
		alice.tuprolog.core.Prolog engine = new alice.tuprolog.core.Prolog();
		engine.setTheory(new Theory("fact(new).\n" +
									"fact(other).\n"));

		Solution info = engine.solve("abolish(fact/1).");
		assertTrue(info.isSuccess());
		info = engine.solve("fact(V).");
		assertFalse(info.isSuccess());
	}
	
	// Based on the bugs 65 and 66 on sourceforge
	public void testRetractall() throws MalformedGoalException, NoSolutionException, NoMoreSolutionsException {
		alice.tuprolog.core.Prolog engine = new alice.tuprolog.core.Prolog();
		Solution info = engine.solve("assert(takes(s1,c2)), assert(takes(s1,c3)).");
		assertTrue(info.isSuccess());
		info = engine.solve("takes(s1, N).");
		assertTrue(info.isSuccess());
		assertTrue(info.hasOpenAlternatives());
		assertEquals("c2", info.getVarValue("N").toString());
		info = engine.solveNext();
		assertTrue(info.isSuccess());
		assertEquals("c3", info.getVarValue("N").toString());
		
		info = engine.solve("retractall(takes(s1,c2)).");
		assertTrue(info.isSuccess());
		info = engine.solve("takes(s1, N).");
		assertTrue(info.isSuccess());
		assertFalse(info.hasOpenAlternatives());
		assertEquals("c3", info.getVarValue("N").toString());
	}

	// TODO test retractall: ClauseDatabase#get(f/a) should return an
	// empty list
	
	public void testRetract() throws InvalidTheoryException, MalformedGoalException {
		alice.tuprolog.core.Prolog engine = new alice.tuprolog.core.Prolog();
		TestOutputListener listener = new TestOutputListener();
		engine.addOutputListener(listener);
		engine.setTheory(new Theory("insect(ant). insect(bee)."));
		Solution info = engine.solve("retract(insect(I)), write(I), retract(insect(bee)), fail.");
		assertFalse(info.isSuccess());
		assertEquals("antbee", listener.output);
		
	}

}
