package alice.tuprolog.util;

import alice.tuprolog.core.event.io.OutputEvent;
import alice.tuprolog.event.OutputListener;

public class TestOutputListener implements OutputListener {
	
	public String output = "";

	public void onOutput(OutputEvent e) {
		output += e.getMsg();
	}

}
