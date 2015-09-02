/*
 * Created on 1-ott-2005
 *
 */
package com.szadowsz.gospel.core.lib

import java.io.File
import java.net.{MalformedURLException, URL, URLClassLoader}
import java.util

import scala.collection.JavaConverters._

import com.szadowsz.gospel.core.data.Term
import com.szadowsz.gospel.core.event.interpreter.LibraryEvent
import com.szadowsz.gospel.core.event.logging.WarningEvent
import com.szadowsz.gospel.core.exception.interpreter.InvalidTheoryException
import com.szadowsz.gospel.core.theory.{Theory, TheoryManager}
import com.szadowsz.gospel.core.Prolog
import com.szadowsz.gospel.util.exception.lib.{LibraryBindException, InvalidLibraryException, LibraryLoadException, LibraryNotFoundException}

/**
 * @author Alex Benini
 *
 */
object LibraryManager {
  private def getClassResource(klass: Class[_]): URL = {
    if (klass == null) return null
    return klass.getClassLoader.getResource(klass.getName.replace('.', '/') + ".class")
  }
}

class LibraryManager(vm: Prolog) {
  private val currentLibraries: util.ArrayList[Library]= new util.ArrayList[Library]
  private val prolog: Prolog = vm
  private lazy val theoryManager: TheoryManager = vm.getTheoryManager
  private lazy val primitiveManager: PrimitiveManager = vm.getPrimitiveManager
  private val externalLibraries: util.Hashtable[String, URL] = new util.Hashtable[String, URL]
  /**
   * @author Alessio Mercurio
   *
   *         This is the directory where optimized dex files should be written.
   *         Is required to the DexClassLoader.
   */
  private var optimizedDirectory: String = null


  /**
   * Loads a library.
   *
   * If a library with the same name is already present, a warning event is
   * notified and the request is ignored.
   *
   * @param className the name of the Java class containing the library to be loaded
   * @return the reference to the Library just loaded
   * @throws InvalidLibraryException
	 * if name is not a valid library
   */
  @throws(classOf[LibraryNotFoundException])
  @throws(classOf[LibraryLoadException])
  def loadLibrary(className: String): Library = {
    var lib: Library = null
    try {
      lib = Class.forName(className).newInstance.asInstanceOf[Library]

      val alib: Library = getLibrary(lib.getName)
      if (alib != null) {
        if (prolog.isWarning) {
          val msg: String = "library " + alib.getName + " already loaded."
          prolog.notifyWarning(new WarningEvent(prolog, msg))
        }
        return alib
      }

    } catch {
      case notFound: ClassNotFoundException => throw new LibraryNotFoundException(className)
      case ex: Exception => throw new LibraryLoadException(className, ex)
    }
    bindLibrary(lib)
    val ev: LibraryEvent = new LibraryEvent(prolog, lib.getName)
    prolog.notifyLoadedLibrary(ev)
    return lib
  }


  /**
   * Loads a library.
   *
   * If a library with the same name is already present, a warning event is
   * notified and the request is ignored.
   *
   * @param theClassName the name of the Java class containing the library to be loaded
   * @param paths the list of the paths where the library may be contained
   * @return the reference to the Library just loaded
   * @throws InvalidLibraryException
	 * if name is not a valid library
   */
  @throws(classOf[LibraryNotFoundException])
  @throws(classOf[LibraryLoadException])
  def loadLibrary(theClassName: String, paths: Array[String]): Library = {
    var className = theClassName
    var lib: Library = null
    var urls: Array[URL] = null
    var loader: java.lang.ClassLoader = null
    var dexPath: String = null
    try {
      if (System.getProperty("java.vm.name") == "Dalvik") {
        /*
        * Only the first path is used. Dex file doesn't contain .class files
        * and therefore getResource() method can't be used to locate the files at runtime.
        */

        dexPath = paths(0)
          /**
           * Description of DexClassLoader
           * A class loader that loads classes from .jar files containing a classes.dex entry.
           * This can be used to execute code not installed as part of an application.
           * param dexPath jar file path where is contained the library.
           * param optimizedDirectory directory where optimized dex files should be written; must not be null
           * param libraryPath the list of directories containing native libraries, delimited by File.pathSeparator; may be null
           * param parent the parent class loader
           */
          /**
           * Here before we were using directly the class DexClassLoader referencing android.jar that
           * contains all the stub classes of Android.
           * This caused the need to have the file android.jar in the classpath even during the execution
           * on the Java SE platform even if it is clearly useless. Therefore we decided to remove this
           * reference and instantiate the DexClassLoader through reflection.
           * This is simplified by the fact that, a part the constructor, we do not use any specific method
           * of DexClassLoader but we use it as any other ClassLoader.
           * A similar approach has been adopted also in the class AndroidDynamicClassLoader.
           */
        loader = Class.forName("dalvik.system.DexClassLoader").getConstructor(classOf[String], classOf[String], classOf[String], classOf[ClassLoader]).newInstance(dexPath, this.getOptimizedDirectory, null, getClass.getClassLoader).asInstanceOf[ClassLoader]
        lib = Class.forName(className, true, loader).newInstance.asInstanceOf[Library]
      }
      else {
        urls = new Array[URL](paths.length)
        for (i <- 0 until paths.length) {

          var file: File = new File(paths(i))
          if (paths(i).contains(".class")) file = new File(paths(i).substring(0, paths(i).lastIndexOf(File.separator) + 1))
          urls(i) = (file.toURI.toURL)
        }

          loader = URLClassLoader.newInstance(urls, getClass.getClassLoader)
          lib = Class.forName(className, true, loader).newInstance.asInstanceOf[Library]
      }
      val name: String = lib.getName
      val alib: Library = getLibrary(name)
      if (alib != null) {
        if (prolog.isWarning) {
          val msg: String = "library " + alib.getName + " already loaded."
          prolog.notifyWarning(new WarningEvent(prolog, msg))
        }
        return alib
      }
    }
    catch {
      case notFound: ClassNotFoundException => throw new LibraryNotFoundException(className)
      case ex: Exception => throw new LibraryLoadException(className, ex)
    }
    if (System.getProperty("java.vm.name") == "Dalvik") {
      try {
        val file: File = new File(paths(0))
        val url: URL = (file.toURI.toURL)
        externalLibraries.put(className, url)
      }
      catch {
        case e: MalformedURLException => {
          e.printStackTrace
        }
      }
    }
    else {
      externalLibraries.put(className, LibraryManager.getClassResource(lib.getClass))
    }
    bindLibrary(lib)
    val ev: LibraryEvent = new LibraryEvent(prolog, lib.getName)
    prolog.notifyLoadedLibrary(ev)
    return lib
  }

