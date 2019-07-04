/**
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3.0 of the License, or (at your option) any later version.
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

import com.szadowsz.gospel.core.data.Term
import com.szadowsz.gospel.core.db.libraries.{Library, LibraryManager}
import com.szadowsz.gospel.core.db.operators.OperatorManager
import com.szadowsz.gospel.core.db.primitives.PrimitivesManager
import com.szadowsz.gospel.core.db.theory.TheoryManager
import com.szadowsz.gospel.core.engine.flags.FlagManager
import com.szadowsz.gospel.core.exception.library.InvalidLibraryException
import com.szadowsz.gospel.core.parser.Parser
import org.slf4j.{Logger, LoggerFactory}

class Interpreter {
  
  protected val logger : Logger = LoggerFactory.getLogger(classOf[Interpreter])

  protected implicit lazy val opManager : OperatorManager = new OperatorManager
  
  protected lazy val flagManager = new FlagManager()
  
  protected lazy val primManager : PrimitivesManager = new PrimitivesManager(this)

  protected lazy val libManager : LibraryManager = new LibraryManager(this)

  protected lazy val thManager : TheoryManager = new TheoryManager(this)
  
  def this(libs : Class[_ <: Library]*){
    this()
    libs.foreach(l => libManager.loadLibraryFromClass(l))
  }

  private[core] def getPrimitiveManager : PrimitivesManager = primManager
 
  private[core] def getFlagManager : FlagManager = flagManager

  private[core] def getTheoryManager : TheoryManager = thManager

  private[core] def getLibraryManager : LibraryManager = libManager

  private[core] def getOperatorManager : OperatorManager = opManager
  
  /**
    * Loads a library.
    *
    * If a library with the same name is already present, a warning event is notified and the request is ignored.
    *
    * @param identifier the name / matching pattern of the library to be loaded.
    * @throws InvalidLibraryException if we cannot create a valid library.
    * @return the reference to the Library just loaded.
    */
  @throws(classOf[InvalidLibraryException])
  def loadLibrary(identifier: String): Library = {
    libManager.loadLibrary(identifier)
  }
  
  private[core] def createTerm(st: String): Term = new Parser(st).nextTerm(false)
  
  //  /**
//    * Solves a query
//    *
//    * @param st the string representing the goal to be demonstrated
//    * @return the result of the demonstration
//    * @see SolveInfo
//    **/
//  @throws[InvalidTermException]
//  def solve(st: String): Solution = {
//    try {
//      val p = new Parser(st)
//      val t = p.nextTerm(true)
//      solve(t)
//    } catch {
//      case ex: InvalidTermException => // TODO don't throw anything back from here
//        throw new InvalidTermException("Demonstration Goal is Malformed",ex, ex.getTerm, ex.getLine, ex.getCol) 
//    }
//  }
  
//  /**
//    * Solves a query
//    *
//    * @param goal the term representing the goal to be demonstrated
//    * @return the result of the demonstration
//    * @see SolveInfo
//    **/
//  def solve(goal: Term): Solution = {
//    Option(goal) match {
//      case Some(_) => 
//        val sinfo: Solution = engManager.solve(goal)
//        val ev: QueryEvent = new QueryEvent(this, sinfo)
//        notifyNewQueryResultAvailable(ev)
//        sinfo
//      case None => null // TODO remove null
//    }
//  }
//  
//   /**
//    * Gets next solution
//    *
//    * @return the result of the demonstration
//    * @see Solution
//    **/
//  def solveNext(): Option[Solution] = {
//    if (hasOpenAlternatives) {
//      val sinfo: Solution = engManager.solveNext
//      val ev: QueryEvent = new QueryEvent(this, sinfo)
//      notifyNewQueryResultAvailable(ev)
//      Option(sinfo)
//    } else {
//      None
//    }
//  }
//  
//  /**
//    * Halts current solve computation
//    */
//  def solveHalt() {
//    engManager.solveHalt()
//  }
}
