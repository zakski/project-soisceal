package alice.tuprolog.ios.launcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class RoboVM_Launcher 
{
	
	/**
     * @author Alberto Sita
     * 
     */
	
	private static String ROBOVM_VERSION = "2.2.1-SNAPSHOT";
	
	public static void main(String[] args) 
	{
	    grantPermissions();
	    compileIOSApp();
	}
	
	private static void grantPermissions() 
	{
    	try 
    	{
    		Runtime.getRuntime().exec("chmod u+x ./robovm.sh");
			Runtime.getRuntime().exec("chmod u+x ./RoboVM_SDK/bin/robovm");
			Runtime.getRuntime().exec("chmod u+x ./RoboVM_SDK/bin/simlauncher");
		} 
    	catch (IOException e) 
    	{
			e.printStackTrace();
		}
	}

	private static void compileIOSApp() 
	{
		String line;
	    try 
	    {
	        Process process = Runtime.getRuntime().exec("./robovm.sh");
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();

            BufferedReader readerOut = new BufferedReader (new InputStreamReader(stdout));
            BufferedReader readerErr = new BufferedReader (new InputStreamReader(stderr));
            
            System.out.println("Compiling core tuProlog engine with RoboVM "+ROBOVM_VERSION+"\n");

            while ((line = readerOut.readLine ()) != null) 
            {
            	System.out.println ("RoboVM: " + line);
            }
            
            while ((line = readerErr.readLine ()) != null) 
            {
            	System.out.println ("RoboVM_ERR: " + line);
            }
            
            readerOut.close();
            readerErr.close();
	    } 
	    catch (IOException e) 
	    {
	        e.printStackTrace();
	    }
	}
}
