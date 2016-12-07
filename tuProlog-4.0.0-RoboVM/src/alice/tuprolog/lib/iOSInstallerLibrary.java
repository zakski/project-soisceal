package alice.tuprolog.lib;

import alice.tuprolog.Library;
import alice.tuprolog.Term;

public class iOSInstallerLibrary extends Library //Experimental!
{
	
	/**
     * @author Alberto Sita
     * 
     */
	
	private static final long serialVersionUID = 1L;
	
	public boolean tuPrologOniPhoneWithRoboVM_2(Term type, Term device)
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