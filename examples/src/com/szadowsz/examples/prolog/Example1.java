package com.szadowsz.examples.prolog;
    import alice.tuprolog.core.Prolog;
    import alice.tuprolog.core.engine.Solution;

public class Example1 {
        public static void main(String[] args) throws Exception {
            Prolog engine = new Prolog();
            Solution info = engine.solve("append([1],[2,3],X).");
            System.out.println(info.getSolution());
        }
    }
