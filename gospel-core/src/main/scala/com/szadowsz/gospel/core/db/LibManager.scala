package com.szadowsz.gospel.core.db

import java.io.File
import java.net.{URL, URLClassLoader}

import alice.tuprolog._
import alice.tuprolog.event.LibraryEvent
import alice.tuprolog.interfaces.ILibraryManager
import com.szadowsz.gospel.core.{PrologEngine,Theory}
import org.slf4j.LoggerFactory

import scala.util.control.NonFatal

/**
  * Manager of Library Components.
  *
  * Created on 17/02/2017.
  *
  * @version Gospel 2.0.0
  */
class LibManager(private val wam: PrologEngine) extends ILibraryManager {
  private lazy val logger = LoggerFactory.getLogger(getClass)

  private var currentLibs: List[Library] = List[Library]()
  // currently bound libraries.
  private var externalLibs: Map[String, URL] = Map() // external libraries.

  /* * This is the directory where optimized dex files should be written.
	 * Is required to the DexClassLoader.
	 */
  private var optimizedDirectory: String = _


  private lazy val thManager = wam.getTheoryManager //  manager of current theories.

  private lazy val primManager = wam.getPrimitiveManager // primitive prolog term manager.

  private def getClassResource(klass: Class[_]): URL = {
    Option(klass) match {
      case None => null
      case Some(c) => c.getClassLoader.getResource(c.getName.replace('.', '/') + ".class")
    }
  }
  /**
    * Binds a library by adding its clauses to the database and carrying out tis directives.
    *
    * @param lib is library object
    * @return the reference to the Library just loaded
    * @throws InvalidLibraryException if name is not a valid library
    */
  @throws(classOf[InvalidLibraryException])
  private def bindLibrary(lib: Library): Unit = {
    try {
      lib.setEngine(wam)
      currentLibs = lib +: currentLibs
      primManager.createPrimitiveInfo(lib)

      val th: String = lib.getTheory
      if (th != null) {
        thManager.consult(new Theory(th), false, lib.getName)
        thManager.solveTheoryGoal()
      }
      thManager.rebindPrimitives() // in current theory there could be predicates and functors which become built-ins after lib loading.
    } catch {
      case ex: InvalidTheoryException => throw new InvalidLibraryException(lib.getName, ex.line, ex.pos)
      case NonFatal(ex) => throw new InvalidLibraryException(lib.getName, -1, -1)
    }
  }

  override def isExternalLibrary(name: String): Boolean = externalLibs.contains(name)

  /**
    * Gets the list of current libraries loaded
    *
    * @return the list of the library names
    */
  override def getCurrentLibraries: Array[String] = currentLibs.map(_.getName).toArray

  override def getExternalLibraryURL(name: String): URL = externalLibs.get(name).orNull

  /**
    * Gets the reference to a loaded library
    *
    * @param name the name of the library already loaded
    * @return the reference to the library loaded, null if the library is not found.
    */
  override def getLibrary(name: String): Library = currentLibs.find(_.getName == name).orNull

  override def getOptimizedDirectory: String = optimizedDirectory

  /**
    * Loads a library.
    *
    * If a library with the same name is already present, a warning event is notified and the request is ignored.
    *
    * @param className the name of the Java class containing the library to be loaded.
    * @throws InvalidLibraryException if we cannot create a valid library.
    * @return the reference to the Library just loaded.
    */
  @throws(classOf[InvalidLibraryException])
  override def loadLibrary(className: String): Library = {
    try {
      loadLibrary(Class.forName(className).asInstanceOf[Class[Library]])
    } catch {
      case NonFatal(e) => throw new InvalidLibraryException(className, -1, -1)
    }
  }

