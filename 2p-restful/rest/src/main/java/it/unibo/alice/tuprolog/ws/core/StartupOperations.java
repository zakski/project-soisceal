package it.unibo.alice.tuprolog.ws.core;


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import it.unibo.alice.tuprolog.ws.persistence.StorageService;
import it.unibo.alice.tuprolog.ws.persistence.User;
import it.unibo.alice.tuprolog.ws.security.Role;


/**
 * @author Andrea Muccioli
 *
 */
@Startup
@Singleton
@LocalBean
public class StartupOperations {
	
	@EJB
	private StorageService manager;

    public StartupOperations() {
    }
    
    
    /**
     * Executes startup operations for the server application.
     */
    @PostConstruct
    private void initialize() {
    	if (manager.getAllUsers().size() == 0)
    	{
    		List<User> list = readUserXMLConfiguration();
    		System.out.println("Utenti letti: "+list.toString());
    		list.forEach(user -> manager.addUser(user));
    	}
    }
    
    
    /**
     * Reads user credentials from the XML configuration file "user_configuration.xml",
     * contained in the deployment archive, and creates a list of User based
     * the information read.
     * 
     * @return A List of User containing the user information read from the
     * configuration file.
     */
    private List<User> readUserXMLConfiguration() {
		SAXBuilder builder = new SAXBuilder();
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("user_configuration.xml");
		Document doc;
		try {
			doc = builder.build(stream);
		} catch (JDOMException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		List<User> toReturn = new ArrayList<User>();
		List<Element> users = doc.getRootElement().getChildren("user");
		users.forEach(el -> {
			String username = el.getChildText("username");
			String password = el.getChildText("password");
			String roleString = el.getChildText("role");
			Role role = Role.valueOf(roleString.toUpperCase());
			toReturn.add(new User(username, password, role));
		});
		
		return toReturn;
    }
    
    
}
