package alice.tuprolog.exception;

import alice.tuprolog.core.data.Struct;
import alice.tuprolog.core.data.Var;
import alice.tuprolog.core.engine.Solution;
import junit.framework.TestCase;

/**
 * @author Matteo Iuliani
 * 
 *         Test del funzionamento delle eccezioni lanciate dai predicati della ISOLibrary
 */
public class ISOLibraryExceptionsTestCase extends TestCase {

	// verifico che atom_length(X, Y) lancia un errore di instanziazione
	public void test_atom_length_2_1() throws Exception {
		alice.tuprolog.core.Prolog engine = new alice.tuprolog.core.Prolog();
		String goal = "catch(atom_length(X, Y), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("atom_length", new Var("X"), new Var("Y"))));
		alice.tuprolog.core.data.numeric.Int argNo = (alice.tuprolog.core.data.numeric.Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
	}

	// verifico che atom_length(1, Y) lancia un errore di tipo
	public void test_atom_length_2_2() throws Exception {
		alice.tuprolog.core.Prolog engine = new alice.tuprolog.core.Prolog();
		String goal = "catch(atom_length(1, Y), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("atom_length", new alice.tuprolog.core.data.numeric.Int(1), new Var("Y"))));
		alice.tuprolog.core.data.numeric.Int argNo = (alice.tuprolog.core.data.numeric.Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("atom")));
		alice.tuprolog.core.data.numeric.Int culprit = (alice.tuprolog.core.data.numeric.Int) info.getTerm("Culprit");
		assertTrue(culprit.intValue() == 1);
	}
	
	// verifico che atom_chars(1, X) lancia un errore di tipo
	public void test_atom_chars_2_1() throws Exception {
		alice.tuprolog.core.Prolog engine = new alice.tuprolog.core.Prolog();
		String goal = "catch(atom_chars(1, X), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("atom_chars", new alice.tuprolog.core.data.numeric.Int(1), new Var("X"))));
		alice.tuprolog.core.data.numeric.Int argNo = (alice.tuprolog.core.data.numeric.Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("atom")));
		alice.tuprolog.core.data.numeric.Int culprit = (alice.tuprolog.core.data.numeric.Int) info.getTerm("Culprit");
		assertTrue(culprit.intValue() == 1);
	}
	
	// verifico che atom_chars(X, a) lancia un errore di tipo
	public void test_atom_chars_2_2() throws Exception {
		alice.tuprolog.core.Prolog engine = new alice.tuprolog.core.Prolog();
		String goal = "catch(atom_chars(X, a), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("atom_chars", new Var("X"), new Struct("a"))));
		alice.tuprolog.core.data.numeric.Int argNo = (alice.tuprolog.core.data.numeric.Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("list")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}
	
	// verifico che char_code(ab, X) lancia un errore di tipo
	public void test_char_code_2_1() throws Exception {
		alice.tuprolog.core.Prolog engine = new alice.tuprolog.core.Prolog();
		String goal = "catch(char_code(ab, X), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("char_code", new Struct("ab"), new Var("X"))));
		alice.tuprolog.core.data.numeric.Int argNo = (alice.tuprolog.core.data.numeric.Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("character")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("ab")));
	}
	
	// verifico che char_code(X, a) lancia un errore di tipo
	public void test_char_code_2_2() throws Exception {
		alice.tuprolog.core.Prolog engine = new alice.tuprolog.core.Prolog();
		String goal = "catch(char_code(X, a), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("char_code", new Var("X"), new Struct("a"))));
		alice.tuprolog.core.data.numeric.Int argNo = (alice.tuprolog.core.data.numeric.Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("integer")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}
	
	// verifico che sub_atom(1, B, C, D, E) lancia un errore di tipo
	public void test_sub_atom_5_2() throws Exception {
		alice.tuprolog.core.Prolog engine = new alice.tuprolog.core.Prolog();
		String goal = "catch(sub_atom(1, B, C, D, E), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("sub_atom_guard", new alice.tuprolog.core.data.numeric.Int(1), new Var("B"),  new Var("C"),  new Var("D"),  new Var("E"))));
		alice.tuprolog.core.data.numeric.Int argNo = (alice.tuprolog.core.data.numeric.Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("atom")));
		alice.tuprolog.core.data.numeric.Int culprit = (alice.tuprolog.core.data.numeric.Int) info.getTerm("Culprit");
		assertTrue(culprit.intValue() == 1);
	}

}