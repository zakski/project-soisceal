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
package com.szadowsz.gospel.core.db.theory

import java.util

import com.szadowsz.gospel.core.Interpreter
import com.szadowsz.gospel.core.data.{Struct, Term, Var}
import com.szadowsz.gospel.core.db.libraries.LibraryPredicateFilter
import com.szadowsz.gospel.core.db.operators.OperatorManager
import com.szadowsz.gospel.core.db.primitives.PrimitivesManager
import com.szadowsz.gospel.core.db.theory.clause.{Clause, ClauseDB}
import com.szadowsz.gospel.core.exception.{InvalidTermException, InvalidTheoryException}
import org.slf4j.{Logger, LoggerFactory}

import scala.util.control.NonFatal

/**
  * This class defines the Theory Manager who manages the clauses/theory often referred to as the Prolog database.
  *
  * The theory (as a set of clauses) are stored in the ClauseDatabase which in essence is a HashMap grouped by
  * functor/arity.
  *
  * The TheoryManager functions logically, as prescribed by ISO Standard 7.5.4 section. The effects of assertions and
  * retractions shall not be undone if the program subsequently backtracks over the assert or retract call, as
  * prescribed by ISO Standard 7.7.9 section.
  *
  * To use the TheoryManager one should primarily use the methods assertA, assertZ, consult, retract, abolish and find.
  *
  * Created on 19/02/2017.
  *
  */
private[core] class TheoryManager(wam: Interpreter) {

  private val logger: Logger = LoggerFactory.getLogger(classOf[TheoryManager])

  private lazy implicit val opManager: OperatorManager = wam.getOperatorManager

  private lazy implicit val primManager: PrimitivesManager = wam.getPrimitiveManager

  private val staticDB : ClauseDB = new ClauseDB()
  private val dynamicDB : ClauseDB = new ClauseDB()

  private var lastConsultedTheory: Theory = _

  private var startGoalStack: util.Stack[Term] = _

  private[db] def staticDBSize : Long = staticDB.toList.length
  
  private[db] def dynamicDBSize : Long = dynamicDB.toList.length
  
  /**
    * Gets a clause from a generic Term
    *
    * @param nclause the term the clause is contained in.
    * @return the identified clause.
    */
  private def toClause(nclause: Struct): Struct = {
    var term = nclause
    if (!term.isClause) {
      term = new Struct(":-", term, new Struct("true"))
    }
    primManager.identifyPredicate(term)
    term
  }

  /**
    * Method to check if a Struct is a directive and if so, run it.
    *
    * @param directive Struct that contains the directive to be run
    * @return true if it is a directive, false otherwise
    */
  private def runDirective(directive: Struct): Boolean = {
    val hasDirName = "':-'" == directive.getName || ":-" == directive.getName
    if (hasDirName && directive.getArity == 1 && directive.getTerm(0).isInstanceOf[Struct]) {
      val dir: Struct = directive.getTerm(0).asInstanceOf[Struct]
      try {
        if (!primManager.evalAsDirective(dir)) {
          logger.warn("The directive {} is unknown.", dir.getPredicateIndicator)
        }
      } catch {
        case NonFatal(thrown) =>
          logger.warn(s"Exception thrown during execution of ${dir.getPredicateIndicator} directive.", thrown)
      }
      true
    } else {
      false
    }
  }

  def unloadLibrary(name: String): Unit =   {
    synchronized {
     staticDB.foreach { clause =>
       clause.libName match {
         case Some(libName) if libName == name =>
           try {
             staticDB.remove(clause)
           } catch {
             case NonFatal(e) => logger.error ("Error during library theory removal", e)
           }
         case _ =>
       }
     }
    }
  }

  def rebindPrimitives(): Unit = {//TODO
  }

  /**
    * Method solves any goals that have been added during the theory initialisation process.
    */
  def validateStack(): Unit = {} // TODO

  /**
    * Returns a family of clauses with functor and arity equals
    * to the functor and arity of the term passed as a parameter
    *
    * Reviewed by Paolo Contessi: modified according to new ClauseDatabase
    * implementation
    */
  def find(headT: Term): List[Clause] = {
    synchronized {
      headT match {
        case headS: Struct =>
          var list: List[Clause] = dynamicDB (headS)
          if (list.isEmpty) {
            list = staticDB(headS)
          }
          list
        case _ => throw new RuntimeException
      }
    }
  }

  /**
    * Consults a theory, adding it to the base of known terms.
    *
    * @param theory    the theory to add
    * @param isDynamic if it is true, then the clauses are marked as dynamic
    * @param libName   if it not null, then the clauses are marked to belong to the specified library
    */
  def consult(theory: Theory,
              isDynamic: Boolean = true,
              filter : LibraryPredicateFilter = new LibraryPredicateFilter(new Struct()),
              libName: Option[String] = None
             ): Unit = {
    
    synchronized {
      startGoalStack = new util.Stack[Term]
      var clause: scala.Int = 0

      // iterate all clauses in theory and assert them or run them in the case of directives.
      try {
        // TODO implement Module support here
        theory.iterator().foreach { t =>
          clause += 1
          val d: Struct = t.asInstanceOf[Struct]
          if (!runDirective(d)) {
            if (filter.retainPredicate(d.getPredicateIndicator)) {
              assertZ(filter.mapStruct(d), isDynamic, libName, true)
            }
          }
        }
      } catch {
        case ite: InvalidTermException => throw new InvalidTheoryException(ite, clause)
        case NonFatal(t) => throw new InvalidTheoryException("Unable To Consult Theory", t, clause)
      }
      if (libName == None) {
        lastConsultedTheory = theory
      }
    }
  }

  /**
    * Method inserts of a clause at the end of the DB.
    *
    * @param clause    the Struct to insert.
    * @param isDynamic True if being done on the fly, false if it comes from a library.
    * @param libName   The name of the library the clause comes from, if any.
    */
  def assertZ(clause: Struct, isDynamic: Boolean, libName: Option[String], backtrackable: Boolean): Unit = {
    synchronized {
      val d: Clause = new Clause(toClause(clause), libName)

      val key: String = d.head.getPredicateIndicator
      if (isDynamic) {
        dynamicDB :+= (key, d)
        if (staticDB.contains(key)) {
          logger.warn("A static predicate with signature {} has been overridden.", key)
        }
      } else {
      staticDB :+= (key -> d)
      }
      logger.info("INSERTZ: {}", d.clause)
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

      val key: String = indicator(0) + "/" + indicator(1)

      val abolished = dynamicDB.abolish(key)
      if (abolished != null) {
        logger.info("ABOLISHED: {} number of clauses={}", key, abolished.size)
      }
      true
    }
  }
  
  /**
    * Checks the Theory DBs to see if we have any knowledge of a given predicate
    *
    * @param predicateIndicator the predicate identifier
    * @return true if found, false otherwise
    */
  def checkExistence(predicateIndicator: String): Boolean = {
    dynamicDB.contains(predicateIndicator) || staticDB.contains(predicateIndicator)
  }
  
  /**
    * Clears the dynamic clause database.
    */
  def clear(): Unit = {
    synchronized {
      dynamicDB.clear()
    }
  }
}
