package alice.tuprolog.lib.sharedclasses;

import alice.tuprolog.Library;
import alice.tuprolog.Term;
import alice.tuprolog.ios.compiler.AppCompiler;

public class iOSInstallerLibrary extends Library //Experimental!
{
	
	/**
     * @author Alberto Sita
     * 
     */
	
	private static final long serialVersionUID = 1L;
	private static boolean ok = false;
	
	public boolean tuPrologOniPhone_3(Term compiler, Term type, Term device)
	{
		ok = false;
		
		if(!type.toString().equals("simulator") && !type.toString().equals("install"))
		{
			return false;
		}
		
		if(!device.toString().equals("iPhone_5") && !device.toString().equals("iPhone_5C") && !device.toString().equals("iPhone_5S") && !device.toString().equals("iPhone_SE") && !device.toString().equals("iPhone_6") && !device.toString().equals("iPhone_6S") && !device.toString().equals("iPhone_6_Plus") && !device.toString().equals("iPhone_6S_Plus") && !device.toString().equals("iPhone_7") && !device.toString().equals("iPhone_7_Plus"))
		{
			return false;
		}
			
		Runnable r = new Runnable()
		{
		    @Override
		    public void run() 
		    {
		    	try 
		    	{
		    		if(compiler.toString().equalsIgnoreCase("bugvm") || compiler.toString().equalsIgnoreCase("robovm"))
		    		{
		    			AppCompiler.createScriptAndCompile(compiler.toString().toLowerCase(),!type.toString().equals("simulator"), device.toString());
		    			ok = true;
		    		}
		    		else
		    			ok = false;
				} 
		    	catch (Exception e) 
		    	{
					e.printStackTrace();
				}
		    }
		};
		
		r.run();
		return ok;
	}
}