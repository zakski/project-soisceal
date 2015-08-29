package com.szadowsz.examples.prolog;
import alice.tuprolog.core.Prolog;
import alice.tuprolog.core.data.Term;
import alice.tuprolog.core.engine.Solution;
import alice.tuprolog.core.event.io.OutputEvent;
import alice.tuprolog.event.OutputListener;

    public class OnOutputExample {
	  static String finalResult = "";
      public static void main(String[] args) throws Exception {
		Prolog engine = new alice.tuprolog.core.Prolog();

		engine.addOutputListener(new OutputListener() {
		  @Override
		  public void onOutput(OutputEvent e) {
			  finalResult += e.getMsg();
		  }
		});

		Term goal = Term.createTerm("write('Hello world!')");
		Solution res = engine.solve(goal);
		res = engine.solve("write('Hello everybody!'), nl.");
		System.out.println("OUTPUT: " + finalResult);
	  }
    }
