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

import java.util

import com.szadowsz.gospel.core.data.Term
import com.szadowsz.gospel.core.db.{Library, LibraryManager, LibraryManagerFactory}
import com.szadowsz.gospel.core.db.libs._
import com.szadowsz.gospel.core.db.ops.{Operator, OperatorManager}
import com.szadowsz.gospel.core.db.primitives.PrimitiveManager
import com.szadowsz.gospel.core.db.theory.TheoryManager
import com.szadowsz.gospel.core.engine.{Engine, EngineManager}
import com.szadowsz.gospel.core.engine.flags.FlagManager
import com.szadowsz.gospel.core.error._
import com.szadowsz.gospel.core.event.interpreter._
import com.szadowsz.gospel.core.event.io.OutputEvent
import com.szadowsz.gospel.core.json.{EngineState, JSONSerializerManager}
import com.szadowsz.gospel.core.listener._
import com.szadowsz.gospel.core.parser.Parser
import com.szadowsz.gospel.core.utils.VersionInfo

import scala.util.Try
import scala.collection.JavaConverters._
import java.io.{File, FileInputStream}

import com.szadowsz.gospel.core.exception.{InvalidLibraryException, InvalidTermException, InvalidTheoryException}


object PrologEngine {

  /**
    * Gets the current version of the tuProlog system
    */
  def getVersion: String = VersionInfo.getEngineVersion

  def fromJSON(jsonString: String): PrologEngine = {
    val brain = JSONSerializerManager.fromJSON(jsonString, classOf[EngineState])
    val engine = new PrologEngine(brain.getLibraries)
    engine.setTheory(new Theory(brain.getDynTheory))
    brain.getOp.asScala.foreach(o => engine.getOperatorManager.opNew(o.name, o.`type`, o.prio))
    engine.getFlagManager.reloadFlags(brain)
    if (brain.hasOpenAlternatives) {
      for (i <- 0 until brain.getNumberAskedResults) {
        engine.solve(brain.getQuery)
      }
    }
    engine
  }
}
// scalastyle:off number.of.methods

/**
  * This class represents a tuProlog engine.
  *
  * Created on 15/02/2017.
  *
  * @version Gospel 2.0.0
  */
class PrologEngine protected(spyFlag: Boolean, warningFlag: Boolean) {

  protected lazy val engManager = EngineManager(this) // primitive prolog term manager.

  protected lazy val flagManager = new FlagManager() // engine flag manager.

  protected lazy val libManager = LibraryManagerFactory.getManagerForCurrPlatform(this) // manager of loaded libraries

  protected lazy val opManager: OperatorManager = new OperatorManager // manager of operators

  protected lazy val primManager = PrimitiveManager(this) // primitive prolog term manager.

  protected lazy val theoryManager = TheoryManager(this) // manager of current theories

  protected val outputListeners = new util.ArrayList[OutputListener]()

  protected val spyListeners = new util.ArrayList[SpyListener]() // listeners registered for virtual machine internal events

  protected val warningListeners = new util.ArrayList[WarningListener]() // listeners registered for virtual machine state change events

  protected val exceptionListeners = new util.ArrayList[ExceptionListener]() // listeners registered for virtual machine state exception events

  protected val theoryListeners = new util.ArrayList[TheoryListener]() // listeners to theory events

  protected val libraryListeners = new util.ArrayList[LibraryListener]() // listeners to library events

  protected val queryListeners = new util.ArrayList[QueryListener]() // listeners to query events

  private val engineState = new EngineState

  protected var spy = spyFlag

  protected var warning = warningFlag //  warning activated ?

  protected var exception = true // exception activated ?

  protected var absolutePathList = new util.ArrayList[String]()
  protected var lastPath: String = _

  /**
    * Builds a Prolog engine with loaded the specified libraries.
    *
    * @param libs the (class) names of the libraries to be loaded
    */
  @throws(classOf[InvalidLibraryException])
  def this(libs: Array[String]) {
    this(false, true)
    libs.foreach(s => loadLibrary(s))
  }

  /**
    * Builds a Prolog engine with loaded the specified libraries.
    *
    * @param libs the classes of the libraries to be loaded
    */
  def this(libs: Array[Class[_ <: Library]]) {
    this(false, true)
    libs.foreach(c => loadLibrary(c.getName))
  }

