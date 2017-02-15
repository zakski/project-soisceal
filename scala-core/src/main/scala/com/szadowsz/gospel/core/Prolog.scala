/*
 * tuProlog - Copyright (C) 2001-2002  aliCE team at deis.unibo.it
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.szadowsz.gospel.core

import com.szadowsz.gospel.core.db.theory.{Theory, TheoryManager}
import com.szadowsz.gospel.core.operation.{Operator, OperatorManager}
import com.szadowsz.gospel.core.parser.Parser
import com.szadowsz.gospel.util.exception.data.InvalidTermException
import com.szadowsz.gospel.util.exception.solution.{MalformedGoalException, NoMoreSolutionsException}
import com.szadowsz.gospel.util.exception.theory.InvalidTheoryException
import org.slf4j.LoggerFactory
import java.{util => ju}

import com.szadowsz.gospel.core.data.{Struct, Term}
import com.szadowsz.gospel.core.db.LibraryManager
import com.szadowsz.gospel.core.db.lib.Library
import com.szadowsz.gospel.core.db.primitive.PrimitiveManager
import com.szadowsz.gospel.core.engine.context.ExecutionContext
import com.szadowsz.gospel.core.engine.{Engine, EngineManager, Solution}
import com.szadowsz.gospel.core.event._
import com.szadowsz.gospel.core.event.interpreter.{LibraryEvent, QueryEvent, TheoryEvent}
import com.szadowsz.gospel.core.event.io.OutputEvent
import com.szadowsz.gospel.core.event.logging.SpyEvent
import com.szadowsz.gospel.core.flag.FlagManager
import com.szadowsz.gospel.core.lib.{BasicLibrary, IOLibrary, ISOLibrary, OOLibrary}
import com.szadowsz.gospel.util.{LoggerCategory, VersionInfo}
import com.szadowsz.gospel.util.exception.lib.InvalidLibraryException

import scala.annotation.varargs
import scala.util.Try

object Prolog {
  /**
    * Gets the current version of the tuProlog system
    */
  def getVersion: String = {
    VersionInfo.getEngineVersion
  }
}


/**
  *
  * The Prolog class represents a tuProlog engine.
  *
  */
@SerialVersionUID(1L)
class Prolog(spy: Boolean, warning: Boolean) extends Serializable {
  private lazy val logger = LoggerFactory.getLogger(LoggerCategory.PROLOG)

  private val theoryManager = TheoryManager(this) // manager of current theories

  private val libManager = LibraryManager(this) // manager of loaded libraries

  private val engManager = EngineManager(this) // manager of the prolog engine execution

  private val flagManager = new FlagManager() // engine flag manager

  /* component managing operators */
  private val _opManager = new OperatorManager

  /*  component managing primitive  */
  private val _primitiveManager: PrimitiveManager = new PrimitiveManager(this)

  /*  if spying activated  */
  private var _spy: Boolean = spy

  /* listeners registrated for virtual machine output events */
  private val _outputListeners = new ju.ArrayList[OutputListener]

  /* listeners registrated for virtual machine internal events */
  private val _spyListeners = new ju.ArrayList[SpyListener]

  /* listeners to theory events */
  private val _theoryListeners = new ju.ArrayList[TheoryListener]

  /* listeners to query events */
  private val _queryListeners = new ju.ArrayList[QueryListener]

  /* path history for including documents */
  private var absolutePathList: ju.ArrayList[String] = new ju.ArrayList[String]
  private var lastPath: String = null



  /**
    * Builds a tuProlog engine with loaded the specified libraries.
    *
    * @param libs the (class) names of the libraries to be loaded
    */
  @throws(classOf[InvalidLibraryException])
  def this(libs: Array[String]) {
    this(false, true)
    libs.foreach(s => loadLibrary(s))
  }

  /**
    * Builds a tuProlog engine with loaded the specified libraries.
    *
    * @param libs the classes of the libraries to be loaded
    */
  def this(libs: Array[Class[_ <: Library]]) {
    this(false, true)
    libs.foreach(c => loadLibrary(c))
  }

  /**
    * Builds a Prolog engine with default libraries loaded.
    *
    * The default libraries are BasicLibrary, ISOLibrary, IOLibrary, and OOLibrary.
    */
  def this() {
    this(Array[Class[_ <: Library]](
      classOf[BasicLibrary],
      classOf[ISOLibrary],
      classOf[IOLibrary],
      classOf[OOLibrary]
    ))
  }

   /**
    * Internal only method to retrieve the component managing the engine.
    *
    * @return the engine manager instance attached to this.
    */
  private[gospel] def getEngineManager: EngineManager = engManager

