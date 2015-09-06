/*
 * Created on 1-ott-2005
 *
 */
package com.szadowsz.gospel.core.db

import com.szadowsz.gospel.core.Prolog
import com.szadowsz.gospel.core.data.Term
import com.szadowsz.gospel.core.db.primitive.PrimitiveManager
import com.szadowsz.gospel.core.db.theory.{Theory, TheoryManager}
import com.szadowsz.gospel.core.lib.Library
import com.szadowsz.gospel.util.LoggerCategory
import com.szadowsz.gospel.util.exception.lib.{InvalidLibraryException, LibraryLoadException, LibraryNotFoundException}
import com.szadowsz.gospel.util.exception.theory.InvalidTheoryException
import org.slf4j.LoggerFactory

/**
 * @author Alex Benini
 *
 */
class LibraryManager(wam: Prolog) {
  private lazy val _logger = LoggerFactory.getLogger(LoggerCategory.DB)

  /**
   * The implementation of Warren's Abstract Machine attached to the database.
   */
  private val _wam: Prolog = wam

  private var _currentLibraries: List[Library] = List[Library]()

  /**
   * component to manage working theory
   */
  private lazy val _theoryManager: TheoryManager = _wam.getTheoryManager

  /**
   * component to record primitives, initialised with the built-in predicates
   */
  private lazy val _primitiveManager: PrimitiveManager = _wam.getPrimitiveManager

  /**
   * Gets the reference to a loaded library
   *
   * @param name
	 * the name of the library already loaded
   * @return the reference to the library loaded, null if the library is not
   *         found
   */
  def getLibrary(name: String): Option[Library] = _currentLibraries.find(_.getName == name)

  /**
   * Gets the list of current libraries loaded
   *
   * @return the list of the library names
   */
  def getCurrentLibraries: Array[String] = _currentLibraries.map(_.getName).toArray

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
    var lib: Library = getLibrary(className).orNull
    if (lib != null) {
      _logger.info("Library " + lib.getName + " already loaded.")
    } else {
      try {
        lib = Class.forName(className).newInstance.asInstanceOf[Library]
      } catch {
        case notFound: ClassNotFoundException => throw new LibraryNotFoundException(className)
        case ex: Exception => throw new LibraryLoadException(className, ex)
      }
      bindLibrary(lib)
      _logger.info("Loaded Library " + lib)
    }
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
   * @throws com.szadowsz.gospel.util.exception.lib.InvalidLibraryException if name is not a valid library
   * @return the library object now loaded in the interpreter
   */
  @throws(classOf[InvalidLibraryException])
  def loadLibrary(lib: Library): Library = {
    val name: String = lib.getName
    val alib: Library = getLibrary(name).orNull
    if (alib != null) {
      _logger.info("Library " + lib.getName + " already loaded.")
      unloadLibrary(name)
      _logger.info("Old Instance of {} removed.",lib.getName)
    }
    bindLibrary(lib)
    _logger.info("Loaded Library {}",lib)
    lib
  }

  /**
   * Unloads a previously loaded library
   *
   * @param className the library to be unloaded
   * @throws InvalidLibraryException
	 * if name is not a valid loaded library
   */
  @throws(classOf[InvalidLibraryException])
  def unloadLibrary(className: String) {
    val libOpt = _currentLibraries.find(_.getName == className)
    if (libOpt.isDefined) {
      val lib = libOpt.get
      lib.dismiss()
      _primitiveManager.deletePrimitiveInfo(lib)
      _currentLibraries = _currentLibraries.filterNot(_ == lib)
    } else {
      throw new LibraryNotFoundException(className)
    }
    _theoryManager.removeLibraryTheory(className)
    _theoryManager.rebind
  }

  /**
   * Binds a library by adding its clauses to the database and carrying out tis directives.
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
      lib.setEngine(_wam)
      _currentLibraries = lib +: _currentLibraries

      _primitiveManager.createPrimitiveInfo(lib)

      val th: String = lib.getTheory
      if (th != null) {
        _theoryManager.consult(new Theory(th), false, name)
        _theoryManager.solveTheoryGoal
      }
      // in current theory there could be predicates and functors
      // which become builtins after lib loading
      _theoryManager.rebind
      lib
    }
    catch {
      case ex: InvalidTheoryException => throw new InvalidLibraryException(lib.getName, ex.getCause)
      case ex: Exception => throw new LibraryLoadException(lib.getName, ex)
    }
  }

  /**
   * Method is called when Prolog demonstration begins. Allows JVM libraries to do
   * any initialisation that they may want.
   *
   * @param goal the goal we are solving for.
   */
  def onSolveBegin(goal: Term) {
    _currentLibraries.foreach(_.onSolveBegin(goal))
  }

  /**
   * Method is called when Prolog demonstration is halted. Allows JVM libraries to do
   * any cleanup that they may want.
   */
  def onSolveHalt() {
    _currentLibraries.foreach(_.onSolveHalt())
  }

  /**
   * Method is called when Prolog demonstration ends. Allows JVM libraries to do
   * any cleanup that they may want.
   */
  def onSolveEnd() {
    _currentLibraries.foreach(_.onSolveEnd())
  }
}