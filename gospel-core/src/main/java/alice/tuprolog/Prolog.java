/*
 * tuProlog - Copyright (C) 2001-2002  aliCE team at deis.unibo.it
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package alice.tuprolog;

import java.util.*;
import java.io.*;

//import alice.tuprologx.ide.ToolBar;
import com.szadowsz.gospel.core.listener.ExceptionListener;
import com.szadowsz.gospel.core.event.interpreter.*;
import com.szadowsz.gospel.core.listener.LibraryListener;
import com.szadowsz.gospel.core.listener.OutputListener;
import com.szadowsz.gospel.core.listener.QueryListener;
import com.szadowsz.gospel.core.listener.SpyListener;
import com.szadowsz.gospel.core.listener.TheoryListener;
import com.szadowsz.gospel.core.listener.WarningListener;
import alice.tuprolog.json.AbstractEngineState;
import alice.tuprolog.json.FullEngineState;
import alice.tuprolog.json.JSONSerializerManager;
import alice.tuprolog.json.ReducedEngineState;
import com.szadowsz.gospel.core.event.io.OutputEvent;


/**
 *
 * The Prolog class represents a tuProlog engine.
 *
 */
public abstract class Prolog implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final boolean INCLUDE_KB_IN_SERIALIZATION = true;
	public static final boolean EXCLUDE_KB_IN_SERIALIZATION = false;

	/* component managing operators */
	protected OperatorManager opManager = new DefaultOperatorManager();

	/*  spying activated ?  */
	protected boolean spy;
	/*  warning activated ?  */
	protected boolean warning;
	/* listeners registrated for virtual machine output events */
	/*Castagna 06/2011*/	
	/* exception activated ? */
	protected boolean exception;
	/**/
	protected ArrayList<OutputListener> outputListeners;
	/* listeners registrated for virtual machine internal events */
	protected ArrayList<SpyListener> spyListeners;
	/* listeners registrated for virtual machine state change events */
	protected ArrayList<WarningListener> warningListeners;
	/*Castagna 06/2011*/	
	/* listeners registrated for virtual machine state exception events */
	protected ArrayList<ExceptionListener> exceptionListeners;
	/**/

	/* listeners to theory events */
	protected ArrayList<TheoryListener> theoryListeners;
	/* listeners to library events */
	protected ArrayList<LibraryListener> libraryListeners;
	/* listeners to query events */
	protected ArrayList<QueryListener> queryListeners;

    /* path history for including documents */
	protected ArrayList<String> absolutePathList;
	protected String lastPath;
    

	/**
	 * Initialize basic engine structures.
	 * 
	 * @param spy spying activated
	 * @param warning warning activated
	 */
	protected Prolog(boolean spy, boolean warning) {
		outputListeners = new ArrayList<OutputListener>();
		spyListeners = new ArrayList<SpyListener>();
		warningListeners = new ArrayList<WarningListener>();
		/*Castagna 06/2011*/		
		exceptionListeners = new ArrayList<ExceptionListener>();
		/**/
		this.spy = spy;
		this.warning = warning;
		/*Castagna 06/2011*/
		exception = true;
		/**/
		theoryListeners = new ArrayList<TheoryListener>();
		queryListeners = new ArrayList<QueryListener>();
		libraryListeners = new ArrayList<LibraryListener>();
        absolutePathList = new ArrayList<String>();
	}

	
	//Alberto
	public static AbstractEngineState getEngineStateFromJSON(String jsonString){
		AbstractEngineState brain = null;
		if(jsonString.contains("FullEngineState")){
			brain = JSONSerializerManager.fromJSON(jsonString, FullEngineState.class);
		} else if(jsonString.contains("ReducedEngineState")){
			brain = JSONSerializerManager.fromJSON(jsonString, ReducedEngineState.class);
		}
		return brain;
	}

	/** Gets the component managing operators */
	public OperatorManager getOperatorManager() {
		return opManager; 
	}

	/**
	 * Gets the current version of the tuProlog system
	 */
	public static String getVersion() {
		return alice.util.VersionInfo.getEngineVersion();
	}

    /**
     * Gets the last Element of the path list
     */
    public String getCurrentDirectory() {
        String directory = "";
        if(absolutePathList.isEmpty()) {
        	if(this.lastPath!=null)
        	{
        		directory = this.lastPath;
        	}
        	else
        	{
        		directory = System.getProperty("user.dir");
        	}
        } else {
            directory = absolutePathList.get(absolutePathList.size()-1);
        }

        return directory;
    }

    /**
     * Sets the last Element of the path list
     */
    public void setCurrentDirectory(String s) {
        this.lastPath=s;    
    }
    
	/**
	 *  Gets the list of the operators currently defined
	 *
	 *  @return the list of the operators
	 */
	public java.util.List<Operator> getCurrentOperatorList() {	//no syn
		return opManager.getOperators();
	}