  /**
    * Internal only method to retrieve the component managing flags.
    *
    * @return the flag manager instance attached to this prolog engine.
    */
  private[gospel] def getFlagManager: FlagManager = flagManager

  /**
    * Internal only method to retrieve the component managing libraries.
    *
    * @return the library manager instance attached to this prolog engine.
    */
  private[gospel] def getLibraryManager: LibraryManager = libManager

  /**
    * Internal only method to retrieve the component managing the theory.
    *
    * @return the theory manager instance attached to this prolog engine.
    */
  private[gospel] def getTheoryManager: TheoryManager = theoryManager

  /**
    * Checks if the demonstration process was stopped by an halt command.
    *
    * @return true if the demonstration was stopped
    */
  def isHalted: Boolean = engManager.isHalted

  /**
    * Tests Unification between two terms using the current demonstration context.
    *
    * @param t0 first term to be unified
    * @param t1 second term to be unified
    * @return true if the unification was successful, false otherwise
    */
  def isMatch(t0: Term, t1: Term): Boolean = t0.matches(t1)

  /**
    * Unifies two terms using current demonstration context.
    *
    * @param t0 first term to be unified
    * @param t1 second term to be unified
    * @return true if the unification was successful, false otherwise
    */
  def unify(t0: Term, t1: Term): Boolean = t0.unify(this, t1)

  /**
    * Gets the component managing primitives
    */
  def getPrimitiveManager: PrimitiveManager = {
    _primitiveManager
  }

  /** Gets the component managing operators */
  def getOperatorManager: OperatorManager = {
    _opManager
  }

  /**
    * Gets the last Element of the path list
    */
  def getCurrentDirectory: String = {
    var directory: String = ""
    if (absolutePathList.isEmpty) {
      if (this.lastPath != null) {
        directory = this.lastPath
      }
      else {
        directory = System.getProperty("user.dir")
      }
    }
    else {
      directory = absolutePathList.get(absolutePathList.size - 1)
    }
    directory
  }

  /**
    * Sets the last Element of the path list
    */
  def setCurrentDirectory(s: String) {
    this.lastPath = s
  }

  /**
    * Sets a new theory
    *
    * @param th is the new theory
    * @throws InvalidTheoryException if the new theory is not valid
    * @see alice.gospel.core.theory.Theory
    */
  @throws(classOf[InvalidTheoryException])
  def setTheory(th: Theory):Unit = {
    theoryManager.clear()
    addTheory(th)
  }

  /**
    * Adds (appends) a theory
    *
    * @param th is the theory to be added
    * @throws InvalidTheoryException if the new theory is not valid
    * @see alice.gospel.core.theory.Theory
    */
  @throws(classOf[InvalidTheoryException])
  def addTheory(th: Theory): Unit = {
    val oldTh: Theory = getTheory
    theoryManager.consult(th, true, null)
    theoryManager.solveTheoryGoal()
    val newTh: Theory = getTheory
    val ev: TheoryEvent = new TheoryEvent(this, oldTh, newTh)
    this.notifyChangedTheory(ev)
  }

  /**
    * Gets current theory
    *
    * @return current(dynamic) theory
    */
  def getTheory: Theory = Try(new Theory(theoryManager.getTheory(true))).toOption.orNull

  /**
    * Gets last consulted theory, with the original textual format
    *
    * @return theory
    */
  def getLastConsultedTheory: Theory = theoryManager.getLastConsultedTheory

  /**
    * Clears current theory
    */
  def clearTheory(): Unit = setTheory(new Theory)

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
  def loadLibrary(className: String): Library = libManager.loadLibrary(className)

  /**
    * Loads a library.
    *
    * If a library with the same name is already present, a warning event is notified and the request is ignored.
    *
    * @param libClass the library class to be loaded.
    * @tparam L parameter to allow for any subclass of library to be used.
    * @throws InvalidLibraryException if we cannot create a valid library.
    * @return the reference to the Library just loaded.
    */
  @throws(classOf[InvalidLibraryException])
  def loadLibrary[L <: Library](libClass: Class[L]): Library = libManager.loadLibrary(libClass)

  /**
    * Loads a specific instance of a library
    *
    * If a library with the same name is already present, a warning event is notified
    *
    * @param lib the (Java class) name of the library to be loaded
    * @throws InvalidLibraryException if name is not a valid library
    */
  @throws(classOf[InvalidLibraryException])
  def loadLibrary(lib: Library): Library = libManager.loadLibrary(lib)

  /**
    * Gets the list of current libraries loaded
    *
    * @return the list of the library names
    */
  def getCurrentLibraries: Array[String] = libManager.getCurrentLibraries

