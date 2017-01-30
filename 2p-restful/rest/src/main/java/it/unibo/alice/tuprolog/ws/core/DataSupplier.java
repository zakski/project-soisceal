package it.unibo.alice.tuprolog.ws.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import com.google.gson.JsonObject;

/**
 * This component is meant to offer functionalities to fetch data from 
 * a generic source with a pull behavior.</br></br>
 * 
 * Note: currently implemented to work with random generated test data.
 * 
 * @author Andrea Muccioli
 *
 */
@Stateless
@LocalBean
public class DataSupplier {
	
	/**
	 * Gets a List of JsonObject containing the data obtained from a generic source.
	 * Every JsonObject contains three fields:</br>
	 * 	-"name" : an identifying name for the data.</br>
	 * 	-"type" : the type of the data (currently only int, double, boolean).</br>
	 * 	-"value: : the value of the data.</br></br>
	 * 
	 * NOTE: Currently, for testing purposes, the data are randomly generated at each invocation based on
	 * the configuration file "data_configuration.xml" contained in the deploy archive.
	 * 
	 * @return a List<JsonObject> containing the data
	 */
	public List<JsonObject> getData()
	{
		SAXBuilder builder = new SAXBuilder();
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("data_configuration.xml");
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
		
		List<JsonObject> lista = new ArrayList<JsonObject>();
		Random r = new Random();
		List<Element> elements = doc.getRootElement().getChildren();
		elements.forEach(element -> {
			JsonObject obj = new JsonObject();
			String name = element.getChildText("name");
			String type = element.getChildText("type");
			obj.addProperty("name", name);
			obj.addProperty("type", type);
			if (type.equals("int")) {
				int from = Integer.parseInt(element.getChild("range").getChildText("from"));
				int to = Integer.parseInt(element.getChild("range").getChildText("to"));
				int result = r.nextInt(1+to-from) + from;
				obj.addProperty("value", result);
			}
			else if (type.equals("double")) {
				double from = Double.parseDouble(element.getChild("range").getChildText("from"));
				double to = Double.parseDouble(element.getChild("range").getChildText("to"));
				double result = (r.nextDouble()*(to-from)) + from;
				obj.addProperty("value", result);
			}
			else if (type.equals("boolean")) {
				boolean result = r.nextBoolean();
				obj.addProperty("value", result);
			}
			lista.add(obj);
		});
		System.out.println("data list: "+lista.toString());
    	return lista;
	}

}
