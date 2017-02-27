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

import alice.tuprolog.event.{QueryEvent, TheoryEvent}
import alice.tuprolog.{InvalidLibraryException, InvalidTheoryException, Library, NoMoreSolutionException, Operator, Prolog, SolveInfo, Term}
import alice.tuprolog.interfaces._
import alice.tuprolog.json.{AbstractEngineState, FullEngineState, JSONSerializerManager, ReducedEngineState}
import alice.tuprolog.lib.{IOLibrary, ISOLibrary, OOLibrary}
import com.szadowsz.gospel.core.db.LibManager
import com.szadowsz.gospel.core.db.libs.MyBasicLibrary
import com.szadowsz.gospel.core.db.primitives.PrimitiveManager
import com.szadowsz.gospel.core.db.theory.TheoryManager
import com.szadowsz.gospel.core.engine.EngineManager
import com.szadowsz.gospel.core.engine.flags.FlagManager

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

  private lazy val libManager = LibManager(this) // manager of loaded libraries

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

  override protected def getLibraryPredicate(name: String, nArgs: Int): Library = primManager.getLibraryPredicate(name, nArgs) // TODO comment or remove

  override protected def getLibraryFunctor(name: String, nArgs: Int): Library = primManager.getLibraryFunctor(name, nArgs) // TODO comment or remove

  /**
    * Method to retrieve the engine component managing flags.
    *
    * @return the flag manager instance attached to this prolog engine.
    */
  override def getFlagManager: IFlagManager = flagManager // TODO Make Internal only

  /**
    * Method to retrieve the db component that manages libraries.
    *
    * @return the library manager instance attached to this prolog engine.
    */
  override def getLibraryManager: ILibraryManager = libManager // TODO Make Internal only

  /**
    * Method to retrieve the db component that manages primitives.
    *
    * @return the primitive manager instance attached to this prolog engine.
    */
  override def getPrimitiveManager: IPrimitiveManager = primManager // TODO Make Internal only


  /**
    * Method to retrieve the db component that manages primitives.
    *
    * @return the primitive manager instance attached to this prolog engine.
    */
  override def getEngineManager: IEngineManager = engManager // TODO Make Internal only

  /**
    * method to retrieve the component managing the theory.
    *
    * @return the theory manager instance attached to this prolog engine.
    */
  override def getTheoryManager: ITheoryManager = theoryManager // TODO Make Internal only

  /**
    * Gets the list of current libraries loaded
    *
    * @return the list of the library names
    */
  override def getCurrentLibraries: Array[String] = libManager.getCurrentLibraries

  /**
    * Gets the reference to a loaded library
    *
    * @param name the name of the library already loaded
    * @return the reference to the library loaded, null if the library is not found
    */
  override def getLibrary(name: String): Library = libManager.getLibrary(name)

  /**
    * Identify any functors.
    *
    * @param term the term to identify.
    */
  override def identifyFunctor(term: Term): Unit = primManager.identifyFunctor(term)

  /**
    * Unloads a previously loaded library
    *
    * @param name of the library to be unloaded
    * @throws InvalidLibraryException if name is not a valid loaded library
    */
  @throws(classOf[InvalidLibraryException])
  override def unloadLibrary(name: String): Unit = libManager.unloadLibrary(name)

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
  override def loadLibrary(className: String): Library = libManager.loadLibrary(className)

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
  override def loadLibrary(className: String, paths: Array[String]): Library = libManager.loadLibrary(className, paths)

  /**
    * Loads a specific instance of a library
    *
    * If a library with the same name is already present, a warning event is notified
    *
    * @param lib the (Java class) name of the library to be loaded
    * @throws InvalidLibraryException if name is not a valid library
    */
  @throws(classOf[InvalidLibraryException])
  override def loadLibrary(lib: Library): Unit = libManager.loadLibrary(lib)

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
  override def setTheory(th: ITheory): Unit = {
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
  override def addTheory(th: ITheory): Unit = {
    val oldTh: ITheory = getTheory
    theoryManager.consult(th, true, null)
    theoryManager.solveTheoryGoal()
    val newTh: ITheory = getTheory
    val ev: TheoryEvent = new TheoryEvent(this, oldTh, newTh)
    this.notifyChangedTheory(ev)
  }

  /**
    * Gets current theory
    *
    * @return current(dynamic) theory
    */
  override def getTheory: ITheory = Try(new Theory(theoryManager.getTheory(true))).toOption.orNull

  /**
    * Gets last consulted theory, with the original textual format
    *
    * @return theory
    */
  override def getLastConsultedTheory: ITheory = theoryManager.getLastConsultedTheory

  /**
    * Clears current theory
    */
  override def clearTheory(): Unit = setTheory(new Theory)

  /**
    * Solves a query
    *
    * @param g the term representing the goal to be demonstrated
    * @return the result of the demonstration
    * @see SolveInfo
    **/
  override def solve(g: Term): SolveInfo = {
    //System.out.println("ENGINE SOLVE #0: "+g);
    if (g == null) {
      null
    } else {
      val sinfo: SolveInfo = engManager.solve(g)
      val ev: QueryEvent = new QueryEvent(this, sinfo)
      notifyNewQueryResultAvailable(ev)
      sinfo
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
  override def solveNext: SolveInfo = {
    if (hasOpenAlternatives) {
      val sinfo: SolveInfo = engManager.solveNext
      val ev: QueryEvent = new QueryEvent(this, sinfo)
      notifyNewQueryResultAvailable(ev)
      sinfo
    }
    else throw new NoMoreSolutionException
  }

  /**
    * Halts current solve computation
    */
  override def solveHalt() {
    engManager.solveHalt()
  }

  /**
    * Accepts current solution
    */
  override def solveEnd() {
    engManager.solveEnd()
  }

  /**
    * Asks for the presence of open alternatives to be explored
    * in current demostration process.
    *
    * @return true if open alternatives are present
    */
  override def hasOpenAlternatives: Boolean = engManager.hasOpenAlternatives

  override def toJSON(alsoKB: Boolean): String = {
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