  /**
   * Loads a specific instance of a library.
   *
   * If a library of the same class is already present, a warning event is
   * notified. Then, the current instance of that library is discarded, and
   * the new instance gets loaded.
   *
   * @param lib the (Java class) name of the library to be loaded
   * @throws InvalidLibraryException
	 * if name is not a valid library
   */
  @throws(classOf[InvalidLibraryException])
  def loadLibrary(lib: Library) {
    val name: String = lib.getName
    val alib: Library = getLibrary(name)
    if (alib != null) {
      if (prolog.isWarning) {
        val msg: String = "library " + alib.getName + " already loaded."
        prolog.notifyWarning(new WarningEvent(prolog, msg))
      }
      unloadLibrary(name)
    }
    bindLibrary(lib)
    val ev: LibraryEvent = new LibraryEvent(prolog, lib.getName)
    prolog.notifyLoadedLibrary(ev)
  }

  /**
   * Gets the list of current libraries loaded
   *
   * @return the list of the library names
   */
  def getCurrentLibraries: Array[String] = {
    val libs: Array[String] = new Array(currentLibraries.size)
    for (i <- 0 until libs.length) {
      libs(i) = (currentLibraries.get(i).asInstanceOf[Library]).getName

    }
    return libs
  }

  /**
   * Unloads a previously loaded library
   *
   * @param name
	 * of the library to be unloaded
   * @throws InvalidLibraryException
	 * if name is not a valid loaded library
   */
  @throws(classOf[LibraryNotFoundException])
  def unloadLibrary(name: String) {
    var found: Boolean = false
    val it: util.Iterator[Library] = currentLibraries.listIterator
    var continue = true
    while (it.hasNext && continue) {
      val lib: Library = it.next.asInstanceOf[Library]
      if (lib.getName == name) {
        found = true
        it.remove
        lib.dismiss
        primitiveManager.deletePrimitiveInfo(lib)
        continue = false
      }
    }
    if (!found) {
      throw new LibraryNotFoundException(name)
    }
    if (externalLibraries.containsKey(name)) externalLibraries.remove(name)
    theoryManager.removeLibraryTheory(name)
    theoryManager.rebindPrimitives
    val ev: LibraryEvent = new LibraryEvent(prolog, name)
    prolog.notifyUnloadedLibrary(ev)
  }

  /**
   * Binds a library.
   *
   * @param lib is library object
   * @return the reference to the Library just loaded
   * @throws InvalidLibraryException
	 * if name is not a valid library
   */
  @throws(classOf[InvalidLibraryException])
  @throws(classOf[LibraryLoadException])
  private def bindLibrary(lib: Library): Library = {
    try {
      val name: String = lib.getName
      lib.setEngine(prolog)
      currentLibraries.add(lib)
      primitiveManager.createPrimitiveInfo(lib)
      val th: String = lib.getTheory
      if (th != null) {
        theoryManager.consult(new Theory(th), false, name)
        theoryManager.solveTheoryGoal
      }
      // in current theory there could be predicates and functors
      // which become builtins after lib loading
      theoryManager.rebindPrimitives
      return lib
    }
    catch {
      case ex: InvalidTheoryException => throw new LibraryBindException(lib.getName, ex.line, ex.pos,ex.getCause)
      case ex: Exception => { throw new LibraryLoadException(lib.getName, ex)
      }
    }
  }

  /**
   * Gets the reference to a loaded library
   *
   * @param name
	 * the name of the library already loaded
   * @return the reference to the library loaded, null if the library is not
   *         found
   */
  def getLibrary(name: String): Library = {
    currentLibraries.asScala.find(_.getName == name)orNull
  }

  def onSolveBegin(g: Term) {
    import scala.collection.JavaConversions._
    for (alib <- currentLibraries) {
      alib.onSolveBegin(g)
    }
  }

  def onSolveHalt {
    import scala.collection.JavaConversions._
    for (alib <- currentLibraries) {
      alib.onSolveHalt
    }
  }

  def onSolveEnd {
    import scala.collection.JavaConversions._
    for (alib <- currentLibraries) {
      alib.onSolveEnd
    }
  }

  def getExternalLibraryURL(name: String): URL = {
    return if (isExternalLibrary(name)) externalLibraries.get(name) else null
  }

  def isExternalLibrary(name: String): Boolean = {
    return externalLibraries.containsKey(name)
  }

  /**
   * @author Alessio Mercurio
   *
   *         Used to set optimized directory required by the DexClassLoader.
   *         The directory is created Android side.
   */
  def setOptimizedDirectory(optimizedDirectory: String) {
    this.optimizedDirectory = optimizedDirectory
  }

  def getOptimizedDirectory: String = {
    return optimizedDirectory
  }
}