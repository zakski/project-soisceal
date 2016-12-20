package alice.tuprologx.runtime.tcp.test;

import alice.tuprolog.*;
import alice.tuprologx.runtime.tcp.Proxy;

public class Test
{
    public static void main(String args[])
    {
        if (args.length<2){
            System.err.println("args:  <host> <goal>");
            System.exit(-1);
        }
        try{
            alice.tuprologx.runtime.tcp.Prolog engine = new Proxy(args[0]);
            SolveInfo info=engine.solve(args[1]);
            if (info.isSuccess())
                System.out.println("yes: "+info.getSolution());
            else
                System.out.println("no.");
        } catch(Exception e) {
            System.err.println("ERROR: " + e);
            e.printStackTrace(System.out);
        }
    }
}




































