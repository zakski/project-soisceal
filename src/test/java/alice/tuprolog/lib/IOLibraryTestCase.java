package alice.tuprolog.lib;

import java.util.List;
import java.util.Map;

import alice.tuprolog.Library;
import alice.tuprolog.util.TestOutputListener;
import alice.tuprolog.util.exception.solve.MalformedGoalException;
import junit.framework.TestCase;

public class IOLibraryTestCase extends TestCase {
	
	public void testGetPrimitives() {
		Library library = new IOLibrary();
		Map<Integer, List<alice.tuprolog.core.lib.PrimitiveInfo>> primitives = library.getPrimitives();
		assertEquals(3, primitives.size());
		assertEquals(0, primitives.get(alice.tuprolog.core.lib.PrimitiveInfo.DIRECTIVE()).size());
		assertTrue(primitives.get(alice.tuprolog.core.lib.PrimitiveInfo.PREDICATE()).size() > 0);
		assertEquals(0, primitives.get(alice.tuprolog.core.lib.PrimitiveInfo.FUNCTOR()).size());
	}
	
	public void testTab1() throws MalformedGoalException {
		alice.tuprolog.core.Prolog engine = new alice.tuprolog.core.Prolog();
		TestOutputListener l = new TestOutputListener();
		engine.addOutputListener(l);
		engine.solve("tab(5).");
		assertEquals("     ", l.output);
	}

}
