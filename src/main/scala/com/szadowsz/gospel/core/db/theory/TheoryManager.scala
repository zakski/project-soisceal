/*
 * tuProlog - Copyright (C) 2001-2007  aliCE team at deis.unibo.it
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
package com.szadowsz.gospel.core.db.theory

import com.szadowsz.gospel.core.Prolog
import com.szadowsz.gospel.core.engine.clause.ClauseInfo
import com.szadowsz.gospel.core.data.{Struct, Term, Var}
import com.szadowsz.gospel.core.db.theory.clause.{ClauseDatabase, FamilyClausesList}
import com.szadowsz.gospel.core.engine.context.ExecutionContext
import com.szadowsz.gospel.core.engine.subgoal.tree.SubGoalLeaf
import com.szadowsz.gospel.util.exception.data.TermParsingException
import com.szadowsz.gospel.util.exception.theory.InvalidTheoryException
import com.szadowsz.gospel.util.{LoggerCategory, Tools}
import org.slf4j.LoggerFactory

import java.io.{DataOutputStream, IOException, OutputStream}
import java.{util => ju}
import scala.collection.JavaConverters._

/**
 * This class defines the Theory Manager who manages the clauses/theory often referred to as the Prolog database.
 * The theory (as a set of clauses) are stored in the ClauseDatabase which in essence is a HashMap grouped by functor/arity.
 * <p/>
 * The TheoryManager functions logically, as prescribed by ISO Standard 7.5.4
 * section. The effects of assertions and retractions shall not be undone if the
 * program subsequently backtracks over the assert or retract call, as prescribed
 * by ISO Standard 7.7.9 section.
 * <p/>
 * To use the TheoryManager one should primarily use the methods assertA, assertZ, consult, retract, abolish and find.
 * <p/>
 *
 * rewritten by:
 * @author ivar.orstavik@hist.no
 *
 * @see alice.gospel.core.theory.Theory
 */
@SerialVersionUID(1L)
class TheoryManager(vm: Prolog) extends Serializable {
  private lazy val _logger = LoggerFactory.getLogger(LoggerCategory.DB)

  private val _engine: Prolog = vm
  private lazy val _primitiveManager = _engine.getPrimitiveManager

  private var _dynamicDBase: ClauseDatabase= new ClauseDatabase
  private val _staticDBase: ClauseDatabase = new ClauseDatabase
  private var _retractDBase: ClauseDatabase= new ClauseDatabase


  private var startGoalStack: ju.Stack[Term] = null
  private[theory] var lastConsultedTheory: Theory = new Theory()

  /**
   * inserting of a clause at the head of the dbase
   */
  def assertA(clause: Struct, dyn: Boolean, libName: String, backtrackable: Boolean) {
    synchronized {
      val d: ClauseInfo = new ClauseInfo(toClause(clause), libName)

      val key: String = d.getHead.getPredicateIndicator
      if (dyn) {
        _dynamicDBase.addFirst(key, d)
        if (_staticDBase.containsKey(key)) {
          _logger.warn("A static predicate with signature TODO has been overriden.",key)
        }
      }
      else _staticDBase.addFirst(key, d)
      _engine.spy("INSERTA: " + d.getClause + "\n")
    }
  }

  /**
   * inserting of a clause at the end of the dbase
   */
  def assertZ(clause: Struct, dyn: Boolean, libName: String, backtrackable: Boolean) {
    synchronized {
      val d: ClauseInfo = new ClauseInfo(toClause(clause), libName)
      val key: String = d.getHead.getPredicateIndicator
      if (dyn) {
        _dynamicDBase.addLast(key, d)
        if (_staticDBase.containsKey(key)) {
          _logger.warn("A static predicate with signature TODO has been overriden.",key)
        }
      }
      else _staticDBase.addLast(key, d)
      _engine.spy("INSERTZ: " + d.getClause + "\n")
    }
  }

