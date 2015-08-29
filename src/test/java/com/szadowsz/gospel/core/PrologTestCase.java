package com.szadowsz.gospel.core;

import com.szadowsz.gospel.core.event.SpyListener;
import com.szadowsz.gospel.core.event.logging.SpyEvent;
import com.szadowsz.gospel.core.exception.interpreter.InvalidTheoryException;
import com.szadowsz.gospel.core.lib.Library;
import com.szadowsz.gospel.core.lib.OOLibrary;
import com.szadowsz.gospel.core.theory.Theory;
import com.szadowsz.gospel.util.event.TestPrologEventAdapter;
import com.szadowsz.gospel.util.exception.lib.InvalidLibraryException;
import com.szadowsz.gospel.util.lib.StringLibrary;
import junit.framework.TestCase;

public class PrologTestCase extends TestCase {

    public void testEngineInitialization() {
        Prolog engine = new Prolog();
        assertEquals(4, engine.getCurrentLibraries().length);
        assertNotNull(engine.getLibrary("com.szadowsz.gospel.core.lib.BasicLibrary"));
        assertNotNull(engine.getLibrary("com.szadowsz.gospel.core.lib.ISOLibrary"));
        assertNotNull(engine.getLibrary("com.szadowsz.gospel.core.lib.IOLibrary"));
        assertNotNull(engine.getLibrary("com.szadowsz.gospel.core.lib.OOLibrary"));
    }

    public void testLoadLibraryAsString() throws InvalidLibraryException {
        Prolog engine = new Prolog();
        engine.loadLibrary("com.szadowsz.gospel.util.lib.StringLibrary");
        assertNotNull(engine.getLibrary("com.szadowsz.gospel.util.lib.StringLibrary"));
    }

    public void testLoadLibraryAsObject() throws InvalidLibraryException {
        Prolog engine = new Prolog();
        Library stringLibrary = new StringLibrary();
        engine.loadLibrary(stringLibrary);
        assertNotNull(engine.getLibrary("com.szadowsz.gospel.util.lib.StringLibrary"));
        Library javaLibrary = new OOLibrary();
        engine.loadLibrary(javaLibrary);
        assertSame(javaLibrary, engine.getLibrary("com.szadowsz.gospel.core.lib.OOLibrary"));
    }

    public void testGetLibraryWithName() throws InvalidLibraryException {
        Prolog engine = new Prolog(new String[]{"com.szadowsz.gospel.util.lib.TestLibrary"});
        assertNotNull(engine.getLibrary("TestLibraryName"));
    }

    public void testUnloadLibraryAfterLoadingTheory() throws Exception {
        Prolog engine = new Prolog();
        assertNotNull(engine.getLibrary("com.szadowsz.gospel.core.lib.IOLibrary"));
        Theory t = new Theory("a(1).\na(2).\n");
        engine.setTheory(t);
        engine.unloadLibrary("com.szadowsz.gospel.core.lib.IOLibrary");
        assertNull(engine.getLibrary("com.szadowsz.gospel.core.lib.IOLibrary"));
    }

    // TODO RE-ADD THIS
//	public void testAddTheory() throws InvalidTheoryException {
//		Prolog engine = new Prolog();
//		Theory t = new Theory("test :- notx existing(s).");
//		try {
//			engine.addTheory(t);
//			fail();
//		} catch (InvalidTheoryException expected) {
//			assertEquals("", engine.getTheory().toString());
//		}
//	}

    public void testSpyListenerManagement() {
        Prolog engine = new Prolog();
        SpyListener listener1 = new SpyListener() {
            public void onSpy(SpyEvent e) {
            }
        };
        SpyListener listener2 = new SpyListener() {
            public void onSpy(SpyEvent e) {
            }
        };
        engine.addSpyListener(listener1);
        engine.addSpyListener(listener2);
        assertEquals(2, engine.getSpyListenerList().size());
    }

    public void testLibraryListener() throws InvalidLibraryException {
        Prolog engine = new Prolog(new String[]{});
        engine.loadLibrary("com.szadowsz.gospel.core.lib.BasicLibrary");
        engine.loadLibrary("com.szadowsz.gospel.core.lib.IOLibrary");
        TestPrologEventAdapter a = new TestPrologEventAdapter();
        engine.addLibraryListener(a);
        engine.loadLibrary("com.szadowsz.gospel.core.lib.OOLibrary");
        assertEquals("com.szadowsz.gospel.core.lib.OOLibrary", a.firstMessage);
        engine.unloadLibrary("com.szadowsz.gospel.core.lib.OOLibrary");
        assertEquals("com.szadowsz.gospel.core.lib.OOLibrary", a.firstMessage);
    }

    public void testTheoryListener() throws InvalidTheoryException {
        Prolog engine = new Prolog();
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
        Prolog engine = new Prolog();
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
