package alice.tuprolog.interfaces;

import java.net.URL;

import alice.tuprolog.Library;
import alice.tuprolog.Prolog;
import alice.tuprolog.Term;
import alice.tuprolog.exceptions.InvalidLibraryException;

//Alberto
public interface ILibraryManager 
{
	void initialize(Prolog prologVM);
	
	Library loadLibrary(String className) throws InvalidLibraryException;
	Library loadLibrary(String className, String[] paths) throws InvalidLibraryException;
	void loadLibrary(Library lib) throws InvalidLibraryException;
	
	String[] getCurrentLibraries();
	
	void unloadLibrary(String name) throws InvalidLibraryException;
	
	Library getLibrary(String name);
	
	void onSolveBegin(Term g);
	void onSolveHalt();
	void onSolveEnd();
	
	URL getExternalLibraryURL(String name);
	
	boolean isExternalLibrary(String name);
}
