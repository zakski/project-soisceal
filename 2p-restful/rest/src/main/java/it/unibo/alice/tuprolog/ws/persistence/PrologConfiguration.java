package it.unibo.alice.tuprolog.ws.persistence;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.google.gson.Gson;

/**
 * This entity specifies how the application persists the set configuration.
 * The class contains the needed getters and setters to access the fields.</br>
 * Currently the only properties that affects the behaviour of the server are
 * "theory" and "goals"; "configuration" is a placeholder for future uses, and
 * "configurationId" is needed for the data source, but the server actually
 * supports a single configuration (with configurationId = 1).
 * 
 * @author Andrea Muccioli
 *
 */
@Entity
@Table(name = "CONFIGURATION")
public class PrologConfiguration implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name="configurationId")
	private String configurationId;
	
	/**
	 * NOTE: the max length of the theory configured is 2000 characters.</br>
	 * The max length of a VARCHAR field in Apache Derby (where this property
	 * is mapped) is 32672 characters. Change the value for different needs.
	 */
	@Column(name = "theory", length=2000)
	private String theory;
	
	@Column(name = "configuration")
	private String configuration;
	
	@ElementCollection
	@CollectionTable(name ="LIST_GOALS")
	private List<String> goals;
	
	public PrologConfiguration() {
		configurationId = "1";
		theory = "";
		configuration = "";
		goals = new ArrayList<String>();
	}
	
	public PrologConfiguration(String theory, String configuration, List<String> goals) {
		configurationId = "1";
		this.theory = theory;
		this.configuration = configuration;
		this.goals = goals;
	}
	
	

	public String getTheory() {
		return theory;
	}

	public void setTheory(String theory) {
		this.theory = theory;
	}

	public String getConfiguration() {
		return configuration;
	}

	public void setConfiguration(String configuration) {
		this.configuration = configuration;
	}

	public List<String> getGoals() {
		return goals;
	}

	public void setGoals(List<String> goals) {
		this.goals = goals;
	}
	
	public void addGoal(String goal) {
		this.goals.add(goal);
	}
	
	public void removeGoal(String goal) {
		int i;
		if ((i = goals.indexOf(goal)) != -1)
			goals.remove(i);
	}
	
	public void removeAt(int index) {
		goals.remove(index);
	}
	
	public String toJSON() {
		Gson gson = new Gson();
		String json = gson.toJson(this);
		return json;
	}
	
	public static PrologConfiguration fromJSON(String json) {
		Gson gson = new Gson();
		return gson.fromJson(json, PrologConfiguration.class);
	}
	
	
}