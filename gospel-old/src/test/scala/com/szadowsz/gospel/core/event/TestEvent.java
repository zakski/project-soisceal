/*
 * Created on May 23, 2005
 *
 */
package com.szadowsz.gospel.core.event;

import com.szadowsz.gospel.core.listener.PrologListener;
import com.szadowsz.gospel.core.PrologEngine;
import com.szadowsz.gospel.core.Solution;
import com.szadowsz.gospel.core.Theory;
import com.szadowsz.gospel.core.event.interpreter.LibraryEvent;
import com.szadowsz.gospel.core.event.interpreter.QueryEvent;
import com.szadowsz.gospel.core.event.interpreter.TheoryEvent;

class MyListener implements PrologListener {

    public void theoryChanged(TheoryEvent ev){
        System.out.println("THEORY CHANGED: \n old: \n"+
                ev.getOldTheory()+"\n new: \n"+ev.getNewTheory());
    }

    public void newQueryResultAvailable(QueryEvent ev){
        System.out.println("NEW QUERY RESULT AVAILABLE: \nquery\n "+
                ev.getSolution().getQuery().toString()+"\nresult\n"+
                ev.getSolution());
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

        PrologEngine engine = new PrologEngine(new String[]{});

        MyListener l = new MyListener();
        engine.addTheoryListener(l);
        engine.addQueryListener(l);
        engine.addLibraryListener(l);

        engine.loadLibrary("com.szadowsz.gospel.core.db.libs.BasicLibrary");
        engine.loadLibrary("com.szadowsz.gospel.core.db.libs.OOLibrary");
        engine.loadLibrary("com.szadowsz.gospel.core.db.libs.IOLibrary");

        Theory th = new Theory(
                "a(1).\n"+
                        "a(2).\n");

        engine.setTheory(th);

        Solution sinfo = engine.solve("a(X).");

        while (sinfo.isSuccess() && engine.hasOpenAlternatives()){
            sinfo = engine.solveNext();
        }

        engine.unloadLibrary("com.szadowsz.gospel.core.db.libs.IOLibrary");

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
