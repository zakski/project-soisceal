package it.unibo.alice.tuprolog.ws.core;


import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.annotation.PostConstruct;
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


/**
 * @author Andrea Muccioli
 *
 */
@Startup
@Singleton
@LocalBean
public class StartupOperations {
	

    public StartupOperations() {
    }
    
    /**
     * Loads the properties file from the deploy archive and bind it with the
     * JNDI name "user/properties".
     */
    @PostConstruct
    private void initialize() {
    	Properties p = readUserXMLConfiguration();
    	System.out.println("Proprieta lette: "+p.toString());
    	try {
			new InitialContext().rebind("user/properties", p);
			System.out.println("rebind ok");
		} catch (NamingException e) {
			e.printStackTrace();
		}
    }
    
    /**
     * NOTE: This method is still incomplete.
     * Reads user credentials from the XML configuration file "user_configuration.xml".
     * Currently the method only reads the information of the the first declared user.
     * 
     * @return The Properties object containing the user information read from the
     * configuration file.
     */
    private Properties readUserXMLConfiguration() {
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
		
		Element user = doc.getRootElement().getChild("user");
    	Properties p = new Properties();
    	p.put("configuration.admin.username", user.getChildText("username"));
    	p.put("configuration.admin.password", user.getChildText("password"));
    	p.put("configuration.admin.role", user.getChildText("role"));
    	
    	return p;
		
    }
    
    
}
