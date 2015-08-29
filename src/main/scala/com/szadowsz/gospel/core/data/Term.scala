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
package com.szadowsz.gospel.core.data

import java.io.Serializable
import java.{util => ju}

import com.szadowsz.gospel.core.engine.EngineManager
import com.szadowsz.gospel.core.exception.interpreter.InvalidTermException
import com.szadowsz.gospel.core.{OperatorManager, Parser, Prolog}
import com.szadowsz.gospel.util.OneWayList

/**
 * Term class is the root abstract class for prolog data type
 * @see Struct
 * @see Var
 * @see  Number
 */
@SerialVersionUID(1L)
object Term {
  // true and false constants
  val TRUE: Term = new Struct("true")
  val FALSE: Term = new Struct("false")

  /**
   * Static service to create a Term from a string.
   * @param st the string representation of the term
   * @return the term represented by the string
   * @throws InvalidTermException if the string does not represent a valid term
   */
  @throws(classOf[InvalidTermException])
  def createTerm(st: String): Term = Parser.parseSingleTerm(st)

  /**
   * Static service to create a Term from a string, providing an
   * external operator manager.
   * @param st the string representation of the term
   * @param op the operator manager used to build the term
   * @return the term represented by the string
   * @throws InvalidTermException if the string does not represent a valid term
   */
  @throws(classOf[InvalidTermException])
  def createTerm(st: String, op: OperatorManager): Term = Parser.parseSingleTerm(st, op)

  /**
   * Gets an iterator providing
   * a term stream from a source text
   */
  def getIterator(text: String): Iterator[Term] = new Parser(text).iterator
}

@SerialVersionUID(1L)
abstract class Term extends Serializable {
  /** is this term a null term? */
  def isEmptyList: Boolean

  /** is this term a constant prolog term? */
  def isAtomic: Boolean

  /** is this term a prolog compound term? */
  def isCompound: Boolean

  /** is this term a prolog (alphanumeric) atom? */
  def isAtom: Boolean

  /** is this term a prolog list? */
  def isList: Boolean

  /** is this term a ground term? */
  def isGround: Boolean

  /**
   * Tests for the equality of two object terms
   *
   * The comparison follows the same semantic of
   * the isEqual method.
   *
   */
  final override def equals(t: Any): Boolean = if (!t.isInstanceOf[Term]) false else isEqual(t.asInstanceOf[Term])

  /**
   * is term greater than term t?
   */
  def isGreater(t: Term): Boolean

  def isGreaterRelink(t: Term, vorder: ju.ArrayList[String]): Boolean

  /**
   * Tests if this term is (logically) equal to another
   */
  def isEqual(t: Term): Boolean

  /**
   * Gets the actual term referred by this Term. if the Term is a bound variable, the method gets the Term linked to the variable
   */
  def getTerm: Term

  /**
   * Unlink variables inside the term
   */
  def free()

  /**
   * Resolves variables inside the term, starting from a specific time count.
   *
   * If the variables has been already resolved, no renaming is done.
   * @param count new starting time count for resolving process
   * @return the new time count, after resolving process
   */
  private[gospel] def resolveTerm(count: scala.Long): scala.Long

  /**
   * Resolves variables inside the term
   *
   * If the variables has been already resolved, no renaming is done.
   */
  final def resolveTerm() {
    resolveTerm(System.currentTimeMillis)
  }

  /**
   * gets a copy of this term for the output
   */
  final def copyResult(goalVars: ju.Collection[Var], resultVars: ju.List[Var]): Term = {
    val originals = new ju.IdentityHashMap[Var, Var]
    import scala.collection.JavaConversions._ //TODO
    for (key <- goalVars) {
      var clone: Var = new Var
      if (!key.isAnonymous) clone = new Var(key.getOriginalName)
      originals.put(key, clone)
      resultVars.add(clone)
    }
    copy(originals, new ju.IdentityHashMap[Term, Var])
  }

