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
 *//*
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

import alice.tuprolog.event.{QueryEvent, SpyEvent, TheoryEvent}
import alice.tuprolog.{InvalidLibraryException, InvalidTermException, InvalidTheoryException, Library, MalformedGoalException, NoMoreSolutionException, Operator, Prolog, Term}
import alice.tuprolog.interfaces._
import alice.tuprolog.json.{AbstractEngineState, FullEngineState, JSONSerializerManager, ReducedEngineState}
import alice.tuprolog.lib.{IOLibrary, ISOLibrary, OOLibrary}
import com.szadowsz.gospel.core.db.LibraryManager
import com.szadowsz.gospel.core.db.libs.MyBasicLibrary
import com.szadowsz.gospel.core.db.primitives.PrimitiveManager
import com.szadowsz.gospel.core.db.theory.TheoryManager
import com.szadowsz.gospel.core.engine.{Engine, EngineManager}
import com.szadowsz.gospel.core.engine.flags.FlagManager
import com.szadowsz.gospel.core.parser.Parser

import scala.util.Try

/**
  * This class represents a tuProlog engine.
  *
  * Created on 15/02/2017.
  *
  * @version Gospel 2.0.0
  */
class PrologEngine protected(spy: Boolean, warning: Boolean) extends Prolog(spy, warning) {

  private lazy val engManager = EngineManager(this) // primitive prolog term manager.

  private lazy val flagManager = new FlagManager() // engine flag manager.

  private lazy val libManager = LibraryManager(this) // manager of loaded libraries

  private lazy val primManager = PrimitiveManager(this) // primitive prolog term manager.

  private lazy val theoryManager = TheoryManager(this) // manager of current theories

  private val engineState = new FullEngineState

  //used in serialization
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
    * Method to retrieve the db component that manages primitives.
    *
    * @return the primitive manager instance attached to this prolog engine.
    */
  def getPrimitiveManager: PrimitiveManager = primManager // TODO Make Internal only


  /**
    * Method to retrieve the db component that manages primitives.
    *
    * @return the primitive manager instance attached to this prolog engine.
    */
  def getEngineManager: EngineManager = engManager // TODO Make Internal only

  /**
    * method to retrieve the component managing the theory.
    *
    * @return the theory manager instance attached to this prolog engine.
    */
  def getTheoryManager: TheoryManager = theoryManager // TODO Make Internal only

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
  def loadLibrary(lib: Library): Unit = libManager.loadLibrary(lib)

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
  @throws[MalformedGoalException]
  def solve(st: String): Solution = {
    try {
      val p = new Parser(opManager, st)
      val t = p.nextTerm(true)
      solve(t)
    } catch {
      case ex: InvalidTermException => throw new MalformedGoalException
    }
  }

  /**
    * Gets next solution
    *
    * @return the result of the demonstration
    * @throws NoMoreSolutionException if no more solutions are present
    * @see SolveInfo
    **/
  @throws[NoMoreSolutionException]
  def solveNext(): Solution = {
    if (hasOpenAlternatives) {
      val sinfo: Solution = engManager.solveNext
      val ev: QueryEvent = new QueryEvent(this, sinfo)
      notifyNewQueryResultAvailable(ev)
      sinfo
    }
    else throw new NoMoreSolutionException
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
  def toTerm(st: String): Term = Parser.parseSingleTerm(st, opManager)

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
      val t = p.nextTerm(true)
      t
    } catch {
      case e: InvalidTermException =>
        val s = "null"
        val t = Term.createTerm(s)
        t
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

  def toJSON(alsoKB: Boolean): String = {
    var brain: AbstractEngineState = null
    if (alsoKB) {
      brain = this.engineState
      this.theoryManager.serializeLibraries(brain.asInstanceOf[FullEngineState])
      this.theoryManager.serializeDynDataBase(brain.asInstanceOf[FullEngineState])
      brain.asInstanceOf[FullEngineState].setOp(getOperatorManager.getOperators.asInstanceOf[util.LinkedList[Operator]])
    } else {
      brain = new ReducedEngineState

    }
    theoryManager.serializeTimestamp(brain)
    engManager.serializeQueryState(brain)
    flagManager.serializeFlags(brain)
    JSONSerializerManager.toJSON(brain)
  }

}