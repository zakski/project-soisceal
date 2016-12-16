package it.unibo.alice.tuprolog.ws.persistence;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * @author Andrea Muccioli
 *
 */
@Stateless
@LocalBean
public class StorageService {
	
	@PersistenceContext(unitName = "myPU")
    EntityManager em;
	
	public StorageService() {
		
	}
	
	/**
	 * Get the configuration entity managed by the EntityManager.
	 * 
	 * @return the PrologConfiguration.
	 */
	public PrologConfiguration getConfiguration() {
		PrologConfiguration existingConfig = em.find(PrologConfiguration.class, "1");
		System.out.println("Trovato: " +existingConfig);
		if (existingConfig == null)
		{
			List<String> goals = new ArrayList<String>();
			goals.add("goal1");
			goals.add("goal2");
			goals.add("goals3");
			existingConfig = new PrologConfiguration("sono una teoria", "sono una configurazione", goals);
			em.persist(existingConfig);
		}
		em.flush();
		return existingConfig;
	}
	
	/**
	 * Reset the configuration entity managed by the EntityManager.
	 * 
	 */
	public void resetConfiguration() {
		PrologConfiguration existingConfig = em.find(PrologConfiguration.class, "1");
		if (existingConfig != null)
		{
			em.remove(existingConfig);
		}
		PrologConfiguration config = new PrologConfiguration();
		em.persist(config);
		em.flush();
	}
	
	public PrologConfiguration refreshConfiguration(PrologConfiguration config) {
		em.refresh(config);
		return config;

	}
	


}