  /**
    * Builds a Prolog engine with default libraries loaded.
    *
    * The default libraries are BasicLibrary, ISOLibrary, IOLibrary, and OOLibrary.
    */
  def this() {
    this(Array[Class[_ <: Library]](
      classOf[MyBasicLibrary],
      classOf[ISOLibrary],
      classOf[IOLibrary],
      classOf[OOLibrary]
    ))
  }

  protected def getLibraryPredicate(name: String, nArgs: Int): Library = primManager.getLibraryPredicate(name, nArgs) // TODO comment or remove

  protected def getLibraryFunctor(name: String, nArgs: Int): Library = primManager.getLibraryFunctor(name, nArgs) // TODO comment or remove

  /**
    * Method to retrieve the db component that manages primitives.
    *
    * @return the primitive manager instance attached to this prolog engine.
    */
  def getEngineManager: EngineManager = engManager // TODO Make Internal only

  /**
    * Method to retrieve the engine component managing flags.
    *
    * @return the flag manager instance attached to this prolog engine.
    */
  def getFlagManager: FlagManager = flagManager // TODO Make Internal only

  /**
    * Method to retrieve the db component that manages libraries.
    *
    * @return the library manager instance attached to this prolog engine.
    */
  def getLibraryManager: LibraryManager = libManager // TODO Make Internal only


  /**
    * Method to retrieve the db component that manages operators.
    *
    * @return the operator manager instance attached to this prolog engine.
    */
  def getOperatorManager: OperatorManager = opManager // TODO Make Internal only

  /**
    * Method to retrieve the db component that manages primitives.
    *
    * @return the primitive manager instance attached to this prolog engine.
    */
  def getPrimitiveManager: PrimitiveManager = primManager // TODO Make Internal only


  /**
    * method to retrieve the component managing the theory.
    *
    * @return the theory manager instance attached to this prolog engine.
    */
  def getTheoryManager: TheoryManager = theoryManager // TODO Make Internal only

  /**
    * Gets the list of the operators currently defined
    *
    * @return the list of the operators
    */
  def getCurrentOperatorList: util.List[Operator] = opManager.getOperators

  /**
    * Gets the list of current libraries loaded
    *
    * @return the list of the library names
    */
  def getCurrentLibraries: Array[String] = libManager.getCurrentLibraries

  /**
    * Gets the reference to a loaded library
    *
    * @param name the name of the library already loaded
    * @return the reference to the library loaded, null if the library is not found
    */
  def getLibrary(name: String): Library = libManager.getLibrary(name)

  /**
    * Gets a copy of current listener list to output events
    */
  def getOutputListenerList = new util.ArrayList[OutputListener](outputListeners)

  /**
    * Gets a copy of current listener list to warning events
    *
    */
  def getWarningListenerList = new util.ArrayList[WarningListener](warningListeners)

  /*Castagna 06/2011*/
  /**
    * Gets a copy of current listener list to exception events
    *
    */
  def getExceptionListenerList = new util.ArrayList[ExceptionListener](exceptionListeners)

  /**/
  /**
    * Gets a copy of current listener list to spy events
    *
    */
  def getSpyListenerList = new util.ArrayList[SpyListener](spyListeners)

  /**
    * Gets a copy of current listener list to theory events
    *
    */
  def getTheoryListenerList = new util.ArrayList[TheoryListener](theoryListeners)

  /**
    * Gets a copy of current listener list to library events
    *
    */
  def getLibraryListenerList: util.List[LibraryListener] = new util.ArrayList[LibraryListener](libraryListeners)

  /**
    * Gets a copy of current listener list to query events
    *
    */
  def getQueryListenerList: util.List[QueryListener] = new util.ArrayList[QueryListener](queryListeners)

  /**
    * Adds a listener to output events
    *
    * @param l the listener
    */
  def addOutputListener(l: OutputListener): Unit = outputListeners.add(l)

  /**
    * Adds a listener to theory events
    *
    * @param l the listener
    */
  def addTheoryListener(l: TheoryListener): Unit = theoryListeners.add(l)

  /**
    * Adds a listener to library events
    *
    * @param l the listener
    */
  def addLibraryListener(l: LibraryListener): Unit = libraryListeners.add(l)

