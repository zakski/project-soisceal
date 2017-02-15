package com.szadowsz.gospel.util.event;


import com.szadowsz.gospel.core.event.PrologEventAdapter;
import com.szadowsz.gospel.core.event.interpreter.LibraryEvent;
import com.szadowsz.gospel.core.event.interpreter.QueryEvent;
import com.szadowsz.gospel.core.event.interpreter.TheoryEvent;

public class TestPrologEventAdapter extends PrologEventAdapter {
	public String firstMessage = "";
    public String secondMessage = "";
    
    public void theoryChanged(TheoryEvent ev) {
    	firstMessage = ev.getOldTheory().toString();
    	secondMessage = ev.getNewTheory().toString();
    }
    
    public void newQueryResultAvailable(QueryEvent ev) {
    	firstMessage = ev.getSolveInfo().getQuery().toString();
    	secondMessage = ev.getSolveInfo().toString();
    }
    
    public void libraryLoaded(LibraryEvent ev) {
    	firstMessage = ev.getLibraryName();
    }

    public void libraryUnloaded(LibraryEvent ev) {
    	firstMessage = ev.getLibraryName();
    }
}
