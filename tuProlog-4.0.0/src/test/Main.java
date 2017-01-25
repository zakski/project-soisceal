package test;

import alice.tuprolog.exceptions.InvalidTheoryException;
import alice.tuprolog.exceptions.MalformedGoalException;
import alice.tuprolog.Prolog;
import alice.tuprolog.Theory;

public class Main {

	public static void main(String[] args) throws InvalidTheoryException, MalformedGoalException {
		String th = ":- flag(ciao, [on, off], off, false).\n"
				+ ":- op(  500, yfx,  'g').\n"
				+ ":- op(  200, fx,   'mnp').\n"
				+ ":- initialization(unload_library('alice.tuprolog.lib.OOLibrary')). \n"
				+ "pippo(i).\n"
				+ "pi(j).";
		
		Prolog p = new Prolog();
		
		p.setTheory(new Theory(th));
		
		System.out.println(p.solve("pi(j).").toString());
		System.out.println(p.solve("retract(pippo(i)).").toString());
		System.out.println(p.solve("retract(pippo(i)).").toString());
		
		System.out.println(p.toJSON(true));
		
		Prolog pp = Prolog.fromJSON(p.toJSON(true));
		
		System.out.println(p.toJSON(true).equals(pp.toJSON(true)));
		
		System.out.println(pp.toJSON(true));
		
		System.out.println(pp.solve("flag_list(L).").toString());
		System.out.println(pp.solve("print(ciao).").toString());
		System.out.println(pp.solve("retract(pippo(i)).").toString());
		System.out.println(pp.solve("pippo.").toString());
				

	}

}
