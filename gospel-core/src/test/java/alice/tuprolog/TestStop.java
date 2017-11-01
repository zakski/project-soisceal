package alice.tuprolog;

import com.szadowsz.gospel.core.PrologEngine;
import com.szadowsz.gospel.core.Solution;
import com.szadowsz.gospel.core.Theory;

class PrologThread extends Thread {
	private final PrologEngine core;
	private final String goal;
	PrologThread(PrologEngine core, String goal){
		this.core = core;
		this.goal = goal;
	}

	public void run(){
		try {
			System.out.println("STARTING...");
			Solution info = core.solve(goal);
			System.out.println(info);
			System.out.println("STOP.");
		} catch (Exception ex){
			ex.printStackTrace();			
		}
	}	
}

public class TestStop {

	public static void main(String[] args) throws Exception {
		
		PrologEngine core = new PrologEngine();
		
		Theory th = new Theory(
			"rec(X):- current_thread <- sleep(X), X1 is X + 100, rec(X1).\n"
		);
		core.setTheory(th);
		
		
		new PrologThread(core,"rec(100).").start();
		
		Thread.sleep(2000);
		
		System.out.println("STOPPING...");
		
		core.solveHalt();
		
		Thread.sleep(2000);
		
		System.out.println("OK.");
	}
}
