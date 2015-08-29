package alice.tuprolog;

import alice.tuprolog.core.data.Struct;
import alice.tuprolog.core.data.Term;
import alice.tuprolog.core.data.Var;
import alice.tuprolog.core.data.numeric.Int;
import alice.tuprolog.core.engine.Solution;
import junit.framework.TestCase;

public class SolveInfoTestCase extends TestCase {

	public void testGetSubsequentQuery() {
		alice.tuprolog.core.Prolog engine = new alice.tuprolog.core.Prolog();
		Term query = new Struct("is", new Var("X"), new Struct("+", new Int(1), new Int(2)));
		Solution result = engine.solve(query);
		assertTrue(result.isSuccess());
		assertEquals(query, result.getQuery());
		query = new Struct("functor", new Struct("p"), new Var("Name"), new Var("Arity"));
		result = engine.solve(query);
		assertTrue(result.isSuccess());
		assertEquals(query, result.getQuery());
	}

}
