package it.unibo.alice.tuprolog.ws.core;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Schedule;
import javax.ejb.Singleton;

import org.jose4j.lang.JoseException;

import com.google.gson.JsonObject;



/**
 * This component represent the shared state of the single StatelessEngine, in particular
 * it manages the list of assertions generated from the data and invalidates the current active sessions
 * every time the data changes.
 * 
 * @author Andrea Muccioli
 *
 */
@Singleton
@LocalBean
public class EngineState {

	
	List<String> currentAssertions;
	
	@EJB
	private DataSupplier dataSupplier;
	
	@EJB
	private it.unibo.alice.tuprolog.ws.security.SecurityManager security;

    public EngineState() {
    	
    	
    }
    
    /**
     * Initializes the EngineState.
     * 
     */
    @PostConstruct
    public void initialize() {
    	invalidateCurrentSessions();
    	currentAssertions = getAssertionsFromData(dataSupplier.getData());
    }
    
    
    /**
     * Gets the current list of assertions.
     * 
     * @return the List of the current assertions.
     */
    public List<String> getCurrentAssertions() {
    	return currentAssertions;
    }
    
    
    /**
     * Replaces the current assertions with new ones based on the data obtained
     * from DataSupplier.</br></br>
     * 
     * Note: currently scheduled to execute every minute, for testing purposes.
     * 
     */
    @Schedule(minute = "*/1", hour = "*", persistent = false)
	private void refreshAssertions() {
    	invalidateCurrentSessions();
		currentAssertions = new ArrayList<String>();
		List<JsonObject> data = dataSupplier.getData();
		currentAssertions = this.getAssertionsFromData(data);
	
	}
    
    
	/**
	 * Obtains a List of assertions in the form of "assert( (dataName(X) :- X is dataValue) )"
	 * generated starting from the information contained in the given List of JsonObject.</br>
	 * Every JsonObject contains three fields:</br>
	 * 	-"name" : an identifying name for the data.</br>
	 * 	-"type" : the type of the data (currently only int, double, boolean).</br>
	 * 	-"value: : the value of the data.
	 * 
	 * @param list : a List of JsonObject containing the required data.
	 * @return a List of assertion based on the data.
	 */
	private List<String> getAssertionsFromData(List<JsonObject> list) {
		List<String> toReturn = new ArrayList<String>();
		list.forEach(element -> {
			String name = element.get("name").getAsString();
			String type = element.get("type").getAsString();
			if (type.equals("boolean"))
			{
				toReturn.add(name+" :- "+element.get("value").getAsBoolean());
			}
			else if (type.equals("int"))
			{
				toReturn.add(name+"(X) :- X is "+element.get("value").getAsInt());
			}
			else if (type.equals("double"))
			{
				toReturn.add(name+"(X) :- X is "+element.get("value").getAsDouble());
			}
		});
		System.out.println("assertions: "+toReturn.toString());
		return toReturn;
	}
	
	/**
	 * Invalidate the current active sessions to maintain consistency.
	 * 
	 */
	private void invalidateCurrentSessions() {
		try {
			security.regenerateEngineKeys();
		} catch (JoseException e) {
			e.printStackTrace();
		}
		
	}
    
    

    
    
    

}
