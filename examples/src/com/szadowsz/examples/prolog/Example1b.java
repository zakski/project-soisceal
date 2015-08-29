package com.szadowsz.examples.prolog;
    import alice.tuprolog.core.Prolog;
    import alice.tuprolog.core.engine.Solution;

public class Example1b {
        public static void main(String[] args) throws Exception {
            Prolog engine = new Prolog();
            Solution info = engine.solve("append(X,Y,[1,2]).");
            while (info.isSuccess()) {
                System.out.println("solution: " + info.getSolution() +
                                   " - bindings: " + info);
                if (engine.hasOpenAlternatives()) {
                    info = engine.solveNext();
                } else {
                    break;
                }
            }
        }
    }
