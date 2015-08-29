package alice.tuprolog;

import alice.tuprolog.core.exception.interpreter.InvalidTheoryException;
import alice.tuprolog.util.exception.solve.MalformedGoalException;
import alice.tuprolog.core.theory.Theory;
import alice.tuprolog.util.TestWarningListener;
import junit.framework.TestCase;

public class StateRuleSelectionTestCase extends TestCase {
	
	public void testUnknownPredicateInQuery() throws MalformedGoalException {
		alice.tuprolog.core.Prolog engine = new alice.tuprolog.core.Prolog();
		TestWarningListener warningListener = new TestWarningListener();
		engine.addWarningListener(warningListener);
		String query = "p(X).";
		engine.solve(query);
		assertTrue(warningListener.warning.indexOf("p/1") > 0);
		assertTrue(warningListener.warning.indexOf("is unknown") > 0);
	}
	
	public void testUnknownPredicateInTheory() throws InvalidTheoryException, MalformedGoalException {
		alice.tuprolog.core.Prolog engine = new alice.tuprolog.core.Prolog();
		TestWarningListener warningListener = new TestWarningListener();
		engine.addWarningListener(warningListener);
		String theory = "p(X) :- a, b. \nb.";
		engine.setTheory(new Theory(theory));
		String query = "p(X).";
		engine.solve(query);
		assertTrue(warningListener.warning.indexOf("a/0") > 0);
		assertTrue(warningListener.warning.indexOf("is unknown") > 0);
	}

}