  @throws(classOf[InvalidLibraryException])
  override def loadLibrary(className: String, paths: Array[String]): Library = {
    try {
      val lib = System.getProperty("java.vm.name") match {
        case "Dalvik" =>
          // Only the first path is used. Dex file doesn't contain.class files and therefore getResource() method can't be used to locate the files at runtime.
          val dexPath = paths.head
          val loaderClass = Class.forName("dalvik.system.DexClassLoader")

          /**
            * Description of DexClassLoader
            * A class loader that loads classes from .jar files containing a classes.dex entry.
            * This can be used to execute code not installed as part of an application.
            *
            * param dexPath jar file path where is contained the library.
            * param optimizedDirectory directory where optimized dex files should be written; must not be null
            * param libraryPath the list of directories containing native libraries, delimited by File.pathSeparator; may be null
            * param parent the parent class loader
            */
          val loaderConstructor = loaderClass.getConstructor(classOf[String], classOf[String], classOf[String], classOf[ClassLoader])
          val loader = loaderConstructor.newInstance(dexPath, this.getOptimizedDirectory, null, getClass.getClassLoader).asInstanceOf[ClassLoader]
          Class.forName(className, true, loader).newInstance.asInstanceOf[Library]
        case _ =>
          val urls = paths.map(p => (if (!p.contains(".class")) new File(p) else new File(p.substring(0, p.lastIndexOf(File.separator) + 1))).toURI.toURL)
          val loader = URLClassLoader.newInstance(urls, getClass.getClassLoader)
          Class.forName(className, true, loader).newInstance.asInstanceOf[Library]
      }
      Option(getLibrary(lib.getName)) match {
        case Some(oldLib) =>
          logger.warn(s"Library ${oldLib.getName} already loaded.")
          oldLib
        case None =>
          System.getProperty("java.vm.name") match {
            case "Dalvik" =>
              val file: File = new File(paths(0))
              val url: URL = file.toURI.toURL
              externalLibs = externalLibs + (className -> url)
            case _ => externalLibs = externalLibs +(className -> getClassResource(lib.getClass))
          }
          bindLibrary(lib)
          logger.info(s"Loaded Library ${lib.getName}")
          val ev: LibraryEvent = new LibraryEvent(wam, lib.getName)
          wam.notifyLoadedLibrary(ev)
          lib
      }
     } catch {
      case NonFatal(e) => throw new InvalidLibraryException(className, -1, -1)
    }
  }

  /**
    * Loads a library.
    *
    * If a library with the same name is already present, a warning event is notified and the request is ignored.
    *
    * @param libClass the library class to be loaded.
    * @throws InvalidLibraryException if we cannot create a valid library.
    * @return the reference to the Library just loaded.
    */
  @throws(classOf[InvalidLibraryException])
  def loadLibrary(libClass: Class[_ <: Library]): Library = {
    val library = Option(getLibrary(libClass.getName)) match {
      case Some(lib) =>
        logger.warn(s"Library ${lib.getName} already loaded.")
        lib
      case None =>
        try {
          libClass.newInstance()
        } catch {
          case NonFatal(ex) => throw new InvalidLibraryException(libClass.getName, -1, -1)
        }
    }
    bindLibrary(library)
    logger.info(s"Loaded Library ${library.getName}")
    val ev: LibraryEvent = new LibraryEvent(wam, library.getName)
    wam.notifyLoadedLibrary(ev)
    library
  }

  /**
    * Loads a specific instance of a library.
    *
    * If a library of the same class is already present, a warning event is notified. Then, the current instance of that library is discarded, and the new
    * instance gets loaded.
    *
    * @param lib the (Java class) name of the library to be loaded
    * @throws InvalidLibraryException if we cannot create a valid library.
    * @return the library object now loaded in the interpreter
    */
  @throws(classOf[InvalidLibraryException])
  override def loadLibrary(lib: Library): Unit = {
    Option(getLibrary(lib.getName)) match {
      case Some(oldLib) =>
        logger.warn(s"Library ${oldLib.getName} already loaded.")
        unloadLibrary(oldLib.getName)
        logger.info("Old Instance of {} removed.", lib.getName)

      case None => // nop
    }
    bindLibrary(lib)
    logger.info("Loaded Library {}", lib)
    val ev: LibraryEvent = new LibraryEvent(wam, lib.getName)
    wam.notifyLoadedLibrary(ev)
  }

  /**
    * Unloads a previously loaded library.
    *
    * @param className the name of the library to be unloaded.
    */
  override def unloadLibrary(className: String): Unit = {
    currentLibs.find(_.getName == className) match {
      case Some(lib) =>
        lib.dismiss()
        primManager.deletePrimitiveInfo(lib)
        currentLibs = currentLibs.filterNot(_ == lib)
        thManager.removeLibraryTheory(className)
        thManager.rebindPrimitives()
      case None => throw new InvalidLibraryException() // TODO don't throw error here logger.warn(s"Library $className is not loaded.")
    }
  }

  /**
    * Method is called when Prolog demonstration begins. Allows JVM libraries to do any initialisation that they may want.
    *
    * @param goal the goal we are solving for.
    */
  override def onSolveBegin(goal: Term): Unit = currentLibs.foreach(_.onSolveBegin(goal))

  /**
    * Method is called when Prolog demonstration is halted. Allows JVM libraries to do any cleanup that they may want.
    */
  override def onSolveHalt(): Unit = currentLibs.foreach(_.onSolveHalt())

  /**
    * Method is called when Prolog demonstration ends. Allows JVM libraries to do any cleanup that they may want.
    */
  override def onSolveEnd(): Unit = currentLibs.foreach(_.onSolveEnd())

  override def setOptimizedDirectory(optimizedDir: String): Unit = {
    optimizedDirectory = optimizedDir
  }
}

