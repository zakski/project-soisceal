package alice.tuprolog;

import com.szadowsz.gospel.core.PrologEngine;
import junit.framework.TestCase;
import alice.tuprolog.event.*;

public class SpyEventTestCase extends TestCase {
	
	public void testToString() {
		String msg = "testConstruction";
		SpyEvent e = new SpyEvent(new PrologEngine(), msg);
		assertEquals(msg, e.toString());
	}

}
