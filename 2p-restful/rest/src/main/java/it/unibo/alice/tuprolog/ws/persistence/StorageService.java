package it.unibo.alice.tuprolog.ws.persistence;

import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import it.unibo.alice.tuprolog.ws.security.Role;


/**
 * This component is meant to be the only access point to the Persistence Context
 * of the entire service application. It provides all the methods for the other components
 * to find, add, remove entities to the Persistence Context. A component that needs these
 * functionalities should inject StorageService and use the provided methods rather than
 * inject and use directly the EntityManager.
 * 
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
	 * Gets the configuration entity managed by the EntityManager.
	 * Note: Currently the server supports only a single PrologConfiguration
	 * with static id = 1.
	 * 
	 * @return the PrologConfiguration.
	 */
	public PrologConfiguration getConfiguration() {
		PrologConfiguration existingConfig = em.find(PrologConfiguration.class, "1");
		System.out.println("Trovato: " +existingConfig);
		return existingConfig;
	}
	
	/**
	 * Resets the configuration entity managed by the EntityManager.
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
	
	
	/**
	 * If valid, and if not already present, persists the User
	 * received as parameter.
	 * 
	 * @param user : the User to persist.
	 */
	public void addUser(User user) {
		if (user == null)
			throw new NullPointerException("User can't be null");
		if (!User.validateUsername(user.getUsername()))
			throw new IllegalArgumentException("Username is not valid");
		if (!User.validatePassword(user.getPassword()))
			throw new IllegalArgumentException("Password is not valid");
		if (getUser(user.getUsername()) != null)
			throw new IllegalArgumentException("User already exists");
			
		em.persist(user);
		em.flush();
	}
	
	/**
	 * If valid, and if not already present, persists the User with the
	 * credentials received as parameter.
	 * 
	 * @param username : the username of the User to persist.
	 * @param password : the password of the User to persist.
	 * @param role : the Role of the User to persist.
	 */
	public void addUser(String username, String password, Role role) {
		if (!User.validateUsername(username))
			throw new IllegalArgumentException("Username is not valid");
		if (!User.validatePassword(password))
			throw new IllegalArgumentException("Password is not valid");
		if (getUser(username) != null)
			throw new IllegalArgumentException("User already exists");
		
		User user = new User(username, password, role);
		em.persist(user);
		em.flush();
	}
	
	
	/**
	 * Removes the User associated with the username from the
	 * persistence context.
	 * 
	 * @param username : the username of the User to remove.
	 */
	public void removeUser(String username) {
		User u = getUser(username);
		if (u == null)
			return;
		em.remove(u);
		em.flush();
	}
	
	/**
	 * Finds and returns the User associated with the given username.
	 * 
	 * @param username : the username of the User to get.
	 * @return the User entity associated with the given username or null if no User with this username exists.
	 */
	public User getUser(String username) {
		User user = em.find(User.class, username);
		System.out.println("Trovato user: "+user);
		return user;
	}
	
	
	/**
	 * Gets a List of all the Users currently persisted in the persistence context.
	 * 
	 * @return a List<User> with all the Users currently persisted.
	 */
	public List<User> getAllUsers() {
		Query q = em.createQuery("SELECT u FROM User u");
		return (List<User>) q.getResultList();
	}
	
	
	/**
	 * Gets the current Role of the User associated with the given username.
	 * 
	 * @param username : the username of the User to check.
	 * @return the Role of the User, or null if no User with the given username exists.
	 */
	public Role getUserRole(String username) {
		User user = getUser(username);
		if (user == null)
			return null;
		return user.getRole();
	}
	
	
	/**
	 * Gets all the Users with the given Role.
	 * 
	 * @param role : the Role of the users to look for.
	 * @return a List<User> of the users with the given Role.
	 */
	public List<User> getUsersWithRole(Role role) {
		Query q = em.createQuery("SELECT u FROM User u WHERE u.role = :userRole")
				.setParameter("userRole", role);
		return (List<User>) q.getResultList();
	}
	


}
