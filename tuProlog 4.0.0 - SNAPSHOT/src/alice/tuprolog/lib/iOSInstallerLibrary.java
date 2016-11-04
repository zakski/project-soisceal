package alice.tuprolog.lib;

import alice.tuprolog.Library;
import alice.tuprolog.Term;
import alice.tuprolog.ios.annotations.RoboVM_Required;

@RoboVM_Required
public class iOSInstallerLibrary extends Library
{
	
	/**
     * @author Alberto Sita
     * 
     */
	
	private static final long serialVersionUID = 1L;
	
	public boolean tuPrologOniPhone_2(Term type, Term device) //it's a BETA!
	{
		if(!type.toString().equals("simulator") && !type.toString().equals("install"))
		{
			return false;
		}
		
		if(!device.toString().equals("iPhone_5") && !device.toString().equals("iPhone_5C") && !device.toString().equals("iPhone_5S") && !device.toString().equals("iPhone_SE") && !device.toString().equals("iPhone_6") && !device.toString().equals("iPhone_6S") && !device.toString().equals("iPhone_6_Plus") && !device.toString().equals("iPhone_6S_Plus") && !device.toString().equals("iPhone_7") && !device.toString().equals("iPhone_7_Plus"))
		{
			return false;
		}
		
		String[] args = new String[4];
		args[0] = "platform";
		args[1] = "ios";
		args[2] = type.toString();
		args[3] = device.toString();
		Runnable r = new Runnable()
		{
		    @Override
		    public void run() 
		    {
		    	try 
		    	{
					alice.tuprolog.ios.launcher.Shell_Launcher.main(args);
				} 
		    	catch (Exception e) 
		    	{
					e.printStackTrace();
				}
		    }
		};
		
		r.run();
		return true;
	}
}