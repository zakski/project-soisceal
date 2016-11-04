package alice.tuprolog.ios.launcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import alice.tuprolog.ios.Main;
import alice.tuprolog.ios.annotations.RoboVM_Required;
import alice.tuprolog.ios.annotations.iOS_Deploy;

@RoboVM_Required
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
			Runtime.getRuntime().exec("chmod u+x ./.RoboVM_SDK/bin/robovm");
			Runtime.getRuntime().exec("chmod u+x ./.RoboVM_SDK/bin/simlauncher");
		} 
    	catch (IOException e) 
    	{
			e.printStackTrace();
		}
	}

	private static void compileIOSApp() 
	{
	    Class<Main> main = Main.class;
	    iOS_Deploy ios_deploy = (iOS_Deploy) main.getAnnotation(iOS_Deploy.class);	
	    String arch = ios_deploy.forArch();	
	    String command = "./.RoboVM_SDK/bin/robovm -config ./.RoboVM_SDK/robovm-config/robovm.xml -arch "+arch+" -cp ./.RoboVM_SDK/lib/robovm-objc.jar:./.RoboVM_SDK/lib/robovm-cocoatouch.jar:./bin/: -verbose -run";
	    
	    launchCompilation(command);
	}

	public static void launchFromShell() throws InterruptedException
	{
		try 
		{
			Runtime.getRuntime().exec("chmod u+x ./.robovm.sh");
		} 
		catch (IOException e)
		{
			e.printStackTrace();
			Thread.sleep(1500);
			System.exit(-1);
		}
		
		String command = "./.robovm.sh";
		
		launchCompilation(command);
	}
	
	private static void launchCompilation(String command)
	{
		String line;
		try
		{	
			Process process = Runtime.getRuntime().exec(command);
			InputStream stderr = process.getErrorStream();
			InputStream stdout = process.getInputStream();

			BufferedReader readerOut = new BufferedReader (new InputStreamReader(stdout));
			BufferedReader readerErr = new BufferedReader (new InputStreamReader(stderr));
        
			System.out.println("Compiling core tuProlog engine with RoboVM "+ROBOVM_VERSION+"\n");

			while ((line = readerOut.readLine ()) != null) 
			{
				System.out.println("RoboVM: " + line);
			}
        
			while ((line = readerErr.readLine ()) != null) 
			{
				System.out.println("RoboVM_ERR: " + line);
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
