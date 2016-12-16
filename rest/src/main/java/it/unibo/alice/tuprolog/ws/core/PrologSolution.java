package it.unibo.alice.tuprolog.ws.core;

import java.util.ArrayList;
import java.util.List;

import alice.tuprolog.SolveInfo;

/**
 * @author Andrea Muccioli
 *
 */
public class PrologSolution {
	
	private List<SolveInfo> info;
	private String engineState;
	
	
	
	public PrologSolution(SolveInfo info, String state) {
		this.info = new ArrayList<SolveInfo>();
		this.info.add(info);
		this.engineState = state;
	}
	
	public PrologSolution(List<SolveInfo> info, String state) {
		this.info = info;
		this.engineState = state;
	}
	
	
	public List<SolveInfo> getInfo() {
		return info;
	}
	public void setInfo(List<SolveInfo> info) {
		this.info = info;
	}
	public SolveInfo getFirstInfo() {
		return info.get(0);
	}
	public void addInfo(SolveInfo info) {
		this.info.add(info);
	}
	public String getEngineState() {
		return engineState;
	}
	public void setEngineStatus(String engineState) {
		this.engineState = engineState;
	}
	
}
