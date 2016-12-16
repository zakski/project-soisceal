package it.unibo.alice.tuprolog.ws.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.naming.InitialContext;
import javax.naming.NamingException;


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
    	InputStream is =  this.getClass().getClassLoader().getResourceAsStream("config.properties");
    	Properties p = new Properties();
    	try {
			p.load(is);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
    	System.out.println("Proprieta lette: "+p.toString());
    	try {
			new InitialContext().rebind("user/properties", p);
			System.out.println("rebind ok");
		} catch (NamingException e) {
			e.printStackTrace();
		}
    	
    	
    }
}
