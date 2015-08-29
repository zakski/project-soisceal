/*
 * Created on May 23, 2005
 * 
 */
package alice.tuprolog.util;

import alice.tuprolog.core.engine.Solution;
import alice.tuprolog.core.event.interpreter.LibraryEvent;
import alice.tuprolog.core.event.interpreter.QueryEvent;
import alice.tuprolog.core.event.interpreter.TheoryEvent;
import alice.tuprolog.core.theory.Theory;
import alice.tuprolog.event.PrologEventAdapter;

class MyListener extends PrologEventAdapter {
    
    public void theoryChanged(TheoryEvent ev){
        System.out.println("THEORY CHANGED: \n old: \n"+
                ev.getOldTheory()+"\n new: \n"+ev.getNewTheory());
    }
    
    public void newQueryResultAvailable(QueryEvent ev){
        System.out.println("NEW QUERY RESULT AVAILABLE: \nquery\n "+
                ev.getSolveInfo().getQuery().toString()+"\nresult\n"+
                ev.getSolveInfo());
    }
    
    public void libraryLoaded(LibraryEvent ev){
        System.out.println("NEW LIB loaded: "+ev.getLibraryName());
    }

    public void libraryUnloaded(LibraryEvent ev){
        System.out.println("LIB unloaded: "+ev.getLibraryName());
    }

}
/**
 * 
 *
 * @author aricci
 *
 */
public class TestEvent {

    public static void main(String[] args) throws Exception {
    
        alice.tuprolog.core.Prolog engine = new alice.tuprolog.core.Prolog(new String[]{});
        
        MyListener l = new MyListener();
        engine.addTheoryListener(l);
        engine.addQueryListener(l);
        engine.addLibraryListener(l);

        engine.loadLibrary("alice.tuprolog.lib.BasicLibrary");
        engine.loadLibrary("alice.tuprolog.lib.OOLibrary");
        engine.loadLibrary("alice.tuprolog.lib.IOLibrary");
        
        Theory th = new Theory(
                	"a(1).\n"+
                	"a(2).\n");
        
        engine.setTheory(th);
        
        Solution sinfo = engine.solve("a(X).");
        
        while (sinfo.isSuccess() && engine.hasOpenAlternatives()){
            sinfo = engine.solveNext();
        }
        
        engine.unloadLibrary("alice.tuprolog.lib.IOLibrary");
        
        th = new Theory(
            	"a(3).\n"+
            	"a(4).\n");
    
        
        engine.addTheory(th);
        
        sinfo = engine.solve("a(X).");
        
        while (sinfo.isSuccess() && engine.hasOpenAlternatives()){
            sinfo = engine.solveNext();
        }
        
    }
}
