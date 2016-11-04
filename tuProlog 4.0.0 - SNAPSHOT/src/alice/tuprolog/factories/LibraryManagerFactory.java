package alice.tuprolog.factories;

import alice.tuprolog.AndroidLibraryManager;
import alice.tuprolog.DotNetLibraryManager;
import alice.tuprolog.JavaLibraryManager;
import alice.tuprolog.interfaces.ILibraryManager;

//Alberto
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
