package alice.tuprolog.factories;

import alice.tuprolog.AndroidPlatformLibraryManager;
import alice.tuprolog.DotNetPlatformLibraryManager;
import alice.tuprolog.JavaPlatformLibraryManager;
import alice.tuprolog.interfaces.ILibraryManager;

//Alberto
public final class LibraryManagerFactory 
{
	public static ILibraryManager getLibraryManagerForCurrentPlatform()
	{
		if (System.getProperty("java.vm.name").equals("Dalvik"))
		{
			//Android
			return new AndroidPlatformLibraryManager();
		}
		else if (System.getProperty("java.vm.name").equals("IKVM.NET"))
		{
			//.NET
			return new DotNetPlatformLibraryManager();
		}
		else
		{
			//Java
			return new JavaPlatformLibraryManager();
		}
	}
}
