package alice.tuprolog.factories;

//Alberto
public final class DefaultLibrariesSet 
{
	public static String[] getDefaultLibrariesSetForCurrentPlatform() 
	{
		if(System.getProperty("java.vm.name").equals("IKVM.NET"))
		{
			return new String[] {"alice.tuprolog.lib.BasicLibrary", "alice.tuprolog.lib.ISOLibrary", 
					"alice.tuprolog.lib.IOLibrary", "OOLibrary.OOLibrary, OOLibrary"};
		}
		else
		{
			return new String[] {"alice.tuprolog.lib.BasicLibrary", "alice.tuprolog.lib.ISOLibrary", 
					"alice.tuprolog.lib.IOLibrary", "alice.tuprolog.lib.OOLibrary"};
		}
	}
}
