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

import alice.util.OneWayList
import com.szadowsz.gospel.core.PrologEngine
import com.szadowsz.gospel.core.db.ops.OperatorManager
import java.util

import com.szadowsz.gospel.core.json.JSONSerializerManager

import scala.collection.JavaConverters._

/**
  * Term class is the root abstract class for prolog data type
  *
  * @see Struct
  * @see Var
  * @see Number
  */
@SerialVersionUID(1L)
object Term { // true and false constants
  val TRUE = new Struct("true")
  val FALSE = new Struct("false")

  //Alberto
  def fromJSON(jsonString: String): Term = if (jsonString.contains("Var")) JSONSerializerManager.fromJSON(jsonString, classOf[Var])
  else if (jsonString.contains("Struct")) JSONSerializerManager.fromJSON(jsonString, classOf[Struct])
  else if (jsonString.contains("Double")) JSONSerializerManager.fromJSON(jsonString, classOf[Double])
  else if (jsonString.contains("Int")) JSONSerializerManager.fromJSON(jsonString, classOf[Int])
  else if (jsonString.contains("Long")) JSONSerializerManager.fromJSON(jsonString, classOf[Long])
  else if (jsonString.contains("Float")) JSONSerializerManager.fromJSON(jsonString, classOf[Float])
  else null
}

@SerialVersionUID(1L)
abstract class Term extends Serializable {
  /**
    * is this term a null term?
    */
  def isEmptyList: Boolean

  /**
    * is this term a constant prolog term?
    */
  def isAtomic: Boolean

  /**
    * is this term a prolog compound term?
    */
  def isCompound: Boolean

  /**
    * is this term a prolog (alphanumeric) atom?
    */
  def isAtom: Boolean

  /**
    * is this term a prolog list?
    */
  def isList: Boolean

  /**
    * is this term a ground term?
    */
  def isGround: Boolean

  /**
    * Tests for the equality of two object terms
    * <p>
    * The comparison follows the same semantic of
    * the isEqual method.
    */
  override def equals(t: Any): Boolean = t.isInstanceOf[Term] && isEqual(t.asInstanceOf[Term])

  /**
    * is term greater than term t?
    */
  def isGreater(t: Term): Boolean

  /**
    * Tests if this term is (logically) equal to another
    */
  def isEqual(t: Term): Boolean = this.toString == t.toString

  /**
    * Tests if this term (as java object) is equal to another
    */
  def isEqualObject(t: Term): Boolean = t.isInstanceOf[Term] && (this eq t)

  /**
    * Gets the actual term referred by this Term. if the Term is a bound variable, the method gets the Term linked to the variable
    */
  def getTerm: Term

  /**
    * Unlink variables inside the term
    */
  def free(): Unit

  /**
    * Resolves variables inside the term, starting from a specific time count.
    * <p>
    * If the variables has been already resolved, no renaming is done.
    *
    * @param count new starting time count for resolving process
    * @return the new time count, after resolving process
    */
  private[data] def resolveTerm(count: scala.Long) : scala.Long

  /**
    * Resolves variables inside the term
    * <p>
    * If the variables has been already resolved, no renaming is done.
    */
  def resolveTerm(): Unit = resolveTerm(System.currentTimeMillis)

  /**
    * gets a engine's copy of this term.
    *
    * @param idExecCtx Execution Context identified
    */
  def copyGoal(vars: util.AbstractMap[Var, Var], idExecCtx: scala.Int): Term = copy(vars, idExecCtx)

  /**
    * gets a copy of this term for the output
    */
  def copyResult(goalVars: util.Collection[Var], resultVars: util.List[Var]): Term = {
    val originals = new util.IdentityHashMap[Var, Var]
    for (key <- goalVars.asScala) {
      var clone = new Var
      if (!key.isAnonymous) clone = new Var(key.getOriginalName)
      originals.put(key, clone)
      resultVars.add(clone)
    }
    copy(originals, new util.IdentityHashMap[Term, Var])
  }

  /**
    * gets a copy for result.
    */
  private[data] def copy(vMap: util.AbstractMap[Var, Var], substMap: util.AbstractMap[Term, Var]) : Term

  /**
    * gets a copy (with renamed variables) of the term.
    * <p>
    * The list argument passed contains the list of variables to be renamed
    * (if empty list then no renaming)
    *
    * @param idExecCtx Execution Context identifier
    */
  def copy(vMap: util.AbstractMap[Var, Var], idExecCtx: scala.Int): Term

