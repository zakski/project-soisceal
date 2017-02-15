package com.szadowsz.gospel.util.event;

import com.szadowsz.gospel.core.event.OutputListener;
import com.szadowsz.gospel.core.event.io.OutputEvent;

public class TestOutputListener implements OutputListener {
	
	public String output = "";

	public void onOutput(OutputEvent e) {
		output += e.getMsg();
	}

}
