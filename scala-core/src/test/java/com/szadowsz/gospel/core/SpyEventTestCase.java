package com.szadowsz.gospel.core;

import com.szadowsz.gospel.core.event.logging.SpyEvent;
import junit.framework.TestCase;

public class SpyEventTestCase extends TestCase {
	
	public void testToString() {
		String msg = "testConstruction";
		SpyEvent e = new SpyEvent(new Prolog(), msg);
		assertEquals(msg, e.toString());
	}

}