  /**
    * Adds a listener to theory events
    *
    * @param l the listener
    */
  def addQueryListener(l: QueryListener): Unit = queryListeners.add(l)

  /**
    * Adds a listener to spy events
    *
    * @param l the listener
    */
  def addSpyListener(l: SpyListener): Unit = spyListeners.add(l)

  /**
    * Adds a listener to warning events
    *
    * @param l the listener
    */
  def addWarningListener(l: WarningListener): Unit = warningListeners.add(l)

  /**
    * Adds a listener to exception events
    *
    * @param l the listener
    */
  def addExceptionListener(l: ExceptionListener): Unit = exceptionListeners.add(l)

  /**/
  /**
    * Removes a listener to ouput events
    *
    * @param l the listener
    */
  def removeOutputListener(l: OutputListener): Unit = outputListeners.remove(l)

  /**
    * Removes a listener to theory events
    *
    * @param l the listener
    */
  def removeTheoryListener(l: TheoryListener): Unit = theoryListeners.remove(l)

  /**
    * Removes a listener to library events
    *
    * @param l the listener
    */
  def removeLibraryListener(l: LibraryListener): Unit = libraryListeners.remove(l)

  /**
    * Removes a listener to query events
    *
    * @param l the listener
    */
  def removeQueryListener(l: QueryListener): Unit = queryListeners.remove(l)

  /**
    * Removes a listener to spy events
    *
    * @param l the listener
    */
  def removeSpyListener(l: SpyListener): Unit = spyListeners.remove(l)

  /**
    * Removes a listener to warning events
    *
    * @param l the listener
    */
  def removeWarningListener(l: WarningListener): Unit = warningListeners.remove(l)

  /**
    * Removes a listener to exception events
    *
    * @param l the listener
    */
  def removeExceptionListener(l: ExceptionListener): Unit = exceptionListeners.remove(l)

  /**
    * Removes all spy event listeners
    */
  def removeAllSpyListeners(): Unit = spyListeners.clear()

  /**
    * Removes all output event listeners
    */
  def removeAllOutputListeners(): Unit = outputListeners.clear()

  /**
    * Removes all warning event listeners
    */
  def removeAllWarningListeners(): Unit = warningListeners.clear()

  /**
    * Removes all exception event listeners
    */
  def removeAllExceptionListeners(): Unit = exceptionListeners.clear()

  /**
    * Switches on/off the notification of spy information events
    *
    * @param state - true for enabling the notification of spy event
    */
  def setSpy(state: Boolean): Unit = spy = state

  /**
    * Checks the spy state of the engine
    *
    * @return true if the engine emits spy information
    */
  def isSpy: Boolean = spy

  /**
    * Switches on/off the notification of warning information events
    *
    * @param state - true for enabling warning information notification
    */
  def setWarning(state: Boolean): Unit = warning = state

  /**
    * Checks if warning information are notified
    *
    * @return true if the engine emits warning information
    */
  def isWarning: Boolean = warning

  /**/
  /*Castagna 06/2011*/
  /**
    * Checks if exception information are notified
    *
    * @return true if the engine emits exception information
    */
  def isException: Boolean = exception

  /**/
  /*Castagna 06/2011*/
  /**
    * Switches on/off the notification of exception information events
    *
    * @param state - true for enabling exception information notification
    */
  def setException(state: Boolean): Unit = exception = state

  /**
    * Identify any functors.
    *
    * @param term the term to identify.
    */
  def identifyFunctor(term: Term): Unit = primManager.identifyFunctor(term)

  /**
    * Unloads a previously loaded library
    *
    * @param name of the library to be unloaded
    * @throws InvalidLibraryException if name is not a valid loaded library
    */
  @throws(classOf[InvalidLibraryException])
  def unloadLibrary(name: String): Unit = libManager.unloadLibrary(name)

