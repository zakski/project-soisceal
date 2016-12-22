package it.unibo.alice.tuprolog.ws.core;

import javax.ejb.Singleton;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Lock;
import javax.ejb.LockType;

import alice.tuprolog.InvalidTheoryException;
import alice.tuprolog.MalformedGoalException;
import alice.tuprolog.NoMoreSolutionException;
import alice.tuprolog.Prolog;
import alice.tuprolog.SolveInfo;
import alice.tuprolog.Term;
import alice.tuprolog.Theory;
import it.unibo.alice.tuprolog.ws.persistence.PrologConfiguration;
import it.unibo.alice.tuprolog.ws.persistence.StorageService;


@Singleton
@LocalBean
public class PrologEngine {
	
	private Prolog engine;
	
	@EJB
	private StorageService manager;

	
	@PostConstruct
	private void setup() {
		System.out.println("PrologEngine/ provo ad ottenere teoria");
		String theory = manager.getConfiguration().getTheory();
//		System.out.println("ottenuto: "+theory);
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
	
	
	
	
	@Lock(LockType.WRITE)
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
	
	public String getInfo() {
		StringBuilder sb = new StringBuilder();
		sb.append("StorageService is "+manager);
		return sb.toString();
	}
	
	@Lock(LockType.READ)
	public Theory getTheory() {
		return engine.getTheory();
	}
	
	@Lock(LockType.READ)
	public PrologConfiguration getConfiguration() {
		return manager.getConfiguration();
	}
	
	@Lock(LockType.WRITE)
	public SolveInfo solve(String toSolve) throws MalformedGoalException {
		return engine.solve(toSolve);
	}
	
	@Lock(LockType.WRITE)
	public SolveInfo solve(Term toSolve) {
		return engine.solve(toSolve);
	}
	
	@Lock(LockType.WRITE)
	public SolveInfo solveNext() throws NoMoreSolutionException {
		return engine.solveNext();
	}
	@Lock(LockType.WRITE)
	public List<SolveInfo> solve(String toSolve, int numSolutions) throws MalformedGoalException, NoMoreSolutionException {
		List<SolveInfo> lista = new ArrayList<SolveInfo>();
		if (numSolutions <= 0)
			throw new IllegalArgumentException("numSolutions must be >0");
		int solutionsToGo = numSolutions;
		lista.add(engine.solve(toSolve));
		solutionsToGo--;
		while (solutionsToGo > 0 && engine.hasOpenAlternatives())
		{
			lista.add(engine.solveNext());
			solutionsToGo--;
		}
		return lista;
	}
	@Lock(LockType.WRITE)
	public List<SolveInfo> solveAll(String toSolve) throws MalformedGoalException, NoMoreSolutionException {
		List<SolveInfo> lista = new ArrayList<SolveInfo>();
		lista.add(engine.solve(toSolve));
		while (engine.hasOpenAlternatives())
		{
			lista.add(engine.solveNext());
		}
		return lista;
	}

}
