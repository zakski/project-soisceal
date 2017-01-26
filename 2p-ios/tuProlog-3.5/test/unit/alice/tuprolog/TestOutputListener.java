package alice.tuprolog;

import alice.tuprolog.event.OutputEvent;
import alice.tuprolog.interfaces.event.OutputListener;

class TestOutputListener implements OutputListener {
	
	public String output = "";

	public void onOutput(OutputEvent e) {
		output += e.getMsg();
	}

}
