package alice.tuprologx.ide;

import com.szadowsz.gospel.core.data.Var;
import com.szadowsz.gospel.core.db.libs.IOLibrary;
import alice.util.Automaton;
import com.szadowsz.gospel.core.PrologEngine;
import com.szadowsz.gospel.core.Solution;
import com.szadowsz.gospel.core.Theory;
import com.szadowsz.gospel.core.error.InvalidTheoryException;
import com.szadowsz.gospel.core.error.MalformedGoalException;
import com.szadowsz.gospel.core.error.NoSolutionException;
import com.szadowsz.gospel.core.event.interpreter.ExceptionEvent;
import com.szadowsz.gospel.core.event.interpreter.SpyEvent;
import com.szadowsz.gospel.core.event.interpreter.WarningEvent;
import com.szadowsz.gospel.core.event.io.OutputEvent;
import com.szadowsz.gospel.core.listener.ExceptionListener;
import com.szadowsz.gospel.core.listener.OutputListener;
import com.szadowsz.gospel.core.listener.SpyListener;
import com.szadowsz.gospel.core.listener.WarningListener;

import java.io.*;

@SuppressWarnings("serial")
public class CUIConsole extends Automaton implements Serializable, OutputListener, SpyListener, WarningListener, ExceptionListener/**/ {

    private static final String incipit =
            "tuProlog system - release " + PrologEngine.getVersion() + "\n";
    static String sol = ""; //to do -> correct output of CUI console in order to show multiple results
    private final BufferedReader stdin;
    private final PrologEngine engine;

    private CUIConsole(String[] args) {

        if (args.length > 1) {
            System.err.println("args: { theory file }");
            System.exit(-1);
        }


        engine = new PrologEngine();
        /**
         * Added the method setExecution to conform
         * the operation of CUIConsole as that of JavaIDE
         */
        IOLibrary IO = (IOLibrary) engine.getLibrary("com.szadowsz.gospel.core.db.libs.IOLibrary");
        IO.setExecutionType(IOLibrary.consoleExecution);
        /***/
        stdin = new BufferedReader(new InputStreamReader(System.in));
        engine.addWarningListener(this);
        engine.addOutputListener(this);
        engine.addSpyListener(this);
        /*Castagna 06/2011*/
        engine.addExceptionListener(this);
        /**/
        if (args.length > 0) {
            try {
                engine.setTheory(new Theory(new FileInputStream(args[0])));
            } catch (InvalidTheoryException ex) {
                System.err.println("invalid theory - line: " + ex.line);
                System.exit(-1);
            } catch (Exception ex) {
                System.err.println("invalid theory.");
                System.exit(-1);
            }
        }
    }

    public static void main(String[] args) {
        new Thread(new CUIConsole(args)).start();
    }

    public void boot() {
        System.out.println(incipit);
        become("goalRequest");
    }

    public void goalRequest() {
        String goal = "";
        while (goal.equals("")) {
            System.out.print("\n?- ");
            try {
                goal = stdin.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        solveGoal(goal);
    }

    private void solveGoal(String goal) {

        try {
            Solution info = engine.solve(goal);

            /*Castagna 06/2011*/
            //if (engine.isHalted())
            //	System.exit(0);
            /**/
            if (!info.isSuccess()) {
                /*Castagna 06/2011*/
                if (info.isHalted())
                    System.out.println("halt.");
                else
                /**/
                    System.out.println("no.");
                become("goalRequest");
            } else if (!engine.hasOpenAlternatives()) {
                String binds = info.toString();
                if (binds.equals("")) {
                    System.out.println("yes.");
                } else {
                    System.out.println(solveInfoToString(info) + "\nyes.");
                }
                become("goalRequest");
            } else {
                System.out.print(solveInfoToString(info) + " ? ");
                become("getChoice");
            }
        } catch (MalformedGoalException ex) {
            System.out.println("syntax error in goal:\n" + goal);
            become("goalRequest");
        }
    }

    private String solveInfoToString(Solution result) {
        String s = "";
        try {
            for (Var v : result.getBindingVars()) {
                if (!v.isAnonymous() && v.isBound() && (!(v.getTerm() instanceof Var) || (!((Var) (v.getTerm())).getName().startsWith("_")))) {
                    s += v.getName() + " / " + v.getTerm() + "\n";
                }
            }
            /*Castagna 06/2011*/
            if (s.length() > 0) {
            /**/
                s.substring(0, s.length() - 1);
            }
        } catch (NoSolutionException e) {
        }
        return s;
    }

    public void getChoice() {
        String choice = "";
        try {
            while (true) {
                choice = stdin.readLine();
                if (!choice.equals(";") && !choice.equals(""))
                    System.out.println("\nAction ( ';' for more choices, otherwise <return> ) ");
                else
                    break;
            }
        } catch (IOException ex) {
        }
        if (!choice.equals(";")) {
            System.out.println("yes.");
            engine.solveEnd();
            become("goalRequest");
        } else {
            try {
                System.out.println();
                Solution info = engine.solveNext();
                if (!info.isSuccess()) {
                    System.out.println("no.");
                    become("goalRequest");
                } else {
                    System.out.print(solveInfoToString(info) + " ? ");
                    become("getChoice");
                }
            } catch (Exception ex) {
                System.out.println("no.");
                become("goalRequest");
            }
        }
    }

    public void onOutput(OutputEvent e) {
        System.out.print(e.getMsg());
    }

    public void onSpy(SpyEvent e) {
        System.out.println(e.getMsg());
    }

    public void onWarning(WarningEvent e) {
        System.out.println(e.getMsg());
    }
	/**/

    /*Castagna 06/2011*/
    public void onException(ExceptionEvent e) {
        System.out.println(e.getMsg());
    }
}
