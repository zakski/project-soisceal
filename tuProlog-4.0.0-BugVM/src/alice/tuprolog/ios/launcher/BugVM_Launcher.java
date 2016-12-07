package alice.tuprolog.ios.launcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import alice.tuprolog.ios.Main;
import alice.tuprolog.ios.annotations.iOS_BugVM_Deploy;

public class BugVM_Launcher 
{
	
	/**
     * @author Alberto Sita
     * 
     */
	
	public static String BUGVM_VERSION = "1.1.19-SNAPSHOT";
	public static String PROJECT_DEPENDENCY = "../tuProlog-4.0.0/bin/";
	
	public static boolean granted = false;
	
	public static void main(String[] args) 
	{
	    grantPermissions();
	    granted = true;
	    compileIOSApp();
	}
	
	private static void grantPermissions() 
	{
    	try 
    	{
			Runtime.getRuntime().exec("chmod u+x ./.BugVM_SDK/bin/bugvm");
			Runtime.getRuntime().exec("chmod u+x ./.BugVM_SDK/bin/bugvm-device");
			Runtime.getRuntime().exec("chmod u+x ./.BugVM_SDK/bin/bugvm-sim");
		} 
    	catch (IOException e) 
    	{
			e.printStackTrace();
		}
	}

	private static void compileIOSApp() 
	{
	    Class<Main> main = Main.class;
	    iOS_BugVM_Deploy ios_deploy = (iOS_BugVM_Deploy) main.getAnnotation(iOS_BugVM_Deploy.class);	
	    String arch = ios_deploy.forArch();	
	    String command = "./.BugVM_SDK/bin/bugvm -config ./.BugVM_SDK/bugvm-config/bugvm.xml -arch "+arch+" -cp ./.BugVM_SDK/lib/bugvm-apple.jar:./.BugVM_SDK/lib/bugvm-rt.jar:./bin/:"+BugVM_Launcher.PROJECT_DEPENDENCY+":./lib/gson-2.6.2.jar: -verbose -run";
	    launchCompilation(command);
	}

	public static void launchFromShell() throws InterruptedException
	{
		try 
		{
			if(!granted)
			{
				grantPermissions();
				granted = true;
			}
			
			Runtime.getRuntime().exec("chmod u+x ./.bugvm.sh");
		} 
		catch (IOException e)
		{
			e.printStackTrace();
			Thread.sleep(1500);
			System.exit(-1);
		}
		
		String command = "./.bugvm.sh";
		
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
        
			System.out.println("Compiling core tuProlog engine (located at: "+PROJECT_DEPENDENCY+") with BugVM "+BUGVM_VERSION+"\n");

			while ((line = readerOut.readLine ()) != null) 
			{
				System.out.println("BugVM: " + line);
			}
        
			while ((line = readerErr.readLine ()) != null) 
			{
				System.out.println("BugVM_ERR: " + line);
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
