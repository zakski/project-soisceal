package alice.tuprolog.interfaces;

import alice.tuprolog.AndroidLibraryManager;
import alice.tuprolog.DotNetLibraryManager;
import alice.tuprolog.JavaLibraryManager;

public final class LibraryManagerFactory 
{
	public static ILibraryManager getLibraryManagerForCurrentPlatform()
	{
		if (System.getProperty("java.vm.name").equals("Dalvik"))
		{
			//Android
			return new AndroidLibraryManager();
		}
		else if (System.getProperty("java.vm.name").equals("IKVM.NET"))
		{
			//.NET
			return new DotNetLibraryManager();
		}
		else
		{
			//Java
			return new JavaLibraryManager();
		}
	}
}
