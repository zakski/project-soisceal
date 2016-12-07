package alice.tuprolog.ios.launcher;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class Shell_Launcher 
{
	
	/**
     * @author Alberto Sita
     * 
     */

	public static void main(String[] args)
	{
		if(args.length<2)
		{
			System.out.println("\nParams not valid!\n");
			usage();
		}
		else
		{
			if(args[0].equals("platform"))
			{
				if(args[1].equals("pc") || args[1].equals("mac"))
				{
					alice.tuprologx.ide.GUILauncher.main(args);
				}
				else if(args[1].equals("ios"))
				{
					if(args[2].equals("simulator"))
					{
						generateRoboVMShellScript(false, args[3]);
					}
					else if(args[2].equals("install"))
					{
						generateRoboVMShellScript(true, args[3]);
					}
					else
					{
						System.out.println("\nParams not valid!\n");
					}
				}
				else
				{
					System.out.println("\nParams not valid!\n");
					usage();
				}
			}
			else
			{
				System.out.println("\nParams not valid!\n");
				usage();
			}
		}
	}

	private static void usage() 
	{
		System.out.println("Available commands:");
		System.out.println("java -jar tuprolog.jar platform [pc|mac]");
		System.out.println("java -jar tuprolog.jar platform ios [simulator|install] [iPhone_5|iPhone_5C|iPhone_5S|iPhone_SE|iPhone_6|iPhone_6S|iPhone_6_Plus|iPhone_6S_Plus|iPhone_7|iPhone_7_Plus]");
		System.out.println();
	}

	private static void generateRoboVMShellScript(boolean onRealDevice, String device)
	{
		String arch = "";
		
		if(onRealDevice && (device.equals("iPhone_5") || device.equals("iPhone_5C")))
		{
			arch="thumbv7";
		}
		else if(onRealDevice && (device.equals("iPhone_5S") || device.equals("iPhone_SE") || device.equals("iPhone_6") || device.equals("iPhone_6S") || device.equals("iPhone_6_Plus") || device.equals("iPhone_6S_Plus") || (device.equals("iPhone_7")) || (device.equals("iPhone_7_Plus"))))
		{
			arch="arm64";
		}
		else if(!onRealDevice && (device.equals("iPhone_5") || device.equals("iPhone_5C")))
		{
			arch="x86";
		}
		else if(!onRealDevice && (device.equals("iPhone_5S") || device.equals("iPhone_SE") || device.equals("iPhone_6") || device.equals("iPhone_6S") || device.equals("iPhone_6_Plus") || device.equals("iPhone_6S_Plus") || (device.equals("iPhone_7")) || (device.equals("iPhone_7_Plus"))))
		{
			arch="x86_64";
		}
		else
		{
			System.out.println("\nParams not valid!\n");
		}
		
		PrintWriter writer = null;
		try
		{
			writer = new PrintWriter("./.robovm.sh", "UTF-8");
		} 
		catch (FileNotFoundException | UnsupportedEncodingException e) 
		{
			System.out.println("Problems -> "+e.getMessage());
		}
		
		if(onRealDevice)
		{
			writer.println("./.RoboVM_SDK/bin/robovm -config ./.RoboVM_SDK/robovm-config/robovm.xml -arch "+arch+" -cp ./.RoboVM_SDK/lib/robovm-objc.jar:./.RoboVM_SDK/lib/robovm-cocoatouch.jar:./bin/:"+RoboVM_Launcher.PROJECT_DEPENDENCY+":./lib/gson-2.6.2.jar: -verbose -run");
		}
		else
		{
			System.out.println("\n!! WARNING: Simulator will open on iPhone 5 !!\n");
			writer.println("./.RoboVM_SDK/bin/robovm -config ./.RoboVM_SDK/robovm-config/robovm.xml -arch x86 -cp ./.RoboVM_SDK/lib/robovm-objc.jar:./.RoboVM_SDK/lib/robovm-cocoatouch.jar:./bin/:"+RoboVM_Launcher.PROJECT_DEPENDENCY+":./lib/gson-2.6.2.jar: -verbose -run");
		}
		writer.close();
		
		try 
		{
			alice.tuprolog.ios.launcher.RoboVM_Launcher.launchFromShell();
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
	}
}
