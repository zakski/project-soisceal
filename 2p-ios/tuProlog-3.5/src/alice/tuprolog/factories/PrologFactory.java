package alice.tuprolog.factories;

import alice.tuprolog.Prolog;
import alice.tuprolog.interfaces.IProlog;

public class PrologFactory {
	
	/**
	 * Builds a prolog engine with default libraries loaded.
	 *
	 * The default libraries are BasicLibrary, ISOLibrary,
	 * IOLibrary, and  JavaLibrary
	 */
	public static IProlog createProlog() {
		return new Prolog();
	}

}
