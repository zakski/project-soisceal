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

@Stateless
@LocalBean
public class DataSupplier {
	
//	public List<JsonObject> getData()
//	{
//		List<JsonObject> lista = new ArrayList<JsonObject>();
//		Random r = new Random();
//		JsonObject obj = new JsonObject();
//		obj.addProperty("name", "peso_specifico");
//		obj.addProperty("type", "int");
//		obj.addProperty("value", r.nextInt(10)+1027);
//		lista.add(obj);
//		
//		JsonObject obj2 = new JsonObject();
//		obj2.addProperty("name", "sodiuria");
//		obj2.addProperty("type", "int");
//		obj2.addProperty("value", r.nextInt(10)+203);
//		lista.add(obj2);
//		
//		JsonObject obj3 = new JsonObject();
//		obj3.addProperty("name", "globuli_bianchi");
//		obj3.addProperty("type", "boolean");
//		obj3.addProperty("value", r.nextBoolean());
//		lista.add(obj3);
//		
//		JsonObject obj4 = new JsonObject();
//		obj4.addProperty("name", "sangue_feci");
//		obj4.addProperty("type", "boolean");
//		obj4.addProperty("value", r.nextBoolean());
//		lista.add(obj4);
//		
//		JsonObject obj5 = new JsonObject();
//		obj5.addProperty("name", "chetoni");
//		obj5.addProperty("type", "boolean");
//		obj5.addProperty("value", r.nextBoolean());
//		lista.add(obj5);
//		
//		JsonObject obj6 = new JsonObject();
//		obj6.addProperty("name", "glucosio");
//		obj6.addProperty("type", "boolean");
//		obj6.addProperty("value", true);
//		lista.add(obj6);
//		
//		return lista;
//	}
	

	
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
