/**
 * 
 */
package alice.tuprolog;

import alice.tuprolog.event.WarningEvent;
import alice.tuprolog.interfaces.event.WarningListener;

class TestWarningListener implements WarningListener {
	public String warning;
	public void onWarning(WarningEvent e) {
		warning = e.getMsg();
	}
}