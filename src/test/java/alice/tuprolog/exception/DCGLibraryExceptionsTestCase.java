package alice.tuprolog.exception;

import alice.tuprolog.core.data.Struct;
import alice.tuprolog.core.data.Var;
import alice.tuprolog.core.engine.Solution;
import junit.framework.TestCase;

/**
 * @author Matteo Iuliani
 * 
 *         Test del funzionamento delle eccezioni lanciate dai predicati della
 *         DCGLibrary
 */
public class DCGLibraryExceptionsTestCase extends TestCase {

	// verifico che phrase(X, []) lancia un errore di instanziazione
	public void test_phrase_2_1() throws Exception {
		alice.tuprolog.core.Prolog engine = new alice.tuprolog.core.Prolog();
		engine.loadLibrary("alice.tuprolog.lib.DCGLibrary");
		String goal = "catch(phrase(X, []), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("phrase_guard", new Var("X"),
				new Struct())));
		alice.tuprolog.core.data.numeric.Int argNo = (alice.tuprolog.core.data.numeric.Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
	}

	// verifico che phrase(X, [], []) lancia un errore di instanziazione
	public void test_phrase_3_1() throws Exception {
		alice.tuprolog.core.Prolog engine = new alice.tuprolog.core.Prolog();
		engine.loadLibrary("alice.tuprolog.lib.DCGLibrary");
		String goal = "catch(phrase(X, [], []), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("phrase_guard", new Var("X"),
				new Struct(), new Struct())));
		alice.tuprolog.core.data.numeric.Int argNo = (alice.tuprolog.core.data.numeric.Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
	}

}