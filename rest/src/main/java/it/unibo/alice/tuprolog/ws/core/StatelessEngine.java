package it.unibo.alice.tuprolog.ws.core;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import com.google.gson.JsonObject;

import alice.tuprolog.InvalidTheoryException;
import alice.tuprolog.MalformedGoalException;
import alice.tuprolog.NoMoreSolutionException;
import alice.tuprolog.Prolog;
import alice.tuprolog.SolveInfo;
import alice.tuprolog.Term;
import alice.tuprolog.Theory;
import it.unibo.alice.tuprolog.ws.persistence.StorageService;


/**
 * @author Andrea Muccioli
 *
 */
@Stateless
@LocalBean
public class StatelessEngine {

	private Prolog engine;
	
	@EJB
	private StorageService manager;
	
	@EJB
	private EngineState engineState;
	
	private List<String> currentAssertions = new ArrayList<String>();
	
    public StatelessEngine() {

    }
    
	public void reloadConfiguration() {
		System.out.println("PrologEngine reload provo ad ottenere teoria");
		String theory = manager.getConfiguration().getTheory();
//		System.out.println("reload ottenuto: "+theory);
		engine = new Prolog();
		try {
			if (!theory.isEmpty()) {
				Theory t = new Theory(theory);
				engine.setTheory(t);
			}
		} catch (InvalidTheoryException e) {
			e.printStackTrace();
			engine = new Prolog();
		}
	}
	
	/**
	 * Initializes the engine with the theory set in the configuration. If no theory is
	 * set or if the theory is not valid, a new Prolog engine is created.
	 */
	public void loadConfiguration() {
		System.out.println("StatelessEngine/ load provo ad ottenere teoria");
		String theory = manager.getConfiguration().getTheory();
//		System.out.println("reload ottenuto: "+theory);
		engine = new Prolog();
		try {
			if (!theory.isEmpty()) {
				Theory t = new Theory(theory);
				engine.setTheory(t);
			}
		} catch (InvalidTheoryException e) {
			e.printStackTrace();
			engine = new Prolog();
		}
		System.out.println("StatelessEngine/ refresh assertions");
		refreshAssertions();
		System.out.println("StatelessEngine/ assertions: "+currentAssertions);
	}
	
   

	/**
	 * Solves the goal provided as String.
	 * 
	 * @param toSolve : the goal to solve as String.
	 * @return the SolveInfo of the solution.
	 * @throws MalformedGoalException
	 */
	public SolveInfo solve(String toSolve) throws MalformedGoalException {
		loadConfiguration();
		return engine.solve(toSolve);
	}
	
	/**
	 * Solves the goal provided.
	 * 
	 * @param toSolve : the goal to solve as Term.
	 * @return the SolveInfo of the solution.
	 */
	public SolveInfo solve(Term toSolve) {
		loadConfiguration();
		return engine.solve(toSolve);
	}
	
	/**
	 * Solves the goal provided and returns both the solution and the state of the engine
	 * to use in future invocations.
	 * 
	 * @param toSolve : the goal to solve as String.
	 * @return a PrologSolution object containing the SolveInfo of the solution
	 * and the serialized state of the engine as JSON.
	 * @throws MalformedGoalException
	 */
	public PrologSolution solveWithSession(String toSolve) throws MalformedGoalException {
		loadConfiguration();
		SolveInfo info = engine.solve(toSolve);
		String state = engine.toJSON(Prolog.INCLUDE_KB_IN_SERIALIZATION);
		return new PrologSolution(info, state);
	}
	
	/**
	 * Gets the next solution given the state of the engine as JSON.
	 * 
	 * @param engineStateJson : the JSON serialization of the engine state.
	 * @return a PrologSolution object containing the SolveInfo of the solution
	 * and the serialized state of the engine as JSON.
	 * @throws NoMoreSolutionException
	 */
	public PrologSolution solveNext(String engineStateJson) throws NoMoreSolutionException {
		engine = Prolog.fromJSON(engineStateJson);
		SolveInfo info = engine.solveNext();
		String state = engine.toJSON(Prolog.INCLUDE_KB_IN_SERIALIZATION);
		return new PrologSolution(info, state);
	}
	
