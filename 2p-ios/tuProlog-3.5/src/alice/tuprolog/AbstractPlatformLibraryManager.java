package alice.tuprolog;

import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import alice.tuprolog.event.LibraryEvent;
import alice.tuprolog.event.WarningEvent;
import alice.tuprolog.exceptions.InvalidLibraryException;
import alice.tuprolog.exceptions.InvalidTheoryException;
import alice.tuprolog.interfaces.ILibraryManager;

//Alberto
public abstract class AbstractPlatformLibraryManager implements ILibraryManager
{
	private ArrayList<Library> currentLibraries = new ArrayList<Library>();

	protected transient Prolog prolog;
	private transient TheoryManager theoryManager;
	private transient PrimitiveManager primitiveManager;
	protected Hashtable<String, URL> externalLibraries = new Hashtable<String, URL>();

	@Override
	public void initialize(Prolog vm)
	{
		prolog = vm;
		theoryManager = vm.getTheoryManager();
		primitiveManager = vm.getPrimitiveManager();
	}

	@Override
	public synchronized Library loadLibrary(String className) throws InvalidLibraryException
	{
		Library lib = null;
		try
		{
			lib = (Library) Class.forName(className).newInstance();
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
		
		bindLibrary(lib);
		LibraryEvent ev = new LibraryEvent(prolog, lib.getName());
		prolog.notifyLoadedLibrary(ev);
		
		return lib;
	}
	
	protected Library bindLibrary(Library lib) throws InvalidLibraryException
	{
		try
		{
			String name = lib.getName();
			lib.setEngine(prolog);
			currentLibraries.add(lib);
			
			primitiveManager.createPrimitiveInfo(lib);
			
			String th = lib.getTheory();
			if (th != null)
			{
				theoryManager.consult(new Theory(th), false, name);
				theoryManager.solveTheoryGoal();
			}
			
			theoryManager.rebindPrimitives();
			
			return lib;
		} 
		catch (InvalidTheoryException ex)
		{
			throw new InvalidLibraryException(lib.getName(), ex.line, ex.pos);
		} 
		catch (Exception ex)
		{
			throw new InvalidLibraryException(lib.getName(), -1, -1);
		}
	}

	@Override
	public abstract Library loadLibrary(String className, String[] paths) throws InvalidLibraryException;

	@Override
	public synchronized void loadLibrary(Library lib) throws InvalidLibraryException
	{
		String name = lib.getName();
		Library alib = getLibrary(name);
		if (alib != null)
		{
			if (prolog.isWarning())
			{
				String msg = "library " + alib.getName() + " already loaded.";
				prolog.notifyWarning(new WarningEvent(prolog, msg));
			}
			
			unloadLibrary(name);
		}
		bindLibrary(lib);
		
		LibraryEvent ev = new LibraryEvent(prolog, lib.getName());
		prolog.notifyLoadedLibrary(ev);
	}

	@Override
	public synchronized String[] getCurrentLibraries()
	{
		String[] libs = new String[currentLibraries.size()];
		for (int i = 0; i < libs.length; i++)
		{
			libs[i] = ((Library) currentLibraries.get(i)).getName();
		}
		
		return libs;
	}

	@Override
	public synchronized void unloadLibrary(String name) throws InvalidLibraryException
	{
		boolean found = false;
		Iterator<Library> it = currentLibraries.listIterator();
		while (it.hasNext())
		{
			Library lib = (Library) it.next();
			if (lib.getName().equals(name))
			{
				found = true;
				it.remove();
				lib.dismiss();
				primitiveManager.deletePrimitiveInfo(lib);
				
				break;
			}
		}
		
		if (!found)
		{
			throw new InvalidLibraryException();
		}
		
		if (externalLibraries.containsKey(name)) 
			externalLibraries.remove(name);
		
		theoryManager.removeLibraryTheory(name);
		theoryManager.rebindPrimitives();
		LibraryEvent ev = new LibraryEvent(prolog, name);
		prolog.notifyUnloadedLibrary(ev);
	}

	@Override
	public synchronized Library getLibrary(String name)
	{
		for (Library alib : currentLibraries)
		{
			if (alib.getName().equals(name))
			{
				return alib;
			}
		}
		
		return null;
	}

	@Override
	public synchronized void onSolveBegin(Term g)
	{
		for (Library alib : currentLibraries)
		{
			alib.onSolveBegin(g);
		}
	}

	@Override
	public synchronized void onSolveHalt()
	{
		for (Library alib : currentLibraries)
		{
			alib.onSolveHalt();
		}
	}

	@Override
	public synchronized void onSolveEnd()
	{
		for (Library alib : currentLibraries)
		{
			alib.onSolveEnd();
		}
	}

	@Override
	public synchronized URL getExternalLibraryURL(String name)
	{
		return isExternalLibrary(name) ? externalLibraries.get(name) : null;
	}

	@Override
	public synchronized boolean isExternalLibrary(String name)
	{
		return externalLibraries.containsKey(name);
	}
	
	protected static URL getClassResource(Class<?> klass)
	{
		if (klass == null)
			return null;
		
		return klass.getClassLoader().getResource(klass.getName().replace('.', '/') + ".class");
	}
}