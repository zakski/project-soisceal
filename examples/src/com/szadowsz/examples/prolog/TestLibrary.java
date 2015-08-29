package com.szadowsz.examples.prolog;
import alice.tuprolog.core.data.Struct;
import alice.tuprolog.core.data.Term;
import alice.tuprolog.core.data.Var;
import alice.tuprolog.core.data.numeric.Float;
import alice.tuprolog.core.data.numeric.Number;
import alice.tuprolog.*;

public class TestLibrary extends Library {

	public Term sum_2(Number arg0, Number arg1){
		float n0 = arg0.floatValue();
		float n1 = arg1.floatValue();
		return new Float(n0+n1);
	}

	public boolean println_1(Term arg){
		System.out.println(arg);
		return true;
	}

	public boolean invert_2(Term in, Var out){
		String s1=null, s2 = "";
		if (in instanceof Var) s1= in.getTerm().toString();
		else s1 = in.toString();
		for(int i=0; i<s1.length(); i++){
			if (s1.charAt(i)=='\'') continue;
			if (Character.isUpperCase(s1.charAt(i)))
				s2 = s2 + Character.toLowerCase(s1.charAt(i));
			else 
				s2 = s2 + Character.toUpperCase(s1.charAt(i));
		}
		return out.unify(getEngine(), new Struct(s2));
	}
}

/*
test :-
 N is sum(5,6),
 println(N),
 invert(abcd,S),
 println(S).
 invert('EFGH',V),
 println(V).
*/