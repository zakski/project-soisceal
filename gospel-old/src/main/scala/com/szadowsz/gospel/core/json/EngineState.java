package com.szadowsz.gospel.core.json;

import java.util.ArrayList;
import java.util.LinkedList;

import com.szadowsz.gospel.core.db.ops.Operator;
import com.szadowsz.gospel.core.data.Term;

//Alberto
public class EngineState {
	private Term query;
	private int nAskedResults;
	private boolean hasOpenAlternatives;

	private long serializationTimestamp;

	private String[] libraries;
	private ArrayList<String> flags;
	
	private String dynTheory;
	
	private LinkedList<Operator> op;
	
	
	public void setQuery(Term query) {
		this.query = query;
	}
	
	
	public Term getQuery(){
		return this.query;
	}

	
	public void setNumberAskedResults(int nResultAsked) {
		this.nAskedResults = nResultAsked;
	}
	
	
	public int getNumberAskedResults(){
		return this.nAskedResults;
	}
	
	public void setLibraries(String[] libraries){
		this.libraries = libraries;
	}

	public String[] getLibraries() {
		return this.libraries;
	}

	
	public void setHasOpenAlternatives(boolean hasOpenAlternatives) {
		this.hasOpenAlternatives = hasOpenAlternatives;
	}
	
	
	public boolean  hasOpenAlternatives(){
		return this.hasOpenAlternatives;
	}

	
	public long getSerializationTimestamp() {
		return serializationTimestamp;
	}

	
	public void setSerializationTimestamp(long serializationTimestamp) {
		this.serializationTimestamp = serializationTimestamp;
	}

	public ArrayList<String> getFlags() {
		return flags;
	}

	public void setFlags(ArrayList<String> flags) {
		this.flags = flags;
	}

	public void setDynTheory(String theory) {
		this.dynTheory = theory;
	}
	
	public String getDynTheory(){
		return this.dynTheory;
	}

	public LinkedList<Operator> getOp() {
		return op;
	}

	public void setOp(LinkedList<Operator> list) {
		this.op = list;
	}

}