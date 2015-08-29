package alice.tuprolog;

import alice.tuprolog.core.event.logging.SpyEvent;
import junit.framework.TestCase;

public class SpyEventTestCase extends TestCase {
	
	public void testToString() {
		String msg = "testConstruction";
		SpyEvent e = new SpyEvent(new alice.tuprolog.core.Prolog(), msg);
		assertEquals(msg, e.toString());
	}

}
