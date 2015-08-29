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
package alice.tuprolog.core.data

import java.{util => ju}

import scala.collection.JavaConverters._

import alice.tuprolog.core.exception.interpreter.InvalidTermException

/**
 * This class represents a variable term.
 * Variables are identified by a name (which must starts with
 * an upper case letter) or the anonymous ('_') name.
 *
 * @see Term
 *
 */
@SerialVersionUID(1L)
object Var {
  private[tuprolog] val ANY: String = "_"
  private[tuprolog] val ORIGINAL: Int = -1
  private[tuprolog] val PROGRESSIVE: Int = -2

  /**
   * De-unify the variables of list
   */
  def free(varsUnified: ju.List[Var]) {
    varsUnified.asScala.foreach(_.free())
  }
}
/**
 * By Default creates an anonymous variable
 *
 * This is equivalent to build a variable with name _
 */

@SerialVersionUID(1L)
final class Var extends Term {
  private var name: String = null
  private var completeName = new StringBuilder
  private var link: Term = null
  private var timestamp: Long = 0L
  private var id = Var.ORIGINAL

  /**
   * Creates a variable identified by a name.
   *
   * The name must starts with an upper case letter or the underscore. If an underscore is
   * specified as a name, the variable is anonymous.
   *
   * @param n is the name
   * @throws InvalidTermException if n is not a valid Prolog variable name
   */
  def this(n: String) {
    this()
    if (n == Var.ANY) {
      name = null
    } else if (Character.isUpperCase(n.charAt(0)) || n.startsWith(Var.ANY)) {
      name = n
      completeName.append(n)
    } else {
      throw new InvalidTermException("Illegal variable name: " + n)
    }
  }

  /**
   * Creates a internal engine variable.
   *
   * @param n is the name
   * @param id is the id of ExecCtx
   * @param alias code to discriminate external vars
   * @param time is timestamp
   */
  private def this(n: String, id: Int, alias: Int, time: Long) {
    this()
    name = n
    timestamp = time
    link = null
    rename(if (id < 0)Var.ORIGINAL else id, alias)
  }



  /**
   * Rename variable (assign completeName)
   */
  private[tuprolog] def rename(idExecCtx: Int, count: Int) {
    id = idExecCtx
    if (id > -1) {
      completeName = completeName.delete(0, completeName.length).append(name).append("_e").append(id)
    }
    else if (id == Var.ORIGINAL) {
      completeName = completeName.delete(0, completeName.length).append(name)
    }
    else if (id == Var.PROGRESSIVE) {
      completeName = completeName.delete(0, completeName.length).append("_").append(count)
    }
  }

  /**
   * Gets a copy of this variable.
   *
   * if the variable is not present in the list passed as argument,
   * a copy of this variable is returned and added to the list. If instead
   * a variable with the same time identifier is found in the list,
   * then the variable in the list is returned.
   */
  private[tuprolog] override def copy(vMap: ju.AbstractMap[Var, Var], idExecCtx: Int): Term = {
    val tt: Term = getTerm
    if (tt eq this) {
      var v: Var = vMap.get(this)
      if (v == null) {
        //No occurence of v before
        v = new Var(name, idExecCtx, 0, timestamp)
        vMap.put(this, v)
      }
      v
    }
    else {
      tt.copy(vMap, idExecCtx)
    }
  }

  /**
   * Gets a copy of this variable.
   */
  private[tuprolog] override def copy(vMap: ju.AbstractMap[Var, Var], substMap: ju.AbstractMap[Term, Var]): Term = {
    var v: Var = null
    val temp: AnyRef = vMap.get(this)
    if (temp == null) {
      v = new Var(null, Var.PROGRESSIVE, vMap.size, timestamp)
      vMap.put(this, v)
    }
    else {
      v = temp.asInstanceOf[Var]
    }
    val t: Term = getTerm
    if (t.isInstanceOf[Var]) {
      val tt: AnyRef = substMap.get(t)
      if (tt == null) {
        substMap.put(t, v)
        v.link = null
      }
      else {
        v.link = if (tt ne v) tt.asInstanceOf[Var] else null
      }
    }
    if (t.isInstanceOf[Struct]) {
      v.link = t.copy(vMap, substMap)
    }
    if (t.isInstanceOf[numeric.Number])
      v.link = t

    v
  }

  /**
   * De-unify the variable
   */
  override def free() {
    link = null
  }

  /**
   * Gets the name of the variable
   */
  def getName: String = {
    if (name != null) {
      completeName.toString()
    } else {
      Var.ANY
    }
  }

  /**
   * Gets the name of the variable
   */
  def getOriginalName: String = {
    if (name != null) {
      name
    } else {
      Var.ANY + hashCode
    }
  }


  /**
   * Gets the term which is referred by the variable.
   *
   * For unbound variable it is the variable itself, while
   * for bound variable it is the bound term.
   */
  override def getTerm: Term = {
    link match {
      case variable : Var => variable.getTerm
      case term : Term => term
      case null => this
    }
  }

  /**
   * Gets the term which is direct referred by the variable.
   */
  def getLink: Term = {
    link
  }

  /**
   * Set the term which is direct bound
   */
  private[tuprolog] def setLink(l: Term) {
    link = l
  }

  /**
   * Set the timestamp
   */
  private[tuprolog] def setTimestamp(t: Long) {
    timestamp = t
  }

  override def isEmptyList: Boolean = {
    val t: Term = getTerm
    !(t eq this) && t.isEmptyList
  }

  override def isAtomic: Boolean = {
    val t: Term = getTerm
    !(t eq this) && t.isAtomic
  }

