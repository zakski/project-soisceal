package alice.tuprolog.lib;

import java.lang.reflect.Method;
import alice.tuprolog.Term;
import alice.tuprolog.exceptions.JavaException;

public class LambdaPlugIn {

	@SuppressWarnings("unchecked")
	public <T> boolean resolve(Term interfaceName, Term implementation, Term id, OOLibrary ooLibrary) throws JavaException {
		try {
    		ooLibrary.counter++;
    		String target_class=(interfaceName.toString()).substring(1, interfaceName.toString().length()-1);
    		String lambda_expression=(implementation.toString()).substring(1, implementation.toString().length()-1);
    		target_class = org.apache.commons.lang3.StringEscapeUtils.unescapeJava(target_class);
    		lambda_expression = org.apache.commons.lang3.StringEscapeUtils.unescapeJava(lambda_expression);
    	
    		Class<?> lambdaMetaFactory = alice.util.proxyGenerator.Generator.make(
				ClassLoader.getSystemClassLoader(),
		        "MyLambdaFactory"+ooLibrary.counter,
		        "" +           
		            "public class MyLambdaFactory"+ooLibrary.counter+" {\n" +
		            "  public "+target_class+" getFunction() {\n" + 
				    " 		return "+lambda_expression+"; \n"+ 
		            "  }\n" +
		            "}\n"
    		);
		
    		Object myLambdaFactory = lambdaMetaFactory.newInstance(); 
    		Class<?> myLambdaClass = myLambdaFactory.getClass(); 
    		Method[] allMethods = myLambdaClass.getDeclaredMethods();
    		T myLambdaInstance=null; 
    		for (Method m : allMethods) {
    			String mname = m.getName();
    			if (mname.startsWith("getFunction"))
    				myLambdaInstance=(T) m.invoke(myLambdaFactory);
    		}
    		id = id.getTerm();
    		if (ooLibrary.bindDynamicObject(id, myLambdaInstance))
    			return true;
    		else
    			throw new JavaException(new Exception());
    	} catch (Exception ex) {
            throw new JavaException(ex);
        }
	}
	
}
