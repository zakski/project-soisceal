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
import com.szadowsz.gospel.core.data.{Struct, Term, Var}
import com.szadowsz.gospel.core.db.theory.clause.{ClauseDatabase, FamilyClausesList}
import com.szadowsz.gospel.core.engine.clause.ClauseInfo
import com.szadowsz.gospel.core.engine.context.ExecutionContext
import com.szadowsz.gospel.core.engine.subgoal.tree.SubGoalLeaf
import com.szadowsz.gospel.core.parser.Parser
import com.szadowsz.gospel.util.exception.data.TermParsingException
import com.szadowsz.gospel.util.exception.theory.InvalidTheoryException
import com.szadowsz.gospel.util.{LoggerCategory, Tools}
import org.slf4j.LoggerFactory

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

  private val _wam: Prolog = vm
  private lazy val _primitiveManager = _wam.getPrimitiveManager

  private var _dynamicDBase: ClauseDatabase = new ClauseDatabase
  private val _staticDBase: ClauseDatabase = new ClauseDatabase
  private var _retractDBase: ClauseDatabase = new ClauseDatabase


  private var startGoalStack: ju.Stack[Term] = null
  private[theory] var lastConsultedTheory: Theory = new Theory()

  /**
   * Method to check if a Strutc is a directive and if so, run it.
   *
   * @param directive Struct that contains the directive to be run
   * @return true if it is a directive, false otherwise
   */
  private def runDirective(directive: Struct): Boolean = {
    val hasDirName = "':-'" == directive.getName || ":-" == directive.getName
    if (hasDirName && directive.getArity == 1 && directive.getTerm(0).isInstanceOf[Struct]) {
      val dir: Struct = directive.getTerm(0).asInstanceOf[Struct]
      try {
        if (!_primitiveManager.evalAsDirective(dir))
          _logger.warn("The directive {} is unknown.", dir.getPredicateIndicator)
      } catch {
        case thrown: Throwable => {
          _logger.error("Exception thrown during execution of " + dir.getPredicateIndicator + " directive.", thrown)
        }
      }
      true
    } else {
      false
    }
  }

  /**
   * Method inserts of a clause at the head of the theory.
   *
   * @param clause the Struct to insert.
   * @param isDynamic True if being done on the fly, false if it comes from a library.
   * @param libName The name of the library the clause comes from, if any.
   */
  def assertA(clause: Struct, isDynamic: Boolean, libName: String): Unit = {
    synchronized {
      val d: ClauseInfo = new ClauseInfo(toClause(clause), libName)

      val key: String = d.getHead.getPredicateIndicator
      if (isDynamic) {
        _dynamicDBase.addFirst(key, d)
        if (_staticDBase.containsKey(key)) {
          _logger.warn("A static predicate with signature {} has been overridden.", key)
        }
      } else _staticDBase.addFirst(key, d)
      _logger.info("INSERTA: {}", d.getClause)
    }
  }

  /**
   * Method inserts of a clause at the end of the DB.
   *
   * @param clause the Struct to insert.
   * @param isDynamic True if being done on the fly, false if it comes from a library.
   * @param libName The name of the library the clause comes from, if any.
   */
  def assertZ(clause: Struct, isDynamic: Boolean, libName: String): Unit = {
    synchronized {
      val d: ClauseInfo = new ClauseInfo(toClause(clause), libName)

      val key: String = d.getHead.getPredicateIndicator
      if (isDynamic) {
        _dynamicDBase.addLast(key, d)
        if (_staticDBase.containsKey(key)) {
          _logger.warn("A static predicate with signature {} has been overridden.", key)
        }
      }
      else _staticDBase.addLast(key, d)
      _logger.info("INSERTZ: {}", d.getClause)
    }
  }

  /**
   * Consults a theory, adding it to the base of known terms.
   *
   * @param theory the theory to add
   * @param isDynamic if it is true, then the clauses are marked as dynamic
   * @param libName if it not null, then the clauses are marked to belong to the specified library
   */
  @throws(classOf[InvalidTheoryException])
  def consult(theory: Theory, isDynamic: Boolean, libName: String): Unit = {
    synchronized {
      startGoalStack = new ju.Stack[Term]
      var clause: scala.Int = 1

      // iterate all clauses in theory and assert them or run them in the case of directives.
      try {
        val it: Iterator[_ <: Term] = theory.iterator(_wam)
        while (it.hasNext) {
          clause += 1
          val d: Struct = it.next().asInstanceOf[Struct]

          if (!runDirective(d)) {
            assertZ(d, isDynamic, libName)
          }
        }
      } catch {
        case e: TermParsingException => throw new InvalidTheoryException(e.getMessage, clause, e.getLine, e.getColumn)
      }
      if (libName == null)
        lastConsultedTheory = theory
    }
  }

  /**
   * Method to remove the first clause that unifies with the provided clause from the DB.
   *
   * @param oldClause the clause struct to remove.
   * @return the removed Clause
   */
  def retract(oldClause: Struct): ClauseInfo = {
    synchronized {
      val clause: Struct = toClause(oldClause)
      val struct: Struct = clause.getArg(0).asInstanceOf[Struct]
      val family: FamilyClausesList = _dynamicDBase.get(struct.getPredicateIndicator)
      val ctx : ExecutionContext = _wam.getEngineManager.getCurrentContext

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
          _logger.info("DELETED: {}",d.getClause)
          return new ClauseInfo(d.getClause, null)
        }
      }
      return null
    }
  }

  /**
   * Binds clauses in the database with the corresponding primitive predicate, if any.
   */
  def rebind(): Unit = {
    for (d <- _dynamicDBase.iterator.asScala) {
      for (sge <- d.getBody.iterator) {
        val t: Term = sge.asInstanceOf[SubGoalLeaf].getValue
        _primitiveManager.identifyPredicate(t)
      }
    }
  }

  /**
   * Clears the dynamic clause database.
   */
  def clear(): Unit = {
    synchronized {
      _dynamicDBase = new ClauseDatabase
    }
  }


  /**
   * Method to remove all the clauses corresponding to the predicate indicator passed as a parameter from the DB.
   *
   * @param indicator struct with "/" as name, arg0 "name" and arg1 "arity".
   * @throws java.lang.IllegalArgumentException thrown if predicate indicator is invalid.
   * @return true if it was successful
   */
  @throws(classOf[IllegalArgumentException])
  def abolish(indicator: Struct): Boolean = {
    synchronized {
      if (!indicator.isInstanceOf[Struct] || !indicator.isGround || !(indicator.getArity == 2)) {
        throw new IllegalArgumentException(indicator + " is not a valid Struct")

      } else if (!(indicator.getName == "/")) {
        throw new IllegalArgumentException(indicator + " has not the valid predicate name. Expected '/' but was " + indicator.getName)
      }

      val arg0: String = Tools.removeApices(indicator.getArg(0).toString)
      val arg1: String = Tools.removeApices(indicator.getArg(1).toString)
      val key: String = arg0 + "/" + arg1

      val abolished = _dynamicDBase.abolish(key)
      if (abolished != null) {
        _logger.info("ABOLISHED: {} number of clauses={}", key, abolished.size)
      }
      true
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
   * Method to remove all the clauses of library theory.
   *
   * @param libName the library to remove.
   */
  def removeLibraryTheory(libName: String):Unit = {
    synchronized {
      val allClauses: ju.Iterator[ClauseInfo] = _staticDBase.iterator
      while (allClauses.hasNext) {
        val clause = allClauses.next
        if (clause.getLib != null && (libName == clause.getLib)) {
          try {
            allClauses.remove()
          }
          catch {
            case e: Exception => _logger.error("Error during library theory removal",e)
            }
        }
      }
    }
  }

  /**
   * Gets a clause from a generic Term
   *
   * @param clauseContainer the term the clause is contained in.
   * @return the identified clause.
   */
  private def toClause(clauseContainer: Struct): Struct = {
    var term = Parser.parseSingleTerm(clauseContainer.toString(), _wam.getOperatorManager).asInstanceOf[Struct]
    if (!term.isClause) {
      term = new Struct(":-", term, new Struct("true"))
    }
    _primitiveManager.identifyPredicate(term)
    term
  }

  /**
   * Method solves and goals that have been added during the theory initialisation process
   */
  def solveTheoryGoal():Unit= {
    synchronized {
      var s: Struct = null
      while (!startGoalStack.empty) {
        s = if (s == null) startGoalStack.pop.asInstanceOf[Struct] else new Struct(",", startGoalStack.pop.asInstanceOf[Struct], s)
      }
      if (s != null) {
        try {
          _wam.solve(s)
        }
        catch {
          case ex: Exception => {
            _logger.error("Exception thrown when solving " + s.toString()  + " during Theory initialisation",ex)
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
        buffer.append(d.toString(_wam.getOperatorManager)).append("\n")
      }

      if (!onlyDynamic) {
        val staticClauses: ju.Iterator[ClauseInfo] = _staticDBase.iterator
        while (staticClauses.hasNext) {
          val d: ClauseInfo = staticClauses.next
          buffer.append(d.toString(_wam.getOperatorManager)).append("\n")
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