  /**
   * gets a copy (with renamed variables) of the term.
   *
   * The list argument passed contains the list of variables to be renamed
   * (if empty list then no renaming)
   * @param idExecCtx Execution Context identifier
   */
  private[gospel] def copy(vMap: ju.AbstractMap[Var, Var], idExecCtx: Int): Term

  /**
   * gets a copy for result.
   */
  private[gospel] def copy(vMap: ju.AbstractMap[Var, Var], substMap: ju.AbstractMap[Term, Var]): Term

  /**
   * Try to unify two terms
   * @param mediator have the reference of EngineManager
   * @param t1 the term to unify
   * @return true if the term is unifiable with this one
   */
  final def unify(mediator: Prolog, t1: Term): Boolean = {
    val engine: EngineManager = mediator.getEngineManager
    resolveTerm()
    t1.resolveTerm()
    val v1: ju.List[Var] = new ju.LinkedList[Var]
    val v2: ju.List[Var] = new ju.LinkedList[Var]
    val ok: Boolean = unify(v1, v2, t1)
    if (ok) {
      val ec = engine.getCurrentContext
      if (ec != null) {
        var id: Int = if (engine.getEnv == null) Var.PROGRESSIVE else engine.getEnv.nDemoSteps
        // Update trailingVars
        ec.trailingVars = new OneWayList[ju.List[Var]](v1, ec.trailingVars)
        // Renaming after unify because its utility regards not the engine but the user
        var count: Int = 0
        import scala.collection.JavaConversions._
        for (v <- v1) {
          v.rename(id, count)
          if (id >= 0) {
            id += 1
          }
          else {
            count += 1
          }
        }
        for (v <- v2) {
          v.rename(id, count)
          if (id >= 0) {
            id += 1
          }
          else {
            count += 1
          }
        }
      }
      true
    }  else {
      Var.free(v1)
      Var.free(v2)
      false
    }
  }

  /**
   * Tests if this term is unifiable with an other term.
   * No unification is done.
   *
   * The test is done outside any demonstration context
   * @param t the term to checked
   *
   * @return true if the term is unifiable with this one
   */
  def matches(t: Term): Boolean = {
    resolveTerm()
    t.resolveTerm()
    val v1: ju.List[Var] = new ju.LinkedList[Var]
    val v2: ju.List[Var] = new ju.LinkedList[Var]
    val ok: Boolean = unify(v1, v2, t)
    Var.free(v1)
    Var.free(v2)
    ok
  }

  /**
   * Tries to unify two terms, given a demonstration context
   * identified by the mark integer.
   *
   * Try the unification among the term and the term specified
   * @param varsUnifiedArg1 Vars unified in myself
   * @param varsUnifiedArg2 Vars unified in term t
   */
  private[gospel] def unify(varsUnifiedArg1: ju.List[Var], varsUnifiedArg2: ju.List[Var], t: Term): Boolean

  /**
   * Gets the string representation of this term
   * as an X argument of an operator, considering the associative property.
   */
  private[gospel] def toStringAsArgX(op: OperatorManager, prio: Int): String = {
    toStringAsArg(op, prio, true)
  }

  /**
   * Gets the string representation of this term
   * as an Y argument of an operator, considering the associative property.
   */
  private[gospel] def toStringAsArgY(op: OperatorManager, prio: Int): String = {
    toStringAsArg(op, prio, false)
  }

  /**
   * Gets the string representation of this term
   * as an argument of an operator, considering the associative property.
   *
   * If the boolean argument is true, then the term must be considered
   * as X arg, otherwise as Y arg (referring to prolog associative rules)
   */
  private[gospel] def toStringAsArg(op: OperatorManager, prio: Int, x: Boolean): String = {
    toString
  }

  /**
   * The iterated-goal term G of a term T is a term defined
   * recursively as follows:
   * <ul>
   * <li>if T unifies with &#94;(_, Goal) then G is the iterated-goal
   * term of Goal</li>
   * <li>else G is T</li>
   * </ul>
   */
  def iteratedGoalTerm: Term = {
    this
  }
}