  override def isCompound: Boolean = {
    val t: Term = getTerm
    !(t eq this) &&t.isCompound

  }

  override def isAtom: Boolean = {
    val t: Term = getTerm
    !(t eq this) && t.isAtom
  }

  override def isList: Boolean = {
    val t: Term = getTerm
    !(t eq this) && t.isList
  }

  override def isGround: Boolean = {
    val t: Term = getTerm
    !(t eq this) && t.isGround
  }

  /**
   * Tests if this variable is ANY
   */
  def isAnonymous: Boolean = name == null

  /**
   * Tests if this variable is bound
   */
  def isBound: Boolean = link != null

  /**
   * finds var occurence in a Struct, doing occur-check.
   * (era una findIn)
   * @param vl TODO
   */
  private def occurCheck(vl: ju.List[Var], t: Struct): Boolean = {
    val arity: Int = t.getArity
    for (c <- 0 until arity) {
      val at: Term = t.getTerm(c)
      if (at.isInstanceOf[Struct]) {
        if (occurCheck(vl, at.asInstanceOf[Struct])) {
          return true
        }
      }
      else if (at.isInstanceOf[Var]) {
        val v: Var = at.asInstanceOf[Var]
        if (v.link == null) {
          vl.add(v)
        }
        if (this eq v) {
          return true
        }
      }
    }
    false
  }

  /**
   * Resolve the occurence of variables in a Term
   */
  private[tuprolog] def resolveTerm(count: Long): Long = {
    val tt: Term = getTerm
    if (tt ne this) {
      tt.resolveTerm(count)
    }
    else {
      timestamp = count
      count + 1
    }
  }

  /**
   * var unification.
   * <p>
   * First, verify the Term eventually already unified with the same Var
   * if the Term exist, unify var with that term, in order to handle situation
   * as (A = p(X) , A = p(1)) which must produce X/1.
   * <p>
   * If instead the var is not already unified, then:
   * <p>
   * if the Term is a var bound to X, then try unification with X
   * so for example if A=1, B=A then B is unified to 1 and not to A
   * (note that it's coherent with chronological backtracking:
   * the eventually backtracked A unification is always after
   * backtracking of B unification.
   * <p>
   * if are the same Var, unification must succeed, but without any new
   * bindings (to avoid cycles for extends in A = B, B = A)
   * <p>
   * if the term is a number, then it's a success and new link is created
   * (retractable by means of a code)
   * <p>
   * if the term is a compound, then occur check test is executed:
   * the var must not appear in the compound ( avoid X=p(X),
   * or p(X,X)=p(Y,f(Y)) ); if occur check is ok
   * then it's success and a new link is created (retractable by a code)
   */
  private[tuprolog] def unify(vl1: ju.List[Var], vl2: ju.List[Var], t: Term): Boolean = {
    val tt: Term = getTerm
    if (tt eq this) {
      val term = t.getTerm
      if (term.isInstanceOf[Var]) {
        if (this eq term) {
          try {
            vl1.add(this)
          }
          catch {
            case e: NullPointerException =>
          }
          return true
        }
      } else if (term.isInstanceOf[Struct]) {
        if (occurCheck(vl2, term.asInstanceOf[Struct])) {
          return false
        }
      }
      link = term
      try {
        vl1.add(this)
      }
      catch {
        case e: NullPointerException => /* vl1==null mean nothing intresting for the caller */
      }
      true
    } else {
      tt.unify(vl1, vl2, t)
    }
  }

  /**
   * Gets a copy of this variable
   */
  def isGreater(t: Term): Boolean = {
    val tt: Term = getTerm
    if (tt eq this) {
      val term = t.getTerm
      if (!term.isInstanceOf[Var]){
        false
      } else {
        timestamp > term.asInstanceOf[Var].timestamp
      }
    } else {
      tt.isGreater(t)
    }
  }

  def isGreaterRelink(t: Term, vorder: ju.ArrayList[String]): Boolean = {
    val tt: Term = getTerm
    if (tt eq this) {
      val term = t.getTerm
      if (!term.isInstanceOf[Var]){
        false
      } else {
        vorder.indexOf(tt.asInstanceOf[Var].getName) > vorder.indexOf(term.asInstanceOf[Var].getName)
      }
    }
    else {
      tt.isGreaterRelink(t, vorder)
    }
  }

  def isEqual(t: Term): Boolean = {
    val tt: Term = getTerm
    if (tt eq this) {
      val term = t.getTerm
      term.isInstanceOf[Var] && timestamp == term.asInstanceOf[Var].timestamp
    }
    else {
      tt.isEqual(t)
    }
  }

  def setName(s: String) {
    this.name = s
  }

  /**
   * Gets the string representation of this variable.
   *
   * For bounded variables, the string is <Var Name>/<bound Term>.
   */
  override def toString: String = {
    val tt: Term = getTerm
    if (name != null) {
      if (tt eq this) {
        completeName.toString()

      } else {
        completeName.toString + " / " + tt.toString
      }

    } else {
      if (tt eq this) {
        Var.ANY + hashCode

      } else {
        tt.toString
      }
    }
  }

  /**
   * Gets the string representation of this variable, providing
   * the string representation of the linked term in the case of
   * bound variable
   *
   */
  def toStringFlattened: String = {
    val tt: Term = getTerm
    if (name != null) {
      if (tt eq this) {
        completeName.toString()
      }else {
        tt.toString
      }
    } else {
      if (tt eq this) {
        Var.ANY + hashCode
      } else {
        tt.toString
      }
    }
  }
}