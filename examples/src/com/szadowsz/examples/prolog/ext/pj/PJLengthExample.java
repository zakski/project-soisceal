package com.szadowsz.examples.prolog.ext.pj;
import alice.tuprologx.pj.annotations.PrologClass;
import alice.tuprologx.pj.annotations.PrologMethod;
import alice.tuprologx.pj.engine.PJ;
import alice.tuprologx.pj.model.*;

@PrologClass(
	clauses={"size(X,Y):-length(X,Y)."}
)
public abstract class PJLengthExample {
	
	@PrologMethod 
	abstract <$L extends List<?>, $S extends Int> Boolean size(List<?> list, int i);
	
	@PrologMethod
	abstract <$Ls extends List<?>, $Ln extends Int> $Ln size($Ls expr);
	
	@PrologMethod 
	abstract <$Ls extends List<?>, $Ln extends Int> $Ls size(int expr);
	
	@PrologMethod 
	abstract <$Ls extends List<?>, $Ln extends Int> Iterable<Compound2<$Ls,$Ln>> size();

	public static void main(String[] args) throws Exception {
		PJLengthExample pjl = PJ.newInstance(PJLengthExample.class);
		java.util.List<?> v = java.util.Arrays.asList(12,"twelve",false);
		List<?> list = new List<Term<?>>(v);
		Boolean b = pjl.size(list, 3);	        //true: �list� is of size 3
		Int i = pjl.size(list);					//length of �list� is 3
		List<?> l = pjl.size(3);				//[_,_,_] is a list whose size is 3
		int cont = 0;
		for (Term<?> t : pjl.size()) { 
			//{[],[_],[_,_], ...} all lists whose size is less than 5
			System.out.println(t);
			if (cont++ == 5) break;
		}
	}
	/* OUPUT:
	Compound:'size'(List[],Int(0))
	Compound:'size'(List[Var(_)],Int(1))
	Compound:'size'(List[Var(_), Var(_)],Int(2))
	Compound:'size'(List[Var(_), Var(_), Var(_)],Int(3))
	Compound:'size'(List[Var(_), Var(_), Var(_), Var(_)],Int(4))
	Compound:'size'(List[Var(_), Var(_), Var(_), Var(_), Var(_)],Int(5))
	*/
}