	/**
	 * Gets numSolutions solutions or all the remaining solutions if the solutions
	 * remaining are less than numSolutions.
	 * engineStatusJson can be null at the first request of the session.
	 * 
	 * @param toSolve : the goal to solve as String.
	 * @param numSolutions : the number of solutions required.
	 * @param engineStateJson : the JSON serialization of the engine state. It needs to be null
	 * if it's the first invocation of the session.
	 * @return a PrologSolution object containing the SolveInfo of all the solutions
	 * and the serialized state of the engine as JSON.
	 * @throws MalformedGoalException
	 * @throws NoMoreSolutionException
	 */
	public PrologSolution solveWithSession(String toSolve, int numSolutions, String engineStateJson) throws MalformedGoalException, NoMoreSolutionException {
		List<SolveInfo> lista = new ArrayList<SolveInfo>();
		if (engineStateJson == null) //first request
		{
			if (numSolutions <= 0)
				throw new IllegalArgumentException("numSolutions must be >0");
			int solutionsToGo = numSolutions;
			loadConfiguration();
			lista.add(engine.solve(toSolve));
			solutionsToGo--;
			while (solutionsToGo > 0 && engine.hasOpenAlternatives())
			{
				lista.add(engine.solveNext());
				solutionsToGo--;
			}
		}
		else //subsequent request
		{
			if (numSolutions <= 0)
				throw new IllegalArgumentException("numSolutions must be >0");
			int solutionsToGo = numSolutions;
			engine = Prolog.fromJSON(engineStateJson);
			while (solutionsToGo > 0 && engine.hasOpenAlternatives())
			{
				lista.add(engine.solveNext());
				solutionsToGo--;
			}
		}
		String finalState = engine.toJSON(Prolog.INCLUDE_KB_IN_SERIALIZATION);
		return new PrologSolution(lista, finalState);
	}
	
	/**
	 * Gets all the remaining solutions.
	 * engineStatusJson can be null at the first request of the session.
	 * 
	 * @param toSolve : the goal to solve as String.
	 * @param engineStateJson : the JSON serialization of the engine state. It needs to be null
	 * if it's the first invocation of the session.
	 * @return a PrologSolution object containing the SolveInfo of all the solutions
	 * and the serialized state of the engine as JSON.
	 * @throws MalformedGoalException
	 * @throws NoMoreSolutionException
	 */
	public PrologSolution solveAllWithSession(String toSolve, String engineStateJson) throws NoMoreSolutionException, MalformedGoalException {
		List<SolveInfo> lista = new ArrayList<SolveInfo>();
		if (engineStateJson == null) //first request
		{
			loadConfiguration();
			lista.add(engine.solve(toSolve));
			while (engine.hasOpenAlternatives())
			{
				lista.add(engine.solveNext());
			}
		}
		else //subsequent request
		{
			engine = Prolog.fromJSON(engineStateJson);
			while (engine.hasOpenAlternatives())
			{
				lista.add(engine.solveNext());
			}
		}
		String finalState = engine.toJSON(Prolog.INCLUDE_KB_IN_SERIALIZATION);
		return new PrologSolution(lista, finalState);
	}
	
	
	/**
	 * Gets numSolutions solutions or all the remaining solutions if the solutions
	 * remaining are less than numSolutions.
	 * 
	 * @param toSolve : the goal to solve as String.
	 * @param numSolutions : the number of solutions required.
	 * @return a List<SolveInfo> containing the SolveInfo of all the solutions.
	 * @throws MalformedGoalException
	 * @throws NoMoreSolutionException
	 */
	public List<SolveInfo> solve(String toSolve, int numSolutions) throws MalformedGoalException, NoMoreSolutionException {
		List<SolveInfo> lista = new ArrayList<SolveInfo>();
		if (numSolutions <= 0)
			throw new IllegalArgumentException("numSolutions must be >0");
		int solutionsToGo = numSolutions;
		loadConfiguration();
		lista.add(engine.solve(toSolve));
		solutionsToGo--;
		while (solutionsToGo > 0 && engine.hasOpenAlternatives())
		{
			lista.add(engine.solveNext());
			solutionsToGo--;
		}
		return lista;
	}
	
	/**
	 * Gets all the solutions for the provided goal.
	 * 
	 * @param toSolve : the goal to solve as String.
	 * @return a List<SolveInfo> containing the SolveInfo of all the solutions.
	 * @throws MalformedGoalException
	 * @throws NoMoreSolutionException
	 */
	public List<SolveInfo> solveAll(String toSolve) throws MalformedGoalException, NoMoreSolutionException {
		List<SolveInfo> lista = new ArrayList<SolveInfo>();
		loadConfiguration();
		lista.add(engine.solve(toSolve));
		while (engine.hasOpenAlternatives())
		{
			lista.add(engine.solveNext());
		}
		return lista;
	}
	
	private void refreshAssertions() {
//		if(this.currentAssertions.equals(engineState.getCurrentAssertions()))
//			return;
		
		currentAssertions.forEach(assertion -> {
			try {
				engine.solve("retract( ("+assertion+") ).");
			} catch (MalformedGoalException e) {
				e.printStackTrace();
			}
		});		
		
		currentAssertions = engineState.getCurrentAssertions();

		currentAssertions.forEach(assertion -> {
			try {
				engine.solve("assert( ("+assertion+") ).");
			} catch (MalformedGoalException e) {
				e.printStackTrace();
			}
		});
	}
    

}
