package it.unibo.alice.tuprolog.ws.persistence;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import it.unibo.alice.tuprolog.ws.security.Role;

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
//			goals.add("goal1");
//			goals.add("goal2");
//			goals.add("goals3");
			
			goals.add("assert( (sintomo(X) :- true + false) ).");
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
	
	public void addUser(User user) {
		if (user == null)
			throw new NullPointerException("User can't be null");
		if (!User.validateUsername(user.getUsername()))
			throw new IllegalArgumentException("Username is not valid");
		if (!User.validatePassword(user.getPassword()))
			throw new IllegalArgumentException("Password is not valid");
		em.persist(user);
		em.flush();
	}
	
	public void addUser(String username, String password, Role role) {
		if (!User.validateUsername(username))
			throw new IllegalArgumentException("Username is not valid");
		if (!User.validatePassword(password))
			throw new IllegalArgumentException("Password is not valid");
		User user = new User(username, password, role);
		em.persist(user);
		em.flush();
	}
	
	public void removeUser(String username) {
		User u = getUser(username);
		if (u == null)
			return;
		em.remove(u);
		em.flush();
	}
	
	public User getUser(String username) {
		User user = em.find(User.class, username);
		System.out.println("Trovato user: "+user);
		return user;
	}
	
	public List<User> getAllUsers() {
		Query q = em.createQuery("SELECT u FROM User u");
		return (List<User>) q.getResultList();
	}
	
	public Role getUserRole(String username) {
		User user = getUser(username);
		if (user == null)
			return null;
		return user.getRole();
	}
	
	public List<User> getUsersWithRole(Role role) {
		Query q = em.createQuery("SELECT u FROM User u WHERE u.role = :userRole")
				.setParameter("userRole", role);
		return (List<User>) q.getResultList();
	}
	


}
