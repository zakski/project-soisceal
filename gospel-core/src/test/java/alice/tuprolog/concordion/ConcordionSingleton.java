package alice.tuprolog.concordion;

import alice.tuprolog.Term;
import alice.tuprolog.Var;
import com.szadowsz.gospel.core.PrologEngine;
import com.szadowsz.gospel.core.Solution;
import com.szadowsz.gospel.core.Theory;
import com.szadowsz.gospel.core.error.PrologException;
import com.szadowsz.gospel.core.listener.ExceptionListener;

import java.util.ArrayList;
import java.util.List;

public class ConcordionSingleton {

    private static ConcordionSingleton singleton;
    private String exceptionFounded = "";
    private boolean exFounded = false; // variable used to found an exception
    private final ExceptionListener ex = e -> {
        exFounded = true;
        exceptionFounded = e.getMsg();

    };

    private ConcordionSingleton() {
    }

    public static ConcordionSingleton getInstance() {
        if (singleton == null) {
            singleton = new ConcordionSingleton();
        }

        return singleton;
    }

    /* If there isn't a theory, insert null in the tag <td> */
    public boolean success(String goal, String theory) throws Exception {

        PrologEngine engine = new PrologEngine();
        if (!theory.equalsIgnoreCase("null"))
            engine.setTheory(new Theory(theory));
        Solution info = engine.solve(goal);
        return info.isSuccess();
    }

    /* Return true if there is an exception */
    public boolean successWithException(String goal, String theory)
            throws PrologException {

        PrologEngine engine;
        @SuppressWarnings("unused")
        Solution info = null;
        exFounded = false;
        engine = new PrologEngine();
        if (!theory.equalsIgnoreCase("null"))
            engine.setTheory(new Theory(theory));
        engine.addExceptionListener(ex);
        info = engine.solve(goal);
        //System.out.println(engine.getErrors());
        return exFounded;

    }

    /* Return type of error */
    public String successWithExceptionAndText(String goal, String theory)
            throws PrologException {

        PrologEngine engine;
        @SuppressWarnings("unused")
        Solution info = null;
        exFounded = false;
        engine = new PrologEngine();
        if (!theory.equalsIgnoreCase("null"))
            engine.setTheory(new Theory(theory));
        engine.addExceptionListener(ex);
        info = engine.solve(goal);
        if (exFounded)
            return exceptionFounded;
        else
            return "No Errors!";

    }

    /* Return the first result (With or Without replace) */
    public String successAndResult(String goal, String theory, String variable)
            throws Exception {

        return successAndResultVerifyReplace(goal, theory, variable, true);

    }

    public String successAndResultWithoutReplace(String goal, String theory,
                                                 String variable) throws Exception {

        return successAndResultVerifyReplace(goal, theory, variable, false);

    }

    /* Return the first result of goal (With or Without replace), with limit */
    public boolean successAndResultsWithLimit(String goal, String theory,
                                              String variable, String solution, int maxSolutions)
            throws Exception {

        return successAndResultsWithLimitVerifyReplace(goal, theory, variable,
                solution, true, maxSolutions);

    }

    public boolean successAndResultsWithLimitWithoutReplace(String goal,
                                                            String theory, String variable, String solution,
                                                            int maxSolutions) throws Exception {

        return successAndResultsWithLimitVerifyReplace(goal, theory, variable,
                solution, false, maxSolutions);

    }

