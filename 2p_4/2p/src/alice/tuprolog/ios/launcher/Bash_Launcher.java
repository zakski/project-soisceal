package alice.tuprolog.ios.launcher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import alice.tuprolog.Prolog;

public class Bash_Launcher 
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
						generateBugVMShellScript(false, args[3]);
					}
					else if(args[2].equals("install"))
					{
						generateBugVMShellScript(true, args[3]);
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

	private static void generateBugVMShellScript(boolean onRealDevice, String device)
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
		
		check_RoboVM_SDK();
		
		PrintWriter writer = null;
		try
		{
			writer = new PrintWriter("./robovm.sh", "UTF-8");
		} 
		catch (FileNotFoundException | UnsupportedEncodingException e) 
		{
			System.out.println("Problems -> "+e.getMessage());
		}
		
		if(onRealDevice)
		{
			writer.println("./RoboVM_SDK/bin/robovm -config ./RoboVM_SDK/robovm-config/robovm.xml -arch "+arch+" -cp ./RoboVM_SDK/lib/robovm-objc.jar:./RoboVM_SDK/lib/robovm-cocoatouch.jar:./bin/ -verbose -run");
		}
		else
		{
			writer.println("./RoboVM_SDK/bin/robovm -config ./RoboVM_SDK/robovm-config/robovm.xml -arch x86 -cp ./RoboVM_SDK/lib/robovm-objc.jar:./RoboVM_SDK/lib/robovm-cocoatouch.jar:./bin/ -verbose -run");
			System.out.println("\n!! WARNING: tuProlog "+Prolog.getVersion()+" only supports simulatior for architecture x86 !!\n");
		}
		writer.close();
		
		alice.tuprolog.ios.launcher.RoboVM_Launcher.main(null);
	}

	private static void check_RoboVM_SDK() 
	{
		File RoboVM_SDK = new File("./RoboVM_SDK");
		
		if(!RoboVM_SDK.exists() || !RoboVM_SDK.isDirectory() || !RoboVM_SDK.canExecute())
		{
			System.out.println("RoboVM_SDK corrupted or missing!");
			System.exit(-1);
		}
		
		File bin_robovm = new File("./RoboVM_SDK/bin/robovm");
		if(!bin_robovm.exists() || !bin_robovm.canExecute())
		{
			System.out.println("File script ./RoboVM_SDK/bin/robovm corrupted or missing");
			System.exit(-2);
		}
		
		File bin_ios_sim = new File("./RoboVM_SDK/bin/simlauncher");
		if(!bin_ios_sim.exists() || !bin_ios_sim.canExecute())
		{
			System.out.println("File script ./RoboVM_SDK/bin/simlauncher corrupted or missing");
			System.exit(-3);
		}
		
		File cacerts = new File("./RoboVM_SDK/lib/robovm-cacerts-full.jar");
		if(!cacerts.exists() || !cacerts.canRead())
		{
			System.out.println("Jar file ./RoboVM_SDK/lib/robovm-cacerts-full.jar corrupted or missing");
			System.exit(-4);
		}
		
		File cocoatouch = new File("./RoboVM_SDK/lib/robovm-cocoatouch.jar");
		if(!cocoatouch.exists() || !cocoatouch.canRead())
		{
			System.out.println("Jar file ./RoboVM_SDK/lib/robovm-cocoatouch.jar corrupted or missing");
			System.exit(-5);
		}
		
		File distcompiler = new File("./RoboVM_SDK/lib/robovm-dist-compiler.jar");
		if(!distcompiler.exists() || !distcompiler.canRead())
		{
			System.out.println("Jar file ./RoboVM_SDK/lib/robovm-dist-compiler.jar corrupted or missing");
			System.exit(-6);
		}
		
		File objc = new File("./RoboVM_SDK/lib/robovm-objc.jar");
		if(!objc.exists() || !objc.canRead())
		{
			System.out.println("Jar file ./RoboVM_SDK/lib/robovm-objc.jar corrupted or missing");
			System.exit(-7);
		}
		
		File rt = new File("./RoboVM_SDK/lib/robovm-rt.jar");
		if(!rt.exists() || !rt.canRead())
		{
			System.out.println("Jar file ./RoboVM_SDK/lib/robovm-rt.jar corrupted or missing");
			System.exit(-8);
		}
	}
}
