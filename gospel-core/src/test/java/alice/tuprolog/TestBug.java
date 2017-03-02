/*
 * Created on Dec 10, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package alice.tuprolog;

import alice.tuprolog.event.SpyEvent;
import alice.tuprolog.event.SpyListener;
import com.szadowsz.gospel.core.PrologEngine;
import com.szadowsz.gospel.core.Solution;
import com.szadowsz.gospel.core.Theory;

/**
 * @author aricci
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class TestBug {

    
	public static void main(String[] args) throws Exception {

	    
	    String goal = "out('"+
			"can_do(X).\n"+
			"can_do(Y).\n"+
		"').";
		
		new PrologEngine().solve(goal);
		
		String st =
		"p(X).				\n"+
		"test(L1,L2):-		\n"+
		"	findall(p(X),p(X),L1), \n"+
		"	append([a,b],L1,L2).	\n";
		
		
		PrologEngine engine = new PrologEngine();
		engine.addSpyListener(new SpyListener(){ 
			public void onSpy(SpyEvent e){
				System.out.println(e);
			}
		});
		//engine.setSpy(true);
		engine.setTheory(new Theory(st));
		Solution info = engine.solve("test(L1,L2).");
		System.out.println(info);
		
	}
}