/**
	 * Gets the string representation of a term, using operators
	 * currently defined by engine
	 *
	 * @param term      the term to be represented as a string
	 * @return the string representing the term
	 */
	public String toString(Term term) {		//no syn
		return (term.toStringAsArgY(opManager, OperatorManager.OP_HIGH));
	}


	// spy interface ----------------------------------------------------------

	/**
	 * Switches on/off the notification of spy information events
	 * @param state  - true for enabling the notification of spy event
	 */
	public synchronized void setSpy(boolean state) {
		spy = state;
	}

	/**
	 * Checks the spy state of the engine
	 * @return  true if the engine emits spy information
	 */
	public synchronized boolean isSpy() {
		return spy;
	}

	/**
	 * Switches on/off the notification of warning information events
	 * @param state  - true for enabling warning information notification
	 */
	public synchronized void setWarning(boolean state) {
		warning = state;
	}

	/**
	 * Checks if warning information are notified
	 * @return  true if the engine emits warning information
	 */
	public synchronized boolean isWarning() {
		return warning;
	}


	/**/

	/*Castagna 06/2011*/
	/**
	 * Checks if exception information are notified
	 * @return  true if the engine emits exception information
	 */
	public synchronized boolean isException() {
		return exception;
	}
	/**/

	/*Castagna 06/2011*/
	/**
	 * Switches on/off the notification of exception information events
	 * @param state  - true for enabling exception information notification
	 */
	public synchronized void setException(boolean state) {
		exception = state;
	}
	/**/

	/**
	 * Produces an output information event
	 *
	 * @param m the output string
	 */
	public synchronized void stdOutput(String m) {
		notifyOutput(new OutputEvent(this, m));
	}

	// event listeners management

	/**
	 * Adds a listener to ouput events
	 *
	 * @param l the listener
	 */
	public synchronized void addOutputListener(OutputListener l) {
		outputListeners.add(l);
	}


	/**
	 * Adds a listener to theory events
	 *
	 * @param l the listener
	 */
	public synchronized void addTheoryListener(TheoryListener l) {
		theoryListeners.add(l);
	}

	/**
	 * Adds a listener to library events
	 *
	 * @param l the listener
	 */
	public synchronized void addLibraryListener(LibraryListener l) {
		libraryListeners.add(l);
	}

	/**
	 * Adds a listener to theory events
	 *
	 * @param l the listener
	 */
	public synchronized void addQueryListener(QueryListener l) {
		queryListeners.add(l);
	}

	/**
	 * Adds a listener to spy events
	 *
	 * @param l the listener
	 */
	public synchronized void addSpyListener(SpyListener l) {
		spyListeners.add(l);
	}

	/**
	 * Adds a listener to warning events
	 *
	 * @param l the listener
	 */
	public synchronized void addWarningListener(WarningListener l) {
		warningListeners.add(l);
	}

	/*Castagna 06/2011*/	
	/**
	 * Adds a listener to exception events
	 *
	 * @param l the listener
	 */
	public synchronized void addExceptionListener(ExceptionListener l) {
		exceptionListeners.add(l);
	}
	/**/

	/**
	 * Removes a listener to ouput events
	 *
	 * @param l the listener
	 */
	public synchronized void removeOutputListener(OutputListener l) {
		outputListeners.remove(l);
	}

	/**
	 * Removes all output event listeners
	 */
	public synchronized void removeAllOutputListeners() {
		outputListeners.clear();
	}

	/**
	 * Removes a listener to theory events
	 *
	 * @param l the listener
	 */
	public synchronized void removeTheoryListener(TheoryListener l) {
		theoryListeners.remove(l);
	}

	/**
	 * Removes a listener to library events
	 *
	 * @param l the listener
	 */
	public synchronized void removeLibraryListener(LibraryListener l) {
		libraryListeners.remove(l);
	}

	/**
	 * Removes a listener to query events
	 *
	 * @param l the listener
	 */
	public synchronized void removeQueryListener(QueryListener l) {
		queryListeners.remove(l);
	}


	/**
	 * Removes a listener to spy events
	 *
	 * @param l the listener
	 */
	public synchronized void removeSpyListener(SpyListener l) {
		spyListeners.remove(l);
	}

	/**
	 * Removes all spy event listeners
	 */
	public synchronized void removeAllSpyListeners() {
		spyListeners.clear();
	}

	/**
	 * Removes a listener to warning events
	 *
	 * @param l the listener
	 */
	public synchronized void removeWarningListener(WarningListener l) {
		warningListeners.remove(l);
	}

	/**
	 * Removes all warning event listeners
	 */
	public synchronized void removeAllWarningListeners() {
		warningListeners.clear();
	}

	/* Castagna 06/2011*/	
	/**
	 * Removes a listener to exception events
	 *
	 * @param l the listener
	 */
	public synchronized void removeExceptionListener(ExceptionListener l) {
		exceptionListeners.remove(l);
	}
	/**/	

	/*Castagna 06/2011*/	
	/**
	 * Removes all exception event listeners
	 */
	public synchronized void removeAllExceptionListeners() {
		exceptionListeners.clear();
	}
	/**/

	/**
	 * Gets a copy of current listener list to output events
	 */
	public synchronized List<OutputListener> getOutputListenerList() {
		return new ArrayList<OutputListener>(outputListeners);
	}

	/**
	 * Gets a copy of current listener list to warning events
	 *
	 */
	public synchronized List<WarningListener> getWarningListenerList() {
		return new ArrayList<WarningListener>(warningListeners);
	}

	/*Castagna 06/2011*/	
	/**
	 * Gets a copy of current listener list to exception events
	 *
	 */
	public synchronized List<ExceptionListener> getExceptionListenerList() {
		return new ArrayList<ExceptionListener>(exceptionListeners);
	}
	/**/
	
	/**
	 * Gets a copy of current listener list to spy events
	 *
	 */
	public synchronized List<SpyListener> getSpyListenerList() {
		return new ArrayList<SpyListener>(spyListeners);
	}

	/**
	 * Gets a copy of current listener list to theory events
	 * 
	 */
	public synchronized List<TheoryListener> getTheoryListenerList() {
		return new ArrayList<TheoryListener>(theoryListeners);
	}

	/**
	 * Gets a copy of current listener list to library events
	 *
	 */
	public synchronized List<LibraryListener> getLibraryListenerList() {
		return new ArrayList<LibraryListener>(libraryListeners);
	}

	/**
	 * Gets a copy of current listener list to query events
	 *
	 */
	public synchronized List<QueryListener> getQueryListenerList() {
		return new ArrayList<QueryListener>(queryListeners);
	}

	// notification

	/**
	 * Notifies an ouput information event
	 *
	 * @param e the event
	 */
	protected void notifyOutput(OutputEvent e) {
		for(OutputListener ol:outputListeners){
			ol.onOutput(e);
		}
	}

	/**
	 * Notifies a spy information event
	 *
	 * @param e the event
	 */
	protected void notifySpy(SpyEvent e) {
		for(SpyListener sl:spyListeners){
			sl.onSpy(e);
		}
	}

	/**
	 * Notifies a warning information event
	 *
	 * @param e the event
	 */
	protected void notifyWarning(WarningEvent e) {
		for(WarningListener wl:warningListeners){
			wl.onWarning(e);
		}
	}

	/*Castagna 06/2011*/	
	/**
	 * Notifies a exception information event
	 *
	 * @param e the event
	 */
	protected void notifyException(ExceptionEvent e) {
		for(ExceptionListener el:exceptionListeners){
			el.onException(e);
		}
	}
	/**/
	
	//

	/**
	 * Notifies a new theory set or updated event
	 * 
	 * @param e the event
	 */
	protected void notifyChangedTheory(TheoryEvent e) {
		for (TheoryListener tl : theoryListeners) {
			tl.theoryChanged(e);
		}
	}

	/**
	 * Notifies a library loaded event
	 * 
	 * @param e the event
	 */
	public void notifyLoadedLibrary(LibraryEvent e) {
		for(LibraryListener ll:libraryListeners){
			ll.libraryLoaded(e);
		}
	}

	/**
	 * Notifies a library unloaded event
	 * 
	 * @param e the event
	 */
	protected void notifyUnloadedLibrary(LibraryEvent e) {
		for(LibraryListener ll:libraryListeners){
			ll.libraryUnloaded(e);
		}
	}

	/**
	 * Notifies a library loaded event
	 * 
	 * @param e the event
	 */
	protected void notifyNewQueryResultAvailable(QueryEvent e) {
		for(QueryListener ql:queryListeners){
			ql.newQueryResultAvailable(e);
		}
	}


    /**
     * Append a new path to directory list
     *
     */
    public void pushDirectoryToList(String path) {
        absolutePathList.add(path);
    }

    /**
     *
     * Retract an element from directory list
     */
    public void popDirectoryFromList() {
        if(!absolutePathList.isEmpty()) {
            absolutePathList.remove(absolutePathList.size()-1);
        }
    }

     /**
       *
       * Reset directory list
      */
    public void resetDirectoryList(String path) {
        absolutePathList = new ArrayList<String>();
        absolutePathList.add(path);
    }
}