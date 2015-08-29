package alice.tuprolog;


import alice.tuprolog.core.event.interpreter.LibraryEvent;
import alice.tuprolog.core.event.interpreter.QueryEvent;
import alice.tuprolog.core.event.interpreter.TheoryEvent;
import alice.tuprolog.event.PrologEventAdapter;

public class TestPrologEventAdapter extends PrologEventAdapter {
	String firstMessage = "";
	String secondMessage = "";
    
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
