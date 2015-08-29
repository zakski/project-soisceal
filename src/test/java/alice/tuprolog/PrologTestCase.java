package alice.tuprolog;

import alice.tuprolog.core.event.logging.SpyEvent;
import alice.tuprolog.util.exception.lib.InvalidLibraryException;
import alice.tuprolog.core.exception.interpreter.InvalidTheoryException;
import alice.tuprolog.core.theory.Theory;
import alice.tuprolog.event.SpyListener;

import alice.tuprolog.util.StringLibrary;
import junit.framework.TestCase;

public class PrologTestCase extends TestCase {
	
	public void testEngineInitialization() {
		alice.tuprolog.core.Prolog engine = new alice.tuprolog.core.Prolog();
		assertEquals(4, engine.getCurrentLibraries().length);
		assertNotNull(engine.getLibrary("alice.tuprolog.lib.BasicLibrary"));
		assertNotNull(engine.getLibrary("alice.tuprolog.lib.ISOLibrary"));
		assertNotNull(engine.getLibrary("alice.tuprolog.lib.IOLibrary"));
		assertNotNull(engine.getLibrary("alice.tuprolog.lib.OOLibrary"));
	}
	
	public void testLoadLibraryAsString() throws InvalidLibraryException {
		alice.tuprolog.core.Prolog engine = new alice.tuprolog.core.Prolog();
		engine.loadLibrary("alice.tuprolog.util.StringLibrary");
		assertNotNull(engine.getLibrary("alice.tuprolog.util.StringLibrary"));
	}
	
	public void testLoadLibraryAsObject() throws InvalidLibraryException {
		alice.tuprolog.core.Prolog engine = new alice.tuprolog.core.Prolog();
		Library stringLibrary = new StringLibrary();
		engine.loadLibrary(stringLibrary);
		assertNotNull(engine.getLibrary("alice.tuprolog.util.StringLibrary"));
		Library javaLibrary = new alice.tuprolog.lib.OOLibrary();
		engine.loadLibrary(javaLibrary);
		assertSame(javaLibrary, engine.getLibrary("alice.tuprolog.lib.OOLibrary"));
	}
	
	public void testGetLibraryWithName() throws InvalidLibraryException {
		alice.tuprolog.core.Prolog engine = new alice.tuprolog.core.Prolog(new String[] {"alice.tuprolog.util.TestLibrary"});
		assertNotNull(engine.getLibrary("TestLibraryName"));
	}
	
	public void testUnloadLibraryAfterLoadingTheory() throws Exception {
		alice.tuprolog.core.Prolog engine = new alice.tuprolog.core.Prolog();
		assertNotNull(engine.getLibrary("alice.tuprolog.lib.IOLibrary"));
		Theory t = new Theory("a(1).\na(2).\n");
		engine.setTheory(t);
		engine.unloadLibrary("alice.tuprolog.lib.IOLibrary");
		assertNull(engine.getLibrary("alice.tuprolog.lib.IOLibrary"));
	}
	
	public void testAddTheory() throws InvalidTheoryException {
		alice.tuprolog.core.Prolog engine = new alice.tuprolog.core.Prolog();
		Theory t = new Theory("test :- notx existing(s).");
		try {
			engine.addTheory(t);
			fail();
		} catch (InvalidTheoryException expected) {
			assertEquals("", engine.getTheory().toString());
		}
	}
	
	public void testSpyListenerManagement() {
		alice.tuprolog.core.Prolog engine = new alice.tuprolog.core.Prolog();
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
		alice.tuprolog.core.Prolog engine = new alice.tuprolog.core.Prolog(new String[]{});
		engine.loadLibrary("alice.tuprolog.lib.BasicLibrary");
		engine.loadLibrary("alice.tuprolog.lib.IOLibrary");
		TestPrologEventAdapter a = new TestPrologEventAdapter();
		engine.addLibraryListener(a);
		engine.loadLibrary("alice.tuprolog.lib.OOLibrary");
		assertEquals("alice.tuprolog.lib.OOLibrary", a.firstMessage);
		engine.unloadLibrary("alice.tuprolog.lib.OOLibrary");
		assertEquals("alice.tuprolog.lib.OOLibrary", a.firstMessage);
	}
	
	public void testTheoryListener() throws InvalidTheoryException {
		alice.tuprolog.core.Prolog engine = new alice.tuprolog.core.Prolog();
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
		alice.tuprolog.core.Prolog engine = new alice.tuprolog.core.Prolog();
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