  /**
    * Unloads a previously loaded library
    *
    * @param name of the library to be unloaded
    * @throws InvalidLibraryException if name is not a valid loaded library
    */
  @throws(classOf[InvalidLibraryException])
  def unloadLibrary(name: String) : Unit = libManager.unloadLibrary(name)

  /**
    * Gets the reference to a loaded library
    *
    * @param name the name of the library already loaded
    * @return the reference to the library loaded, null if the library is not found
    */
  def getLibrary(name: String): Library = libManager.getLibrary(name).orNull

  protected def getLibraryPredicate(name: String, nArgs: Int): Library = _primitiveManager.getLibraryPredicate(name, nArgs)

  protected def getLibraryFunctor(name: String, nArgs: Int): Library = _primitiveManager.getLibraryFunctor(name, nArgs)

  /**
    * Gets the list of the operators currently defined
    *
    * @return the list of the operators
    */
  def getCurrentOperatorList: ju.List[Operator] = _opManager.getOperators

  /**
    * Solves a query
    *
    * @param g the term representing the goal to be demonstrated
    * @return the result of the demonstration
    * @see Solution
    **/
  def solve(g: Term): Solution = {
    if (g == null) return null
    val sinfo: Solution = engManager.solve(g)
    val ev: QueryEvent = new QueryEvent(this, sinfo)
    notifyNewQueryResultAvailable(ev)
    sinfo
  }

  /**
    * Solves a query
    *
    * @param st the string representing the goal to be demonstrated
    * @return the result of the demonstration
    * @see Solution
    **/
  @throws(classOf[MalformedGoalException])
  def solve(st: String): Solution = {
    try {
      val p: Parser = new Parser(_opManager, st)
      val t: Term = p.nextTerm(true)
      solve(t)
    } catch {
      case ex: InvalidTermException => throw new MalformedGoalException
    }
  }

  /**
    * Gets next solution
    *
    * @return the result of the demonstration
    * @throws NoMoreSolutionsException if no more solutions are present
    * @see Solution
    **/
  @throws(classOf[NoMoreSolutionsException])
  def solveNext: Solution = {
    if (hasOpenAlternatives) {
      val sinfo: Solution = engManager.solveNext
      val ev: QueryEvent = new QueryEvent(this, sinfo)
      notifyNewQueryResultAvailable(ev)
      sinfo
    } else {
      throw new NoMoreSolutionsException
    }
  }

  /**
    * Halts current solve computation
    */
  def solveHalt(): Unit = engManager.solveHalt()

  /**
    * Accepts current solution
    */
  def solveEnd(): Unit = engManager.solveEnd()

  /**
    * Asks if there is the presence of any open alternatives to be explored in current demonstration process.
    *
    * @return true if open alternatives are present, false otherwise.
    */
  def hasOpenAlternatives: Boolean = engManager.hasOpenAlternatives

  /**
    * Identify functors
    *
    * @param term term to identify
    */
  def identifyFunctor(term: Term):Unit =_primitiveManager.identifyFunctor(term)

  /**
    * Gets a term from a string, using the operators currently
    * defined by the engine
    *
    * @param st the string representing a term
    * @return the term parsed from the string
    * @throws InvalidTermException if the string does not represent a valid term
    */
  @throws(classOf[InvalidTermException])
  def toTerm(st: String): Term = Parser.parseSingleTerm(st, _opManager)

  /**
    * Gets the string representation of a term, using operators
    * currently defined by engine
    *
    * @param term the term to be represented as a string
    * @return the string representing the term
    */
  def toString(term: Term): String = term.toStringAsArgY(_opManager, OperatorManager.OP_HIGH)

  /**
    * Defines a new flag
    */
  def defineFlag(name: String, valueList: Struct, defValue: Term, modifiable: Boolean, libName: String): Boolean = {
    flagManager.defineFlag(name, valueList, defValue, modifiable, libName)
  }

  /**
    * Switches on/off the notification of spy information events
    *
    * @param state - true for enabling the notification of spy event
    */
  def setSpy(state: Boolean) {
    _spy = state
  }

  /**
    * Checks the spy state of the engine
    *
    * @return true if the engine emits spy information
    */
  def isSpy: Boolean = {
    _spy
  }

  /**
    * Notifies a spy information event
    */
  def spy(s: String) {
    if (_spy) {
      notifySpy(new SpyEvent(this, s))
    }
  }

  /**
    * Notifies a spy information event
    *
    * @param s TODO
    */
  def spy(s: String, e: Engine) {
    if (_spy) {
      val ctx: ExecutionContext = e.context
      var i: Int = 0
      var g: String = "-"
      if (ctx.fatherCtx != null) {
        i = ctx.depth - 1
        g = ctx.fatherCtx.currentGoal.toString
      }
      notifySpy(new SpyEvent(this, e, "spy: " + i + "  " + s + "  " + g))
    }
  }

