/**
 * 
 */
package alice.tuprolog.util;

import alice.tuprolog.core.event.logging.WarningEvent;
import alice.tuprolog.event.WarningListener;

public class TestWarningListener implements WarningListener {
	public String warning;
	public void onWarning(WarningEvent e) {
		warning = e.getMsg();
	}
}