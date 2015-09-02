package com.szadowsz.gospel.core.lib;

import com.szadowsz.gospel.core.Prolog;
import com.szadowsz.gospel.util.event.TestOutputListener;
import com.szadowsz.gospel.util.exception.solution.InvalidSolutionException;
import junit.framework.TestCase;

import java.util.List;
import java.util.Map;

public class IOLibraryTestCase extends TestCase {
	
	public void testGetPrimitives() {
		Library library = new IOLibrary();
		Map<Integer, List<PrimitiveInfo>> primitives = library.getPrimitives();
		assertEquals(3, primitives.size());
		assertEquals(0, primitives.get(PrimitiveInfo.DIRECTIVE()).size());
		assertTrue(primitives.get(PrimitiveInfo.PREDICATE()).size() > 0);
		assertEquals(0, primitives.get(PrimitiveInfo.FUNCTOR()).size());
	}
	
	public void testTab1() throws InvalidSolutionException {
		Prolog engine = new Prolog();
		TestOutputListener l = new TestOutputListener();
		engine.addOutputListener(l);
		engine.solve("tab(5).");
		assertEquals("     ", l.output);
	}

}