  /**
   * removing from dbase the first clause with head unifying with clause
   */
  def retract(cl: Struct): ClauseInfo = {
    synchronized {
      val clause: Struct = toClause(cl)
      val struct: Struct = clause.getArg(0).asInstanceOf[Struct]
      val family: FamilyClausesList = _dynamicDBase.get(struct.getPredicateIndicator)
      val ctx : ExecutionContext = _engine.getEngineManager.getCurrentContext

      /* create a new clause list to store the theory upon retract
     * This is done only on the first loop of the same retract
     * (Which we recognise by the id of the context)
     * Will be 'the retract from this db to return the result
     */
      var familyQuery: FamilyClausesList = null
      if (!_retractDBase.containsKey("ctxId " + ctx.id)) {
        familyQuery = new FamilyClausesList
        for (i <- 0 until family.size) {
          familyQuery.add(family.get(i))
        }
        _retractDBase.put("ctxId " + ctx.id, familyQuery)
      }
      else {
        familyQuery = _retractDBase.get("ctxId " + ctx.id)
      }
      if (familyQuery == null)
        return null

      if (family != null) {
        // does the retract from the basic theory
        val it = family.iterator
        while (it.hasNext) {
          val d: ClauseInfo = it.next
          if (clause.matches(d.getClause)) {
            it.remove()
          }
        }
      }
      val i = familyQuery.iterator // does the retract from the db
      while (i.hasNext) {
        val d: ClauseInfo = i.next
        if (clause.matches(d.getClause)) {
          i.remove()
          _engine.spy("DELETE: " + d.getClause + "\n")
          return new ClauseInfo(d.getClause, null)
        }
      }

      return null
    }
  }

  /**
   * removing from dbase all the clauses corresponding to the
   * predicate indicator passed as a parameter
   */
  def abolish(pi: Struct): Boolean = {
    synchronized {
      if (!pi.isInstanceOf[Struct] || !pi.isGround || !(pi.getArity == 2))
        throw new IllegalArgumentException(pi + " is not a valid Struct")
      if (!(pi.getName == "/"))
        throw new IllegalArgumentException(pi + " has not the valid predicate name. Espected '/' but was " + pi.getName)

      val arg0: String = Tools.removeApices(pi.getArg(0).toString)
      val arg1: String = Tools.removeApices(pi.getArg(1).toString)
      val key: String = arg0 + "/" + arg1

      val abolished = _dynamicDBase.abolish(key) /* Reviewed by Paolo Contessi: LinkedList -> List */
      if (abolished != null)
        _engine.spy("ABOLISHED: " + key + " number of clauses=" + abolished.size + "\n")
      return true
    }
  }

  /**
   * Returns a family of clauses with functor and arity equals
   * to the functor and arity of the term passed as a parameter
   *
   * Reviewed by Paolo Contessi: modified according to new ClauseDatabase
   * implementation
   */
  def find(headt: Term): List[ClauseInfo] = {
    synchronized {
      if (headt.isInstanceOf[Struct]) {
        var list: List[ClauseInfo] = _dynamicDBase.getPredicates(headt)
        if (list.isEmpty) list = _staticDBase.getPredicates(headt)
        return list
      }
      if (headt.isInstanceOf[Var]) {
        throw new RuntimeException
      }
      List[ClauseInfo]()
    }
  }

  /**
   * Consults a theory.
   *
   * @param theory        theory to add
   * @param dynamicTheory if it is true, then the clauses are marked as dynamic
   * @param libName       if it not null, then the clauses are marked to belong to the specified library
   */
  @throws(classOf[InvalidTheoryException])
  def consult(theory: Theory, dynamicTheory: Boolean, libName: String) {
    synchronized {
      startGoalStack = new ju.Stack[Term]
      var clause: scala.Int = 1

      // iterate all clauses in theory and assert them
      try {
        val it: Iterator[_ <: Term] = theory.iterator(_engine)
        while (it.hasNext) {
          clause += 1
          val d: Struct = it.next.asInstanceOf[Struct]
          if (!runDirective(d)) assertZ(d, dynamicTheory, libName, true)
        }
      } catch {
        case e: TermParsingException => {
          throw new InvalidTheoryException(e.getMessage, clause, e.getLine, e.getColumn)
        }
      }
      if (libName == null)
        lastConsultedTheory = theory
    }
  }

