package com.szadowsz.examples.prolog;
    import alice.tuprolog.core.data.Struct;
    import alice.tuprolog.core.data.Var;

public class Example2 {
        public static void main(String[] args) throws Exception {
			Struct myStruct = new Struct("p", new Var("X"), new alice.tuprolog.core.data.numeric.Int(1), new Var("X"));
            System.out.println(myStruct);
            System.out.println(myStruct.getArg(0));
            System.out.println(myStruct.getArg(1));
            System.out.println(myStruct.getArg(2));
			System.out.println("Before:" + (myStruct.getArg(0)==myStruct.getArg(2)));
			boolean res = myStruct.unify(new alice.tuprolog.core.Prolog(), new Struct());
//			boolean res = myStruct.match(new Struct());
//			myStruct.resolveTerm();
			System.out.println("After: " + (myStruct.getArg(0)==myStruct.getArg(2)));
      }
    }
