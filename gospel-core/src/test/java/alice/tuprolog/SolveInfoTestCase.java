package alice.tuprolog;

import com.szadowsz.gospel.core.PrologEngine;
import junit.framework.TestCase;

public class SolveInfoTestCase extends TestCase {

	public void testGetSubsequentQuery() {
		PrologEngine engine = new PrologEngine();
		Term query = new Struct("is", new Var("X"), new Struct("+", new Int(1), new Int(2)));
		SolveInfo result = engine.solve(query);
		assertTrue(result.isSuccess());
		assertEquals(query, result.getQuery());
		query = new Struct("functor", new Struct("p"), new Var("Name"), new Var("Arity"));
		result = engine.solve(query);
		assertTrue(result.isSuccess());
		assertEquals(query, result.getQuery());
	}

}