  /**
   * Binds clauses in the database with the corresponding
   * primitive predicate, if any
   */
  def rebindPrimitives {
    for (d <- _dynamicDBase.iterator.asScala) {
      for (sge <- d.getBody.iterator) {
        val t: Term = (sge.asInstanceOf[SubGoalLeaf]).getValue
        _primitiveManager.identifyPredicate(t)
      }
    }
  }

  /**
   * Clears the clause dbase.
   */
  def clear {
    synchronized {
      _dynamicDBase = new ClauseDatabase
    }
  }

  /**
   * remove all the clauses of lib theory
   */
  def removeLibraryTheory(libName: String) {
    synchronized {
      val allClauses: ju.Iterator[ClauseInfo] = _staticDBase.iterator
      while (allClauses.hasNext) {
        val d: ClauseInfo = allClauses.next
        if (d.getLib != null && (libName == d.getLib)) {
          try {
            allClauses.remove
          }
          catch {
            case e: Exception => {
            }
          }
        }
      }
    }
  }

  private def runDirective(c: Struct): Boolean = {
    if (("':-'" == c.getName) || (":-" == c.getName) && c.getArity == 1 && c.getTerm(0).isInstanceOf[Struct]) {
      val dir: Struct = c.getTerm(0).asInstanceOf[Struct]
      try {
        if (!_primitiveManager.evalAsDirective(dir)) _logger.warn("The directive TODO is unknown.", dir.getPredicateIndicator )
      }
      catch {
        case t: Throwable => {
          _logger.error("An exception has been thrown during the execution of the " + dir.getPredicateIndicator + " directive.",t)
        }
      }
      return true
    }
    return false
  }

  /**
   * Gets a clause from a generic Term
   */
  private def toClause(t: Struct): Struct = {
    var term  = Term.createTerm(t.toString, this._engine.getOperatorManager).asInstanceOf[Struct]
    if (!term.isClause)
      term= new Struct(":-", term, new Struct("true"))

    _primitiveManager.identifyPredicate(term)
    return term
  }

  def solveTheoryGoal {
    synchronized {
      var s: Struct = null
      while (!startGoalStack.empty) {
        s = if ((s == null)) startGoalStack.pop.asInstanceOf[Struct] else new Struct(",", startGoalStack.pop.asInstanceOf[Struct], s)
      }
      if (s != null) {
        try {
          _engine.solve(s)
        }
        catch {
          case ex: Exception => {
            ex.printStackTrace
          }
        }
      }
    }
  }

  /**
   * add a goal eventually defined by last parsed theory.
   */
  def addStartGoal(g: Struct) {
    synchronized {
      startGoalStack.push(g)
    }
  }

  /**
   * saves the dbase on a output stream.
   */
  private[theory] def save(os: OutputStream, onlyDynamic: Boolean): Boolean = {
    synchronized {
      try {
        new DataOutputStream(os).writeBytes(getTheory(onlyDynamic))
        return true
      }
      catch {
        case e: IOException => {
          return false
        }
      }
    }
  }

  /**
   * Gets current theory
   *
   * @param onlyDynamic if true, fetches only dynamic clauses
   */
  def getTheory(onlyDynamic: Boolean): String = {
    synchronized {
      val buffer = new StringBuffer
      val dynamicClauses = _dynamicDBase.iterator

      while (dynamicClauses.hasNext) {
        val d = dynamicClauses.next
        buffer.append(d.toString(_engine.getOperatorManager)).append("\n")
      }

      if (!onlyDynamic) {
        val staticClauses: ju.Iterator[ClauseInfo] = _staticDBase.iterator
        while (staticClauses.hasNext) {
          val d: ClauseInfo = staticClauses.next
          buffer.append(d.toString(_engine.getOperatorManager)).append("\n")
        }
      }
      return buffer.toString
    }
  }

  /**
   * Gets last consulted theory
   * @return  last theory
   */
  def getLastConsultedTheory: Theory = {
    synchronized {
      return lastConsultedTheory
    }
  }

  def clearRetractDB() {
    this._retractDBase = new ClauseDatabase
  }
}