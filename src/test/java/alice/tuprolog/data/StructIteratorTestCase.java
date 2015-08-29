package alice.tuprolog.data;

import java.util.NoSuchElementException;

import alice.tuprolog.core.data.Struct;
import alice.tuprolog.core.data.Term;
import junit.framework.TestCase;
import scala.collection.Iterator;

/**
 * 
 * @author <a href="mailto:giulio.piancastelli@unibo.it">Giulio Piancastelli</a>
 */
public class StructIteratorTestCase extends TestCase {
	
	public void testEmptyIterator() {
		Struct list = new Struct();
		Iterator<? extends Term> i = list.iterator();
		assertFalse(i.hasNext());
		try {
			i.next();
			fail();
		} catch (NoSuchElementException expected) {}
	}
	
	public void testIteratorCount() {
		Struct list = new Struct(new Term[] {new alice.tuprolog.core.data.numeric.Int(1), new alice.tuprolog.core.data.numeric.Int(2), new alice.tuprolog.core.data.numeric.Int(3), new alice.tuprolog.core.data.numeric.Int(5), new alice.tuprolog.core.data.numeric.Int(7)});
		Iterator<? extends Term> i = list.iterator();
		int count = 0;
		for (; i.hasNext(); count++)
			i.next();
		assertEquals(5, count);
		assertFalse(i.hasNext());
	}
	
	public void testMultipleHasNext() {
		Struct list = new Struct(new Term[] {new Struct("p"), new Struct("q"), new Struct("r")});
		Iterator<? extends Term> i = list.iterator();
		assertTrue(i.hasNext());
		assertTrue(i.hasNext());
		assertTrue(i.hasNext());
		assertEquals(new Struct("p"), i.next());
	}
	
	public void testMultipleNext() {
		Struct list = new Struct(new Term[] {new alice.tuprolog.core.data.numeric.Int(0), new alice.tuprolog.core.data.numeric.Int(1), new alice.tuprolog.core.data.numeric.Int(2), new alice.tuprolog.core.data.numeric.Int(3), new alice.tuprolog.core.data.numeric.Int(5), new alice.tuprolog.core.data.numeric.Int(7)});
		Iterator<? extends Term> i = list.iterator();
		assertTrue(i.hasNext());
		i.next(); // skip the first term
		assertEquals(new alice.tuprolog.core.data.numeric.Int(1), i.next());
		assertEquals(new alice.tuprolog.core.data.numeric.Int(2), i.next());
		assertEquals(new alice.tuprolog.core.data.numeric.Int(3), i.next());
		assertEquals(new alice.tuprolog.core.data.numeric.Int(5), i.next());
		assertEquals(new alice.tuprolog.core.data.numeric.Int(7), i.next());
		// no more terms
		assertFalse(i.hasNext());
		try {
			i.next();
			fail();
		} catch (NoSuchElementException expected) {}
	}
	
//	public void testRemoveOperationNotSupported() {
//		Struct list = new Struct(new alice.tuprolog.core.data.numeric.Int(1), new Struct());
//		Iterator<? extends Term> i = list.iterator();
//        assertNotNull(i.next());
//		try {
//			i.remove();
//			fail();
//		} catch (UnsupportedOperationException expected) {}
//	}

}
