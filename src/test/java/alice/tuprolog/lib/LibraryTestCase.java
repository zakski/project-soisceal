package alice.tuprolog.lib;

import alice.tuprolog.core.engine.Solution;
import alice.tuprolog.core.exception.PrologException;
import alice.tuprolog.util.exception.lib.InvalidLibraryException;
import alice.tuprolog.util.exception.solve.MalformedGoalException;
import alice.tuprolog.util.exception.solve.NoSolutionException;
import alice.tuprolog.util.TestLibrary;
import alice.tuprolog.util.TestOutputListener;
import junit.framework.TestCase;

public class LibraryTestCase extends TestCase {
	
	public void testLibraryFunctor() throws PrologException, InvalidLibraryException, MalformedGoalException, NoSolutionException {
		alice.tuprolog.core.Prolog engine = new alice.tuprolog.core.Prolog();
		engine.loadLibrary(new TestLibrary());
		Solution goal = engine.solve("N is sum(1, 3).");
		assertTrue(goal.isSuccess());
		assertEquals(new alice.tuprolog.core.data.numeric.Int(4), goal.getVarValue("N"));
	}
	
	public void testLibraryPredicate() throws PrologException, InvalidLibraryException, MalformedGoalException {
		alice.tuprolog.core.Prolog engine = new alice.tuprolog.core.Prolog();
		engine.loadLibrary(new TestLibrary());
		TestOutputListener l = new TestOutputListener();
		engine.addOutputListener(l);
		engine.solve("println(sum(5)).");
		assertEquals("sum(5)", l.output);
	}

}
