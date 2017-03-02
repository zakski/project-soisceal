package alice.tuprolog;

import com.szadowsz.gospel.core.listener.SpyListener;

import com.szadowsz.gospel.core.PrologEngine;
import com.szadowsz.gospel.core.Theory;
import com.szadowsz.gospel.core.event.interpreter.SpyEvent;
import junit.framework.TestCase;

public class PrologTestCase extends TestCase {
	
	public void testGetLibraryWithName() throws InvalidLibraryException {
		PrologEngine engine = new PrologEngine(new String[] {"alice.tuprolog.TestLibrary"});
		assertNotNull(engine.getLibrary("TestLibraryName"));
	}
	
	public void testUnloadLibraryAfterLoadingTheory() throws Exception {
		PrologEngine engine = new PrologEngine();
		assertNotNull(engine.getLibrary("alice.tuprolog.lib.IOLibrary"));
		Theory t = new Theory("a(1).\na(2).\n");
		engine.setTheory(t);
		engine.unloadLibrary("alice.tuprolog.lib.IOLibrary");
		assertNull(engine.getLibrary("alice.tuprolog.lib.IOLibrary"));
	}
	
	public void testAddTheory() throws InvalidTheoryException {
		PrologEngine engine = new PrologEngine();
		Theory t = new Theory("test :- notx existing(s).");
		try {
			engine.addTheory(t);
			fail();
		} catch (InvalidTheoryException expected) {
			assertEquals("", engine.getTheory().toString());
		}
	}
	
	public void testSpyListenerManagement() {
		PrologEngine engine = new PrologEngine();
		SpyListener listener1 = new SpyListener() {
			public void onSpy(SpyEvent e) {}
		};
		SpyListener listener2 = new SpyListener() {
			public void onSpy(SpyEvent e) {}
		};
		engine.addSpyListener(listener1);
		engine.addSpyListener(listener2);
		assertEquals(2, engine.getSpyListenerList().size());
	}
	
	public void testLibraryListener() throws InvalidLibraryException {
		PrologEngine engine = new PrologEngine(new String[]{});
		engine.loadLibrary("alice.tuprolog.lib.BasicLibrary");
		engine.loadLibrary("alice.tuprolog.lib.IOLibrary");
		TestPrologEventAdapter a = new TestPrologEventAdapter();
		engine.addLibraryListener(a);
		engine.loadLibrary("alice.tuprolog.lib.JavaLibrary");
		assertEquals("alice.tuprolog.lib.JavaLibrary", a.firstMessage);
		engine.unloadLibrary("alice.tuprolog.lib.JavaLibrary");
		assertEquals("alice.tuprolog.lib.JavaLibrary", a.firstMessage);
	}
	
	public void testTheoryListener() throws InvalidTheoryException {
		PrologEngine engine = new PrologEngine();
		TestPrologEventAdapter a = new TestPrologEventAdapter();
		engine.addTheoryListener(a);
		Theory t = new Theory("a(1).\na(2).\n");
		engine.setTheory(t);
		assertEquals("", a.firstMessage);
		assertEquals("a(1).\n\na(2).\n\n", a.secondMessage);
		t = new Theory("a(3).\na(4).\n");
		engine.addTheory(t);
		assertEquals("a(1).\n\na(2).\n\n", a.firstMessage);
		assertEquals("a(1).\n\na(2).\n\na(3).\n\na(4).\n\n", a.secondMessage);
	}
	
	public void testQueryListener() throws Exception {
		PrologEngine engine = new PrologEngine();
		TestPrologEventAdapter a = new TestPrologEventAdapter();
		engine.addQueryListener(a);
		engine.setTheory(new Theory("a(1).\na(2).\n"));
		engine.solve("a(X).");
		assertEquals("a(X)", a.firstMessage);
		assertEquals("yes.\nX / 1", a.secondMessage);
		engine.solveNext();
		assertEquals("a(X)", a.firstMessage);
		assertEquals("yes.\nX / 2", a.secondMessage);
	}

}
