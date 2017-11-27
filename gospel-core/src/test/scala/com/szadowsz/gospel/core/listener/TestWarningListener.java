/**
 * 
 */
package com.szadowsz.gospel.core.listener;

import com.szadowsz.gospel.core.event.interpreter.WarningEvent;
import com.szadowsz.gospel.core.listener.WarningListener;

public class TestWarningListener implements WarningListener {
	public String warning;
	public void onWarning(WarningEvent e) {
		warning = e.getMsg();
	}
}