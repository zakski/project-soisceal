/**
 * 
 */
package com.szadowsz.gospel.util.event;

import com.szadowsz.gospel.core.event.WarningListener;
import com.szadowsz.gospel.core.event.logging.WarningEvent;

public class TestWarningListener implements WarningListener {
	public String warning;
	public void onWarning(WarningEvent e) {
		warning = e.getMsg();
	}
}