  /**
    * Produces an output information event
    *
    * @param m the output string
    */
  def stdOutput(m: String) {
    notifyOutput(new OutputEvent(this, m))
  }

  /**
    * Adds a listener to ouput events
    *
    * @param l the listener
    */
  def addOutputListener(l: OutputListener): Unit = _outputListeners.add(l)

  /**
    * Adds a listener to theory events
    *
    * @param l the listener
    */
  def addTheoryListener(l: TheoryListener): Unit = _theoryListeners.add(l)

  /**
    * Adds a listener to theory events
    *
    * @param l the listener
    */
  def addQueryListener(l: QueryListener): Unit = _queryListeners.add(l)

  /**
    * Adds a listener to spy events
    *
    * @param l the listener
    */
  def addSpyListener(l: SpyListener): Unit = _spyListeners.add(l)

  /**
    * Removes a listener to ouput events
    *
    * @param l the listener
    */
  def removeOutputListener(l: OutputListener) : Unit = _outputListeners.remove(l)

  /**
    * Removes all output event listeners
    */
  def removeAllOutputListeners(): Unit =_outputListeners.clear()

  /**
    * Removes a listener to theory events
    *
    * @param l the listener
    */
  def removeTheoryListener(l: TheoryListener): Unit =_theoryListeners.remove(l)

  /**
    * Removes a listener to query events
    *
    * @param l the listener
    */
  def removeQueryListener(l: QueryListener): Unit = _queryListeners.remove(l)

  /**
    * Removes a listener to spy events
    *
    * @param l the listener
    */
  def removeSpyListener(l: SpyListener): Unit = _spyListeners.remove(l)

  /**
    * Removes all spy event listeners
    */
  def removeAllSpyListeners():Unit = _spyListeners.clear()

  /**
    * Gets a copy of current listener list to output events
    */
  def getOutputListenerList: ju.List[OutputListener] = {
    new ju.ArrayList[OutputListener](_outputListeners)
  }

  /**
    * Gets a copy of current listener list to spy events
    *
    */
  def getSpyListenerList: ju.List[SpyListener] = {
    new ju.ArrayList[SpyListener](_spyListeners)
  }

  /**
    * Gets a copy of current listener list to theory events
    *
    */
  def getTheoryListenerList: ju.List[TheoryListener] = {
    new ju.ArrayList[TheoryListener](_theoryListeners)
  }

  /**
    * Gets a copy of current listener list to query events
    *
    */
  def getQueryListenerList: ju.List[QueryListener] = {
    new ju.ArrayList[QueryListener](_queryListeners)
  }

  /**
    * Notifies an ouput information event
    *
    * @param e the event
    */
  protected def notifyOutput(e: OutputEvent) {
    import scala.collection.JavaConversions._
    for (ol <- _outputListeners) {
      ol.onOutput(e)
    }
  }

  /**
    * Notifies a spy information event
    *
    * @param e the event
    */
  protected def notifySpy(e: SpyEvent) {
    import scala.collection.JavaConversions._
    for (sl <- _spyListeners) {
      sl.onSpy(e)
    }
  }

  /**
    * Notifies a new theory set or updated event
    *
    * @param e the event
    */
  protected def notifyChangedTheory(e: TheoryEvent) {
    import scala.collection.JavaConversions._
    for (tl <- _theoryListeners) {
      tl.theoryChanged(e)
    }
  }

  /**
    * Notifies a library loaded event
    *
    * @param e the event
    */
  protected def notifyNewQueryResultAvailable(e: QueryEvent) {
    import scala.collection.JavaConversions._
    for (ql <- _queryListeners) {
      ql.newQueryResultAvailable(e)
    }
  }

  /**
    * Append a new path to directory list
    *
    */
  def pushDirectoryToList(path: String) {
    absolutePathList.add(path)
  }

  /**
    *
    * Retract an element from directory list
    */
  def popDirectoryFromList {
    if (!absolutePathList.isEmpty) {
      absolutePathList.remove(absolutePathList.size - 1)
    }
  }

  /**
    *
    * Reset directory list
    */
  def resetDirectoryList(path: String) {
    absolutePathList = new ju.ArrayList[String]
    absolutePathList.add(path)
  }

  def termSolve(st: String): Term = {
    try {
      val p: Parser = new Parser(_opManager, st)
      val t: Term = p.nextTerm(true)
      t
    }
    catch {
      case e: InvalidTermException => {
        val s: String = "null"
        val t: Term = Term.createTerm(s)
        t
      }
    }
  }
}