package it.unibo.alice.tuprolog.ws.core;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Schedule;
import javax.ejb.Singleton;

import com.google.gson.JsonObject;

import alice.tuprolog.MalformedGoalException;
import alice.tuprolog.Prolog;


@Singleton
@LocalBean
public class EngineState {

	
	List<String> currentAssertions;
	
	@EJB
	private DataSupplier dataSupplier;

    public EngineState() {
    	
    	
    }
    
    @PostConstruct
    public void initialize() {
    	currentAssertions = getAssertionsFromData(dataSupplier.getData());
    }
    
    
    public List<String> getCurrentAssertions() {
    	return currentAssertions;
    }
    
    
    @Schedule(minute = "*/1", hour = "*", persistent = false)
	private void refreshAssertions() {
		currentAssertions = new ArrayList<String>();
		List<JsonObject> data = dataSupplier.getData();
		currentAssertions = this.getAssertionsFromData(data);
	
	}
    
    
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
    
    

    
    
    

}
