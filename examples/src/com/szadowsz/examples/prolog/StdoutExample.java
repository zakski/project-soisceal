package com.szadowsz.examples.prolog;
import alice.tuprolog.*;
import alice.tuprolog.core.Prolog;
import alice.tuprolog.core.data.Struct;
import alice.tuprolog.core.theory.Theory;
import alice.tuprolog.lib.OOLibrary;

public class StdoutExample {
	public static void main(String[] args) throws Exception {
		alice.tuprolog.core.Prolog engine = new Prolog();
		// Library lib = engine.loadLibrary("alice.tuprolog.lib.JavaLibrary");
		Library lib = engine.getLibrary("alice.tuprolog.lib.OOLibrary");
		// Library lib = engine.getLibrary("JavaLibrary");
		((OOLibrary) lib).register(new Struct("stdout"), System.out);
		engine.setTheory(new Theory(":-solve(go). \n go:- stdout <- println('hello!')."));
	}
}
