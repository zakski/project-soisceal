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
package com.szadowsz.gospel.core.engine.context

import java.util

import com.szadowsz.gospel.core.data.{Term, Var}
import com.szadowsz.gospel.core.db.theory.clause.Clause

import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.collection.JavaConverters._
import scala.collection.mutable


/**
  * A list of clauses belonging to the same family as a goal. A family is composed by clauses with the same functor and arity.
  */
class ClauseStore(goalTerm: Term, varList: util.List[Var]) {
  private val goal: Term = goalTerm
  private val vars: util.List[Var] = varList
  private var clauses: mutable.Buffer[Clause] = ArrayBuffer()
  private var haveAlternatives: Boolean = false
  
  def this(goal: Term, vars: util.List[Var], familyClauses: List[Clause]) = {
    this(goal, vars)
    clauses = familyClauses.toBuffer
  }
  
  /**
    * Returns the clause to load
    */
  def fetch: Clause = {
    if (clauses.isEmpty){
      null
    } else {
      deunify(vars)
      
      if (checkCompatibility(goal)) {
        val clause: Clause = clauses.head
        clauses = clauses.tail
        haveAlternatives = checkCompatibility(goal)
        clause
      } else {
        null
      }
    }
  }

  /**
    * Save the unifications of the variables to deunify
    *
    * @param varsToDeunify
    * @return Unification of variables
    */
  private def deunify(varsToDeunify: util.List[Var]): util.List[Term] = {
    val saveUnifications: util.List[Term] = new util.ArrayList[Term]
    val it: util.Iterator[Var] = varsToDeunify.iterator
    while (it.hasNext) {
      val v: Var = it.next
      saveUnifications.add(v.getDirectBinding)
      v.freeVars()
    }
    saveUnifications
  }

  /**
    * Verify if a clause exists that is compatible with goal.
    * As a side effect, clauses that are not compatible get
    * discarded from the currently examined family.
    *
    * @param goal
    */
  private def checkCompatibility(goal: Term): Boolean = {
    if (clauses.isEmpty){
      false
    } else {
      var result = false
      do {
        val clause = clauses.head
        if (goal.isUnifiable(clause.head)) {
          result = true
        } else {
          clauses = clauses.tail
        }
      } while (!result && clauses.nonEmpty)
      false
    }
  }

  def hasAlternatives(): Boolean = haveAlternatives

  override def toString: String = {
    "clauses: " + clauses + "\n" + "goal: " + goal + "\n" + "vars: " + vars + "\n"
  }

  def getClauses: util.List[Clause] = {
    val l: util.ArrayList[Clause] = new util.ArrayList[Clause]
    clauses.foreach(l.add)
    l
  }

  def getMatchGoal: Term = goal

  def getVarsForMatch: util.List[Var] = vars

  /**
    * Verify if there is a term in compatibleGoals compatible with goal.
    *
    * @return true if compatible or false otherwise.
    */
  protected[core] def existCompatibleClause: Boolean = {
    val saveUnifications: util.List[Term] = deunify(vars)
    val found: Boolean = checkCompatibility(goal)
    reunify(vars, saveUnifications)
    found
  }

  /**
    * Restore previous unified terms links in variables.
    *
    * @param varsToReunify
    * @param saveUnifications
    */
  private def reunify(varsToReunify: util.List[Var], saveUnifications: util.List[Term]) {
    val size: Int = varsToReunify.size
    val it1 = varsToReunify.listIterator(size)
    val it2 = saveUnifications.listIterator(size)
    // Only the first occurrence of a variable gets its binding saved;
    // following occurrences get a null instead. So, to avoid clashes
    // between those values, and avoid random variable deunification,
    // the reunification is made starting from the end of the list.
    while (it1.hasPrevious) {
      it1.previous.setBinding(it2.previous)
    }
  }
}