    /* Check the result in the list(infinite) */
    private boolean successAndResultsWithLimitVerifyReplace(String goal,
                                                            String theory, String variable, String solution, boolean replace,
                                                            int maxSolutions) throws Exception {

        PrologEngine engine = new PrologEngine();
        Solution info;
        List<String> results = new ArrayList<>();


        if (!theory.equalsIgnoreCase("null"))
            engine.setTheory(new Theory(theory));
        info = engine.solve(goal);
        while (info.isSuccess() && maxSolutions != 0) {

            for (Var var : info.getBindingVars()) {

                if ((var.toString()).startsWith(variable)) {

                    variable = (replace ? replaceForVariable(
                            var.toString(), ' ') : var.toString());

                    Term t = info.getVarValue(variable);
                    results.add(replace ? replaceUnderscore(t
                            .toString()) : t.toString());

                }

            }

            if (replace)
                variable = replaceForVariable(variable, '_');
            Term t = info.getVarValue(variable);
            results.add(replace ? replaceUnderscore(t.toString()) : t
                    .toString());

            if (engine.hasOpenAlternatives()) {
                info = engine.solveNext();
            } else {
                break;
            }
            maxSolutions--;
        }

        //System.out.println(results.toString());
        return results.contains(solution);

    }

    private boolean successAndResultsVerifyReplace(String goal, String theory,
                                                   String variable, String solution, boolean replace) throws Exception {

        PrologEngine engine = new PrologEngine();
        Solution info;
        List<String> results = new ArrayList<>();

        if (!theory.equalsIgnoreCase("null"))
            engine.setTheory(new Theory(theory));
        info = engine.solve(goal);
        while (info.isSuccess()) {

            for (Var var : info.getBindingVars()) {
                if ((var.toString()).startsWith(variable)) {

                    variable = replaceForVariable(var.toString(), ' ');
                    Term t = info.getVarValue(variable);
                    results.add(replace ? replaceUnderscore(t
                            .toString()) : t.toString());


                }

            }
            variable = replaceForVariable(variable, '_');
            Term t = info.getVarValue(variable);
            results.add(replace ? replaceUnderscore(t.toString()) : t
                    .toString());

            if (engine.hasOpenAlternatives()) {
                info = engine.solveNext();
            } else {
                break;
            }
        }
        System.out.println(results.toString());
        return results.contains(solution);

    }

    /* Check the result in the list(not infinite) */
    public boolean successAndResults(String goal, String theory,
                                     String variable, String solution) throws Exception {

        return successAndResultsVerifyReplace(goal, theory, variable, solution,
                true);

    }

    public boolean successAndResultsWithoutReplace(String goal, String theory,
                                                   String variable, String solution) throws Exception {

        return successAndResultsVerifyReplace(goal, theory, variable, solution,
                false);

    }

    /* Return the first result of goal */
    private String successAndResultVerifyReplace(String goal, String theory,
                                                 String variable, boolean replace) throws Exception {

        PrologEngine engine = new PrologEngine();
        if (!theory.equalsIgnoreCase("null"))
            engine.setTheory(new Theory(theory));
        Solution info = engine.solve(goal);
        for (Var var : info.getBindingVars()) {
            if ((var.toString()).startsWith(variable)) {

                variable = replaceForVariable(var.toString(), ' ');
                Term t = info.getVarValue(variable);
                System.out.println(t.toString());
                return (replace ? replaceUnderscore(t.toString()) : t
                        .toString());

            }

        }
        variable = replaceForVariable(variable, '_');
        Term t = info.getVarValue(variable);
        return (replace ? replaceUnderscore(t.toString()) : t
                .toString());
    }

    private String replaceForVariable(String query, char car) {

        String result = "";
        for (int i = 0; i < query.length(); i++) {

            if (query.charAt(i) == car)
                return result;
            result += (query.charAt(i) + "");

        }

        return result;

    }

    private String replaceUnderscore(String query) {

        String result = "";
        boolean trovato = false;
        for (int i = 0; i < query.length(); i++) {

            if (query.charAt(i) == ',' || query.charAt(i) == ')'
                    || query.charAt(i) == ']')
                trovato = false;
            if (!trovato)
                result += (query.charAt(i) + "");
            if (query.charAt(i) == '_')
                trovato = true;

        }

        return result;
    }


    public Term value(String evaluable) throws Exception {
        PrologEngine engine = new PrologEngine();
        Solution result = engine.solve("X is " + evaluable);
        return result.getVarValue("X");
    }


}
