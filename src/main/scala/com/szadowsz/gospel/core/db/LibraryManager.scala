/*
 * Created on 1-ott-2005
 *
 */
package com.szadowsz.gospel.core.db

import com.szadowsz.gospel.core.Prolog
import com.szadowsz.gospel.core.data.Term
import com.szadowsz.gospel.core.db.lib.Library
import com.szadowsz.gospel.core.db.primitive.PrimitiveManager
import com.szadowsz.gospel.core.db.theory.{Theory, TheoryManager}
import com.szadowsz.gospel.util.LoggerCategory
import com.szadowsz.gospel.util.exception.lib.{InvalidLibraryException, LibraryLoadException, LibraryNotFoundException}
import com.szadowsz.gospel.util.exception.theory.InvalidTheoryException
import org.slf4j.LoggerFactory

import scala.util.control.NonFatal

/**
  *
  * @param wam The implementation of Warren's Abstract Machine attached to the database.
  */
final class LibraryManager(private val wam: Prolog) {
  private lazy val logger = LoggerFactory.getLogger(LoggerCategory.DB)

  private var currentLibraries: List[Library] = List[Library]()

  /**
    * component to manage working theory
    */
  private lazy val theoryManager: TheoryManager = wam.getTheoryManager

  /**
    * component to record primitives, initialised with the built-in predicates
    */
  private lazy val primitiveManager: PrimitiveManager = wam.getPrimitiveManager

  /**
    * Binds a library by adding its clauses to the database and carrying out tis directives.
    *
    * @param lib is library object
    * @return the reference to the Library just loaded
    * @throws InvalidLibraryException
    * if name is not a valid library
    */
  @throws(classOf[InvalidLibraryException])
  private def bindLibrary(lib: Library): Unit = {
    try {
      lib.setEngine(wam)
      currentLibraries = lib +: currentLibraries
      primitiveManager.createPrimitiveInfo(lib)

      val th: String = lib.getTheory
      if (th != null) {
        theoryManager.consult(new Theory(th), false, lib.getName)
        theoryManager.solveTheoryGoal()
      }
      theoryManager.rebind() // in current theory there could be predicates and functors which become built-ins after lib loading.
    } catch {
      case ex: InvalidTheoryException => throw new InvalidLibraryException(lib.getName, ex.getCause)
      case NonFatal(ex) => throw new LibraryLoadException(lib.getName, ex)
    }
  }

  /**
    * Method is called when Prolog demonstration begins. Allows JVM libraries to do any initialisation that they may want.
    *
    * @param goal the goal we are solving for.
    */
  def onSolveBegin(goal: Term): Unit = currentLibraries.foreach(_.onSolveBegin(goal))

  /**
    * Method is called when Prolog demonstration is halted. Allows JVM libraries to do any cleanup that they may want.
    */
  def onSolveHalt(): Unit = currentLibraries.foreach(_.onSolveHalt())

  /**
    * Method is called when Prolog demonstration ends. Allows JVM libraries to do any cleanup that they may want.
    */
  def onSolveEnd(): Unit = currentLibraries.foreach(_.onSolveEnd())

  /**
    * Gets the reference to a loaded library
    *
    * @param name the name of the library already loaded
    * @return the reference to the library loaded, null if the library is not found.
    */
  def getLibrary(name: String): Option[Library] = currentLibraries.find(_.getName == name)

  /**
    * Gets the list of current libraries loaded
    *
    * @return the list of the library names
    */
  def getCurrentLibraries: Array[String] = currentLibraries.map(_.getName).toArray

  /**
    * Unloads a previously loaded library.
    *
    * @param className the name of the library to be unloaded.
    */
  def unloadLibrary(className: String): Unit = {
    currentLibraries.find(_.getName == className) match {
      case Some(lib) =>
        lib.dismiss()
        primitiveManager.deletePrimitiveInfo(lib)
        currentLibraries = currentLibraries.filterNot(_ == lib)
        theoryManager.removeLibraryTheory(className)
        theoryManager.rebind()
      case None => throw new LibraryNotFoundException(className)// TODO don't throw error here logger.warn(s"Library $className is not loaded.")
    }
  }

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
  def loadLibrary(className: String): Library = {
    val lib = getLibrary(className) match {
      case Some(l) =>
        logger.warn(s"Library ${l.getName} already loaded.")
        l
      case None =>
        try {
          Class.forName(className).newInstance.asInstanceOf[Library]
        } catch {
          case notFound: ClassNotFoundException => throw new LibraryNotFoundException(className)
          case NonFatal(ex) => throw new LibraryLoadException(className, ex)
        }
    }
    bindLibrary(lib)
    logger.info(s"Loaded Library ${lib.getName}")
    lib
  }

  /**
    * Loads a specific instance of a library.
    *
    * If a library of the same class is already present, a warning event is
    * notified. Then, the current instance of that library is discarded, and
    * the new instance gets loaded.
    *
    * @param lib the (Java class) name of the library to be loaded
    * @throws InvalidLibraryException if we cannot create a valid library.
    * @return the library object now loaded in the interpreter
    */
  @throws(classOf[InvalidLibraryException])
  def loadLibrary(lib: Library): Library = {
    getLibrary(lib.getName) match {
      case Some(oldLib) =>
        logger.warn(s"Library ${oldLib.getName} already loaded.")
        unloadLibrary(oldLib.getName)
        logger.info("Old Instance of {} removed.", lib.getName)

      case None => // nop
    }
    bindLibrary(lib)
    logger.info("Loaded Library {}", lib)
    lib
  }

}