  def copyAndRetainFreeVar(vMap: util.AbstractMap[Var, Var], idExecCtx: scala.Int): Term


  /**
    * Try to unify two terms
    *
    * @param mediator have the reference of EngineManager
    * @param t1       the term to unify
    * @return true if the term is unifiable with this one
    */
  def unify(mediator: PrologEngine, t1: Term): Boolean = {
    val engine = mediator.getEngineManager
    resolveTerm()
    t1.resolveTerm()
    val v1 = new util.LinkedList[Var]
    /* Reviewed by: Paolo Contessi (was: ArrayList()) */
    val v2 = new util.LinkedList[Var]
    val ok = unify(v1, v2, t1, mediator.getFlagManager.isOccursCheckEnabled)
    if (ok) {
      val ec = engine.getCurrentContext
      if (ec != null) {
        var id = if (engine.getEnv == null) Var.PROGRESSIVE
        else engine.getEnv.getNDemoSteps
        // Update trailingVars
        ec.trailingVars_$eq(new OneWayList[util.List[Var]](v1, ec.trailingVars))
        // Renaming after unify because its utility regards not the engine but the user
        var count = 0
        for (v <- v1.asScala) {
          v.rename(id, count)
          if (id >= 0) id += 1
          else count += 1
        }
        for (v <- v2.asScala) {
          v.rename(id, count)
          if (id >= 0) id += 1
          else count += 1
        }
      }
      return true
    }
    Var.free(v1)
    Var.free(v2)
    false
  }

  /**
    * Tests if this term is unifiable with an other term.
    * No unification is done.
    * <p>
    * The test is done outside any demonstration context
    *
    * @param t the term to checked
    * @param isOccursCheckEnabled
    * @return true if the term is unifiable with this one
    */
  def `match`(isOccursCheckEnabled: Boolean, t: Term): Boolean = {
    resolveTerm()
    t.resolveTerm()
    val v1 = new util.LinkedList[Var]
    val v2 = new util.LinkedList[Var]
    val ok = unify(v1, v2, t, isOccursCheckEnabled)
    Var.free(v1)
    Var.free(v2)
    ok
  }

  /**
    * Tests if this term is unifiable with an other term.
    * No unification is done.
    * <p>
    * The test is done outside any demonstration context
    *
    * @param t the term to checked
    * @return true if the term is unifiable with this one
    */
  def `match`(t: Term): Boolean = `match`(true, t)

  /**
    * Tries to unify two terms, given a demonstration context
    * identified by the mark integer.
    * <p>
    * Try the unification among the term and the term specified
    *
    * @param varsUnifiedArg1 Vars unified in myself
    * @param varsUnifiedArg2 Vars unified in term t
    * @param isOccursCheckEnabled
    */
  def unify(varsUnifiedArg1: util.List[Var], varsUnifiedArg2: util.List[Var], t: Term, isOccursCheckEnabled: Boolean): Boolean

  /**
    * Tries to unify two terms, given a demonstration context
    * identified by the mark integer.
    * <p>
    * Try the unification among the term and the term specified
    *
    * @param varsUnifiedArg1 Vars unified in myself
    * @param varsUnifiedArg2 Vars unified in term t
    */
  private[data] def unify(varsUnifiedArg1: util.List[Var], varsUnifiedArg2: util.List[Var], t: Term): Boolean = {
    unify(varsUnifiedArg1,varsUnifiedArg2,t,true)
  }

  /**
    * Gets the string representation of this term
    * as an X argument of an operator, considering the associative property.
    */
  def toStringAsArgX(op: OperatorManager, prio: scala.Int): String = toStringAsArg(op, prio, true)

  /**
    * Gets the string representation of this term
    * as an Y argument of an operator, considering the associative property.
    */
  def toStringAsArgY(op: OperatorManager, prio: scala.Int): String = toStringAsArg(op, prio, false)

  /**
    * Gets the string representation of this term
    * as an argument of an operator, considering the associative property.
    * <p>
    * If the boolean argument is true, then the term must be considered
    * as X arg, otherwise as Y arg (referring to prolog associative rules)
    */
  private[data] def toStringAsArg(op: OperatorManager, prio: scala.Int, x: Boolean) = toString

  /**
    * The iterated-goal term G of a term T is a term defined
    * recursively as follows:
    * <ul>
    * <li>if T unifies with ^(_, Goal) then G is the iterated-goal
    * term of Goal</li>
    * <li>else G is T</li>
    * </ul>
    **/
  def iteratedGoalTerm: Term = this

  def toJSON: String = JSONSerializerManager.toJSON(this)
}