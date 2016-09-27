package alice.tuprolog;

import java.io.File;
import java.net.URL;

import alice.tuprolog.event.LibraryEvent;
import alice.tuprolog.event.WarningEvent;
import alice.tuprolog.exceptions.InvalidLibraryException;
import alice.tuprolog.interfaces.ILibraryManager;
import alice.util.AssemblyCustomClassLoader;
import cli.System.Reflection.Assembly;

public class DotNetLibraryManager extends AbstractLibraryManager implements ILibraryManager 
{
	@Override
	public synchronized Library loadLibrary(String className, String[] paths) throws InvalidLibraryException
	{
		Library lib = null;
		URL[] urls = null;
		ClassLoader loader = null;

		try
		{
			urls = new URL[paths.length];
			for (int i = 0; i < paths.length; i++)
			{
				File file = new File(paths[i]);
				
				if (paths[i].contains(".class"))
					file = new File(paths[i].substring(0, paths[i].lastIndexOf(File.separator) + 1));
				
				urls[i] = (file.toURI().toURL());
			}
			
			Assembly asm = null;
			boolean classFound = false;
			className = "cli."+ className.substring(0, className.indexOf(",")).trim();
			for (int i = 0; i < paths.length; i++)
			{
				try
				{
					asm = Assembly.LoadFrom(paths[i]);
					loader = new AssemblyCustomClassLoader(asm, urls);
					lib = (Library) Class.forName(className, true, loader).newInstance();
					if (lib != null)
					{
						classFound = true;
						break;
					}
				} 
				catch (Exception e)
				{
					e.printStackTrace();
					continue;
				}
			}
			
			if (!classFound)
				throw new InvalidLibraryException(className, -1, -1);

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
		
		externalLibraries.put(className, getClassResource(lib.getClass()));
		
		bindLibrary(lib);
		LibraryEvent ev = new LibraryEvent(prolog, lib.getName());
		prolog.notifyLoadedLibrary(ev);
		
		return lib;
	}
}