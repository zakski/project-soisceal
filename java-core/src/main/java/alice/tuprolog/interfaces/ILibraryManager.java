package alice.tuprolog.interfaces;

import alice.tuprolog.InvalidLibraryException;
import alice.tuprolog.Library;
import alice.tuprolog.Term;

import java.net.URL;

/**
 * Created on 17/02/2017.
 */
public interface ILibraryManager {
    Library loadLibrary(String className)
            throws InvalidLibraryException;

    Library loadLibrary(String className, String[] paths)
                    throws InvalidLibraryException;

    void loadLibrary(Library lib)
                            throws InvalidLibraryException;

    String[] getCurrentLibraries();

    void unloadLibrary(String name)
			throws InvalidLibraryException;

    Library getLibrary(String name);

    void onSolveBegin(Term g);

    void onSolveHalt();

    void onSolveEnd();

    URL getExternalLibraryURL(String name);

    boolean isExternalLibrary(String name);

    void setOptimizedDirectory(String optimizedDirectory);

    String getOptimizedDirectory();
}
