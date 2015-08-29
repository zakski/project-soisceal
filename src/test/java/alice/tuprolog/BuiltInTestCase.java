package alice.tuprolog;

import alice.tuprolog.core.data.Struct;
import alice.tuprolog.core.data.Term;
import alice.tuprolog.core.data.Var;
import alice.tuprolog.core.engine.Solution;
import alice.tuprolog.core.exception.interpreter.InvalidTermException;
import alice.tuprolog.core.exception.interpreter.InvalidTheoryException;
import alice.tuprolog.util.exception.solve.MalformedGoalException;
import alice.tuprolog.core.theory.Theory;
import junit.framework.TestCase;

public class BuiltInTestCase extends TestCase {
	
	public void testConvertTermToGoal() throws InvalidTermException {
		Term t = new Var("T");
		Struct result = new Struct("call", t);
		assertEquals(result, BuiltIn.convertTermToGoal(t));
		assertEquals(result, BuiltIn.convertTermToGoal(new Struct("call", t)));
		
		t = new alice.tuprolog.core.data.numeric.Int(2);
		assertNull(BuiltIn.convertTermToGoal(t));
		
		t = new Struct("p", new Struct("a"), new Var("B"), new Struct("c"));
		result = (Struct) t;
		assertEquals(result, BuiltIn.convertTermToGoal(t));
		
		Var linked = new Var("X");
		linked.setLink(new Struct("!"));
		Term[] arguments = new Term[] { linked, new Var("Y") };
		Term[] results = new Term[] { new Struct("!"), new Struct("call", new Var("Y")) };
		assertEquals(new Struct(";", results), BuiltIn.convertTermToGoal(new Struct(";", arguments)));
		assertEquals(new Struct(",", results), BuiltIn.convertTermToGoal(new Struct(",", arguments)));
		assertEquals(new Struct("->", results), BuiltIn.convertTermToGoal(new Struct("->", arguments)));
	}
	
	//Based on the bug #59 Grouping conjunctions in () changes result on sourceforge
	public void testGroupingConjunctions() throws InvalidTheoryException, MalformedGoalException {
		alice.tuprolog.core.Prolog engine = new alice.tuprolog.core.Prolog();
		engine.setTheory(new Theory("g1. g2."));
		Solution info = engine.solve("(g1, g2), (g3, g4).");
		assertFalse(info.isSuccess());
		engine.setTheory(new Theory("g1. g2. g3. g4."));
		info = engine.solve("(g1, g2), (g3, g4).");
		assertTrue(info.isSuccess());
	}

}