  /**
    * Loads a library.
    *
    * If a library with the same name is already present, a warning event is notified and the request is ignored.
    *
    * @param file the name of the Java class containing the library to be loaded.
    * @throws InvalidLibraryException if we cannot create a valid library.
    * @return the reference to the Library just loaded.
    */
  @throws(classOf[InvalidLibraryException])
  def loadLibrary(file: File): Library = libManager.loadLibrary(new TheoryLibrary(file.getName.dropRight(3),new Theory(new FileInputStream(file))))


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
    * If a library with the same name is already present,
    * a warning event is notified and the request is ignored.
    *
    * @param className name of the Java class containing the library to be loaded
    * @param paths     The path where is contained the library.
    * @return the reference to the Library just loaded
    * @throws InvalidLibraryException if name is not a valid library
    */
  @throws[InvalidLibraryException]
  def loadLibrary(className: String, paths: Array[String]): Library = libManager.loadLibrary(className, paths)

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
    * Sets a new theory
    *
    * @param th is the new theory
    * @throws InvalidTheoryException if the new theory is not valid
    * @see alice.gospel.core.theory.Theory
    */
  @throws(classOf[InvalidTheoryException])
  def setTheory(th: Theory): Unit = {
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
    * Solves a query
    *
    * @param g the term representing the goal to be demonstrated
    * @return the result of the demonstration
    * @see SolveInfo
    **/
  def solve(g: Term): Solution = {
    //System.out.println("ENGINE SOLVE #0: "+g);
    if (g == null) {
      null
    } else {
      val sinfo: Solution = engManager.solve(g)
      val ev: QueryEvent = new QueryEvent(this, sinfo)
      notifyNewQueryResultAvailable(ev)
      sinfo
    }
  }

  /**
    * Solves a query
    *
    * @param st the string representing the goal to be demonstrated
    * @return the result of the demonstration
    * @see SolveInfo
    **/
  @throws[InvalidTermException]
  def solve(st: String): Solution = {
    try {
      val p = new Parser(opManager, st)
      val t = p.nextTerm(true)
      solve(t)
    } catch {
      case ex: InvalidTermException =>
        throw new InvalidTermException("Goal is Malformed",ex, ex.getTerm, ex.getLine, ex.getPos)
    }
  }
  /**
    * Gets next solution
    *
    * @return the result of the demonstration
    * @see Solution
    **/
  def solveNext(): Solution = solveNextOpt().orNull

  /**
    * Gets next solution
    *
    * @return the result of the demonstration
    * @see Solution
    **/
  def solveNextOpt(): Option[Solution] = {
    if (hasOpenAlternatives) {
      val sinfo: Solution = engManager.solveNext
      val ev: QueryEvent = new QueryEvent(this, sinfo)
      notifyNewQueryResultAvailable(ev)
      Option(sinfo)
    } else {
      None
    }
  }

  /**
    * Halts current solve computation
    */
  def solveHalt() {
    engManager.solveHalt()
  }

  /**
    * Accepts current solution
    */
  def solveEnd() {
    engManager.solveEnd()
  }

  @throws[InvalidTermException]
  def createTerm(st: String): Term = Parser.parseSingleTerm(st, opManager)

  def createTerms(st: String): util.Iterator[Term] = new Parser(opManager, st).iterator

  /**
    * Gets the string representation of a term, using operators
    * currently defined by engine
    *
    * @param term the term to be represented as a string
    * @return the string representing the term
    */
  def toString(term: Term): String = term.toStringAsArgY(opManager, OperatorManager.OP_HIGH)

  /**
    * Unifies two terms using current demonstration context.
    *
    * @param t0 first term to be unified
    * @param t1 second term to be unified
    * @return true if the unification was successful
    */
  def unify(t0: Term, t1: Term): Boolean = t0.unify(this, t1)

  /**
    * Unifies two terms using current demonstration context.
    *
    * @param t0 first term to be unified
    * @param t1 second term to be unified
    * @return true if the unification was successful
    */
  def `match`(t0: Term, t1: Term): Boolean = t0.`match`(this.getFlagManager.isOccursCheckEnabled, t1)

  def termSolve(st: String): Term = {
    try {
      val p = new Parser(opManager, st)
      p.nextTerm(true)
    } catch {
      case e: InvalidTermException => createTerm("null")
    }
  }

  /**
    * Asks for the presence of open alternatives to be explored
    * in current demostration process.
    *
    * @return true if open alternatives are present
    */
  def hasOpenAlternatives: Boolean = engManager.hasOpenAlternatives

  /**
    * Notifies a spy information event
    */
  protected def spy(s: String) {
    if (spy) notifySpy(new SpyEvent(this, s))
  }

  /**
    * Notifies a spy information event
    *
    * @param s TODO
    */
  def spy(s: String, e: Engine) {
    //System.out.println("spy: "+i+"  "+s+"  "+g);
    if (spy) {
      val ctx = e.currentContext
      var i = 0
      var g = "-"
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
  def stdOutput(m: String): Unit = notifyOutput(new OutputEvent(this, m))

  /**
    * Notifies a warn information event
    *
    * @param m the warning message
    */
  def warn(m: String) {
    if (warning) {
      notifyWarning(new WarningEvent(this, m))
      //log.warn(m);
    }
  }

  /**
    * Notifies a exception information event
    *
    * @param m the exception message
    */
  def exception(m: String) {
    if (exception) {
      notifyException(new ExceptionEvent(this, m))
    }
  }

  /**
    * Notifies an output information event
    *
    * @param e the event
    */
  protected def notifyOutput(e: OutputEvent): Unit = outputListeners.asScala.foreach(sl => sl.onOutput(e))

  /**
    * Notifies a spy information event
    *
    * @param e the event
    */
  protected def notifySpy(e: SpyEvent): Unit = spyListeners.asScala.foreach(sl => sl.onSpy(e))

  /**
    * Notifies a warning information event
    *
    * @param e the event
    */
  protected def notifyWarning(e: WarningEvent): Unit = warningListeners.asScala.foreach(wl => wl.onWarning(e))

  /**
    * Notifies a exception information event
    *
    * @param e the event
    */
  protected def notifyException(e: ExceptionEvent): Unit = exceptionListeners.asScala.foreach(el => el.onException(e))

  /**
    * Notifies a new theory set or updated event
    *
    * @param e the event
    */
  protected def notifyChangedTheory(e: TheoryEvent): Unit = theoryListeners.asScala.foreach(tl => tl.theoryChanged(e))

  /**
    * Notifies a library loaded event
    *
    * @param e the event
    */
  def notifyLoadedLibrary(e: LibraryEvent): Unit = libraryListeners.asScala.foreach(ll => ll.libraryLoaded(e))

  /**
    * Notifies a library unloaded event
    *
    * @param e the event
    */
  protected def notifyUnloadedLibrary(e: LibraryEvent): Unit = libraryListeners.asScala.foreach(ll => ll.libraryUnloaded(e))

  /**
    * Notifies a new query result available event
    *
    * @param e the event
    */
  protected def notifyNewQueryResultAvailable(e: QueryEvent): Unit = queryListeners.asScala.foreach(ql => ql.newQueryResultAvailable(e))

  /**
    * Gets the last Element of the path list
    */
  def getCurrentDirectory: String = {
    var directory = ""
    if (absolutePathList.isEmpty) if (this.lastPath != null) directory = this.lastPath
    else directory = System.getProperty("user.dir")
    else directory = absolutePathList.get(absolutePathList.size - 1)
    directory
  }

  /**
    * Sets the last Element of the path list
    */
  def setCurrentDirectory(s: String): Unit = lastPath = s

  /**
    * Append a new path to directory list
    *
    */
  def pushDirectoryToList(path: String): Unit = absolutePathList.add(path)

  /**
    *
    * Retract an element from directory list
    */
  def popDirectoryFromList(): Unit = {
    if (!absolutePathList.isEmpty) absolutePathList.remove(absolutePathList.size - 1)
  }

  /**
    *
    * Reset directory list
    */
  def resetDirectoryList(path: String): Unit = {
    absolutePathList = new util.ArrayList[String]
    absolutePathList.add(path)
  }

  def toJSON(): String = {
    val brain = this.engineState
    theoryManager.serializeLibraries(brain.asInstanceOf[EngineState])
    theoryManager.serializeDynDataBase(brain.asInstanceOf[EngineState])
    brain.asInstanceOf[EngineState].setOp(opManager.getOperators.asInstanceOf[util.LinkedList[Operator]])
    theoryManager.serializeTimestamp(brain)
    engManager.serializeQueryState(brain)
    flagManager.serializeFlags(brain)
    JSONSerializerManager.toJSON(brain)
  }
}