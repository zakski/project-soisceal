package alice.tuprolog;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import alice.tuprolog.event.LibraryEvent;
import alice.tuprolog.event.WarningEvent;
import alice.tuprolog.exceptions.InvalidLibraryException;
import alice.tuprolog.interfaces.ILibraryManager;

//Alberto
public class AndroidPlatformLibraryManager extends AbstractPlatformLibraryManager implements ILibraryManager
{	
	private String optimizedDirectory;

	@Override
	public synchronized Library loadLibrary(String className, String[] paths) throws InvalidLibraryException
	{
		Library lib = null;
		ClassLoader loader = null;
		String dexPath;

		try
		{
			dexPath = paths[0];
			loader = (ClassLoader) Class.forName("dalvik.system.DexClassLoader").getConstructor(String.class, String.class, String.class, ClassLoader.class).newInstance(dexPath, this.getOptimizedDirectory(), null, getClass().getClassLoader());
			lib = (Library) Class.forName(className, true, loader).newInstance();

			String name = lib.getName();
			Library alib = getLibrary(name);
			if (alib != null)
			{
				if (prolog.isWarning())
				{
					String msg = "library " + alib.getName()+ " already loaded.";
					prolog.notifyWarning(new WarningEvent(prolog, msg));
				}
				
				return alib;
			}
		} 
		catch (Exception ex)
		{
			throw new InvalidLibraryException(className, -1, -1);
		}
		
		try
		{
			File file = new File(paths[0]);
			URL url = (file.toURI().toURL());
			externalLibraries.put(className, url);
		} 
		catch (MalformedURLException e)
		{
			e.printStackTrace();
		}
		
		bindLibrary(lib);
		LibraryEvent ev = new LibraryEvent(prolog, lib.getName());
		prolog.notifyLoadedLibrary(ev);
		
		return lib;
	}
	
	public void setOptimizedDirectory(String optimizedDirectory)
	{
		this.optimizedDirectory = optimizedDirectory;
	}
	
	public String getOptimizedDirectory()
	{
		return optimizedDirectory;
	}
}