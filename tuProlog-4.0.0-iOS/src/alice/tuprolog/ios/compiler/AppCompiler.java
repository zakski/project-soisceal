package alice.tuprolog.ios.compiler;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class AppCompiler 
{
	
	/**
     * @author Alberto Sita
     * 
     */
	
	public static String PATH_TO_APP_BINARIES = "./.bin";
	public static String PROJECT_DEPENDENCY = ":../tuProlog-4.0.0/bin/";
	
	public static String ROBOVM_VERSION = "2.3.0-SNAPSHOT";
	public static String BUGVM_VERSION = "1.1.19-SNAPSHOT"; //1.2.2-SNAPSHOT IN FUTURO... vedere sito bugvm.com

	public static void compile(String tool, String arch) 
	{
	    grantPermissions(tool);
	    compileApp(tool, arch);
	}
	
	private static void grantPermissions(String tool) 
	{
    	try 
    	{
    		if(tool.equalsIgnoreCase("bugvm"))
    		{
    			Runtime.getRuntime().exec("chmod u+x ./.BugVM_SDK/bin/bugvm");
    			Runtime.getRuntime().exec("chmod u+x ./.BugVM_SDK/bin/bugvm-device");
    			Runtime.getRuntime().exec("chmod u+x ./.BugVM_SDK/bin/bugvm-sim");
    		}
    		else
    		{
    			Runtime.getRuntime().exec("chmod u+x ./.RoboVM_SDK/bin/robovm");
    			Runtime.getRuntime().exec("chmod u+x ./.RoboVM_SDK/bin/simlauncher");
    		}
		} 
    	catch (IOException e) 
    	{
			e.printStackTrace();
		}
	}

	private static void compileApp(String tool, String arch) 
	{
		String command = "";
		if(tool.equalsIgnoreCase("bugvm"))
			command = "./.BugVM_SDK/bin/bugvm -config ./.BugVM_SDK/bugvm-config/bugvm.xml -arch "+arch+" -cp ./.BugVM_SDK/lib/bugvm-apple.jar:./.BugVM_SDK/lib/bugvm-rt.jar:"+PATH_TO_APP_BINARIES+"-bugvm/"+PROJECT_DEPENDENCY+":./lib/gson-2.6.2.jar: -verbose -run";
		else
			command = "./.RoboVM_SDK/bin/robovm -config ./.RoboVM_SDK/robovm-config/robovm.xml -arch "+arch+" -cp ./.RoboVM_SDK/lib/robovm-objc.jar:./.RoboVM_SDK/lib/robovm-cocoatouch.jar:"+PATH_TO_APP_BINARIES+"-robovm/"+PROJECT_DEPENDENCY+":./lib/gson-2.6.2.jar: -verbose -run";
		paketizeBinaries(tool);
	    launchCompilationProcess(tool, command);
	}
	
	private static void launchCompilationProcess(String tool, String command)
	{
		String line;
		try
		{	
			Process process = Runtime.getRuntime().exec(command);
			InputStream stderr = process.getErrorStream();
			InputStream stdout = process.getInputStream();

			BufferedReader readerOut = new BufferedReader (new InputStreamReader(stdout));
			BufferedReader readerErr = new BufferedReader (new InputStreamReader(stderr));
			
			if(tool.equalsIgnoreCase("bugvm"))
			{
				System.out.println("Compiling core tuProlog engine (located at: "+PROJECT_DEPENDENCY+") with BugVM "+BUGVM_VERSION+"\n");
			
				while ((line = readerOut.readLine ()) != null) 
					System.out.println("BugVM: " + line);
        
				while ((line = readerErr.readLine ()) != null) 
					System.out.println("BugVM_ERR: " + line);
			}
			else
			{
				System.out.println("Compiling core tuProlog engine (located at: "+PROJECT_DEPENDENCY+") with RoboVM "+ROBOVM_VERSION+"\n");
				
				while ((line = readerOut.readLine ()) != null) 
					System.out.println("RoboVM: " + line);
	        
				while ((line = readerErr.readLine ()) != null) 
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
	
	public static void createScriptAndCompile(String tool, boolean onRealDevice, String device)
	{
		String arch = "";
		
		if(onRealDevice && (device.equals("iPhone_5") || device.equals("iPhone_5C")))
			arch="thumbv7";
		else if(onRealDevice && (device.equals("iPhone_5S") || device.equals("iPhone_SE") || device.equals("iPhone_6") || device.equals("iPhone_6S") || device.equals("iPhone_6_Plus") || device.equals("iPhone_6S_Plus") || (device.equals("iPhone_7")) || (device.equals("iPhone_7_Plus"))))
			arch="arm64";
		else if(!onRealDevice && (device.equals("iPhone_5") || device.equals("iPhone_5C")))
			arch="x86";
		else if(!onRealDevice && (device.equals("iPhone_5S") || device.equals("iPhone_SE") || device.equals("iPhone_6") || device.equals("iPhone_6S") || device.equals("iPhone_6_Plus") || device.equals("iPhone_6S_Plus") || (device.equals("iPhone_7")) || (device.equals("iPhone_7_Plus"))))
			arch="x86_64";
		else
			System.out.println("\nParams not valid!\n");
		
		PrintWriter writer = null;
		
		if(tool.equalsIgnoreCase("bugvm"))
		{
			try
			{
				writer = new PrintWriter("./.bugvm.sh", "UTF-8");
			} 
			catch (FileNotFoundException | UnsupportedEncodingException e) 
			{
				System.out.println("Problems -> "+e.getMessage());
			}
		
			if(!onRealDevice)
				System.out.println("\n!! WARNING: Simulator will open on iPhone 6 !!\n");
			writer.println("./.BugVM_SDK/bin/bugvm -config ./.BugVM_SDK/bugvm-config/bugvm.xml -arch "+arch+" -cp ./.BugVM_SDK/lib/bugvm-apple.jar:./.BugVM_SDK/lib/bugvm-rt.jar:"+PATH_TO_APP_BINARIES+"-bugvm/"+PROJECT_DEPENDENCY+":./lib/gson-2.6.2.jar: -verbose -run");
		}
		else
		{
			try
			{
				writer = new PrintWriter("./.robovm.sh", "UTF-8");
			} 
			catch (FileNotFoundException | UnsupportedEncodingException e) 
			{
				System.out.println("Problems -> "+e.getMessage());
			}
			
			if(onRealDevice)
				writer.println("./.RoboVM_SDK/bin/robovm -config ./.RoboVM_SDK/robovm-config/robovm.xml -arch "+arch+" -cp ./.RoboVM_SDK/lib/robovm-objc.jar:./.RoboVM_SDK/lib/robovm-cocoatouch.jar:"+PATH_TO_APP_BINARIES+"-robovm/"+PROJECT_DEPENDENCY+":./lib/gson-2.6.2.jar: -verbose -run");
			else
			{
				System.out.println("\n!! WARNING: Simulator will open on iPhone 5 !!\n");
				writer.println("./.RoboVM_SDK/bin/robovm -config ./.RoboVM_SDK/robovm-config/robovm.xml -arch x86 -cp ./.RoboVM_SDK/lib/robovm-objc.jar:./.RoboVM_SDK/lib/robovm-cocoatouch.jar:"+PATH_TO_APP_BINARIES+"-robovm"+PROJECT_DEPENDENCY+":./lib/gson-2.6.2.jar: -verbose -run");
			}
		}
		writer.close();
		
		try 
		{
			launchFromShellScript(tool);
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
	}
	
	private static void launchFromShellScript(String tool) throws InterruptedException
	{
		try 
		{
			grantPermissions(tool);
			if(tool.equalsIgnoreCase("bugvm"))
				Runtime.getRuntime().exec("chmod u+x ./.bugvm.sh");
			else
				Runtime.getRuntime().exec("chmod u+x ./.robovm.sh");
		} 
		catch (IOException e)
		{
			e.printStackTrace();
			Thread.sleep(1500);
			System.exit(-1);
		}
		
		String command = "";
		if(tool.equalsIgnoreCase("bugvm"))
			command = "./.bugvm.sh";
		else
			command = "./.robovm.sh";
		
		launchCompilationProcess(tool, command);
	}
	
	public static void paketizeBinaries(String compiler)
	{
		try
		{
			Runtime.getRuntime().exec("cp -r ./bin/alice/tuprolog/ios/app/"+compiler+"/ ./"+PATH_TO_APP_BINARIES+"-"+compiler+"/alice/tuprolog/ios/app/"+compiler+"/");
			Runtime.getRuntime().exec("cp -r ./bin/alice/tuprolog/ios/app/sharedclasses/ ./"+PATH_TO_APP_BINARIES+"-"+compiler+"/alice/tuprolog/ios/app/sharedclasses/");
			Runtime.getRuntime().exec("cp -r ./bin/alice/tuprolog/lib/"+compiler+"/ ./"+PATH_TO_APP_BINARIES+"-"+compiler+"/alice/tuprolog/lib/"+compiler+"/");
			Runtime.getRuntime().exec("cp -r ./bin/alice/tuprolog/lib/sharedclasses/ ./"+PATH_TO_APP_BINARIES+"-"+compiler+"/alice/tuprolog/lib/sharedclasses/");
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
