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
package com.szadowsz.gospel.core.data

import com.szadowsz.gospel.core.error.InvalidTermException
import java.util

/**
  * This class represents a variable term.
  * Variables are identified by a name (which must starts with
  * an upper case letter) or the anonymous ('_') name.
  *
  * @see Term
  */
@SerialVersionUID(1L)
object Var {
  /* Identify kind of renaming */
  private val ORIGINAL = -1
  val PROGRESSIVE = -2

  private val ANY = "_"

  //static version as global counter
  private var fingerprint = 0

  //called by Var constructors
  private def getFingerprint = {
    fingerprint += 1
    fingerprint
  }

  /**
    * De-unify the variables of list
    */
  def free(varsUnified: util.List[Var]): Unit = {
    import scala.collection.JavaConversions._
    for (v <- varsUnified) {
      v.free()
    }
  }
}

// scalastyle:off number.of.methods

/**
  * Creates a internal engine variable.
  *
  * @param n     is the name
  * @param id    is the id of ExecCtx
  * @param count timestamp
  */
@SerialVersionUID(1L)
class Var private(n: String, id: scala.Int, count: scala.Long) extends Term {

  // the name identifying the var
  private var name = n

  /* link is used for unification process */
  private var link: Term = _

  /* internalTimestamp is used for fix vars order (resolveTerm()) */
  private var internalTimestamp = count

  /* id of ExecCtx owners of this var util for renaming*/
  private var ctxid = id //if (id < 0) Var.ORIGINAL else id

  //fingerPrint is a unique id (per run) used for var comparison
  private var fingerPrint = Var.getFingerprint

  private var completeName = new StringBuilder()

  /**
    * Creates an anonymous variable
    * <p>
    * This is equivalent to build a variable with name _
    */
  def this() {
    this(null, Var.ORIGINAL, 0L)
  }

  /**
    * Creates a variable identified by a name.
    * <p>
    * The name must starts with an upper case letter or the underscore. If an underscore is
    * specified as a name, the variable is anonymous.
    *
    * @param n is the name
    * @throws InvalidTermException if n is not a valid Prolog variable name
    */
  def this(n: String) {
    this(if (n == Var.ANY) null else n, Var.ORIGINAL, 0L)
    buildCompleteName()
  }

  private def this(n: String, id: scala.Int, alias: scala.Int, count: scala.Long /*, boolean isCyclic*/) {
    this(n, if (id < 0) Var.ORIGINAL else id, count)
    rename(id, alias)
  }

  /**
    * Rename variable (assign completeName)
    */
  private def buildCompleteName(): Unit = {
    if (name != null) {
      if (Character.isUpperCase(name.charAt(0)) || name.startsWith(Var.ANY)) {
        completeName = new StringBuilder(name)
      } else {
        throw new InvalidTermException("Illegal variable name: " + name)
      }
    }
  }

  /**
    * Rename variable (assign completeName)
    */
  private[data] def rename(idExecCtx: scala.Int, count: scala.Int): Unit = {
    ctxid = idExecCtx
    if (ctxid > Var.ORIGINAL) {
      completeName = completeName
        .delete(0, completeName.length())
        .append(name).append("_e").append(ctxid)
    } else if (ctxid == Var.ORIGINAL) {
      completeName = completeName
        .delete(0, completeName.length())
        .append(name)
    } else if (ctxid == Var.PROGRESSIVE) {
      completeName = completeName
        .delete(0, completeName.length())
        .append("_").append(count)
    }
  }

  /**
    * Gets a copy of this variable.
    * <p>
    * if the variable is not present in the list passed as argument,
    * a copy of this variable is returned and added to the list. If instead
    * a variable with the same time identifier is found in the list,
    * then the variable in the list is returned.
    */
  override def copy(vMap: util.AbstractMap[Var, Var], idExecCtx: scala.Int): Term = {
    val tt = getTerm()
    if (tt eq this) {
      val v = vMap.computeIfAbsent(this, (k: Var) => new Var(name, idExecCtx, 0, internalTimestamp /*, this.isCyclic*/))
      //No occurence of v before
      v
    } else {
      tt.copy(vMap, idExecCtx)
    }
  }

  override def copyAndRetainFreeVar(vMap: util.AbstractMap[Var, Var], idExecCtx: scala.Int): Term = {
    val tt = getTerm()
    if (tt eq this) {
      val v = vMap.computeIfAbsent(this, k => this) // No occurence of v before
      v
    } else {
      tt.copy(vMap, idExecCtx)
    }
  }

  /**
    * Gets a copy of this variable.
    */
  override def copy(vMap: util.AbstractMap[Var, Var], substMap: util.AbstractMap[Term, Var]): Term = {
    var v: Var = null
    val temp = vMap.get(this)
    if (temp == null) {
      v = new Var(null, Var.PROGRESSIVE, vMap.size(), internalTimestamp /*, this.isCyclic*/)
      vMap.put(this, v)
    } else {
      v = temp
    }

    //if(v.isCyclic) //Alberto
    //	return v;

    getTerm() match {
      case v2 : Var =>
        val tt = substMap.get(v2)
        if (tt == null) {
          substMap.put(v2, v)
          v.link = null
        } else {
          v.link = if (tt ne v) tt else null
        }
      case s: Struct =>
        v.link = s.copy(vMap, substMap)
      case n : Number =>
        v.link = n
      case _ =>
    }
    v
  }

  /**
    * De-unify the variable
    */
  override def free(): Unit = {
    link = null
  }

  /**
    * Gets the name of the variable
    */
  def getName: String = {
    if (name != null) {
      completeName.toString
    } else {
      Var.ANY
    }
  }

  def setName(s: String): Unit = {
    this.name = s
  }

  def getOriginalName: String = {
    if (name != null) {
      name
    } else {
      Var.ANY  + this.fingerPrint
    }
  }

  /**
    * Gets the term which is referred by the variable.
    * <p>
    * For unbound variable it is the variable itself, while
    * for bound variable it is the bound term.
    */
  override def getTerm(): Term = {
    var previous: Term = this
    var next = link
    while (next != null) {
      previous = next
      if (next.isInstanceOf[Var]) {
        next = next.asInstanceOf[Var].link
      } else {
        next = null
      }
    }
    Option(next).getOrElse(previous)
  }


  /**
    * Gets the term which is direct referred by the variable.
    */
  def getLink: Term = link

  /**
    * Set the term which is direct bound
    */
  def setLink(l: Term): Unit = {
    link = l
  }

  /**
    * Set the timestamp
    */
  private[data] def setInternalTimestamp(t: scala.Long) = {
    internalTimestamp = t
  }

  override def isEmptyList: Boolean = {
    val t = getTerm()
    (t ne this) && t.isEmptyList
  }

  override def isAtomic: Boolean = {
    val t = getTerm()
    (t ne this) && t.isAtomic
  }

  override def isCompound: Boolean = {
    val t = getTerm()
    (t ne this) && t.isCompound
  }

  override def isAtom: Boolean = {
    val t = getTerm()
    (t ne this) && t.isAtom
  }

  override def isList: Boolean = {
    val t = getTerm()
    (t ne this) && t.isList
  }

  override def isGround: Boolean = {
    val t = getTerm()
    (t ne this) && t.isGround
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
    * finds var occurrence in a Struct, doing occur-check.
    *
    * @param vl TODO
    * @param choice
    */
  private def occurCheck(vl: util.List[Var], t: Struct): Boolean = {
    val arity = t.getArity
    for (c <- 0 until arity) {
        t.getTerm(c) match {
        case struct: Struct =>
          if (occurCheck(vl, struct)) {
            return true
          }
        case v: Var =>
          if (v.link == null) {
            vl.add(v)
          }
          if (this == v) {
            return true
          }
        case _ =>
      }
    }
    false
  }


  /**
    * Resolve the occurrence of variables in a Term
    */
  override def resolveTerm(count: scala.Long): scala.Long = {
    val tt = getTerm()
    if (tt ne this) {
      tt.resolveTerm(count)
    } else {
      internalTimestamp = count
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
    * (test done if occursCheck is enabled)
    */
  override def unify(vl1: util.List[Var], vl2: util.List[Var], t: Term, isOccursCheckEnabled: Boolean): Boolean = {
    val tt = getTerm()
    if (tt eq this) {
      val t2 = t.getTerm
      if (t2.isInstanceOf[Var]) {
        t2.asInstanceOf[Var].fingerPrint = this.fingerPrint
        if (this eq t2) {
          try {
            vl1.add(this)
          } catch {
            case e: NullPointerException =>
          }
          return true
        }
      } else if (t2.isInstanceOf[Struct]) {
        if (isOccursCheckEnabled) {
          if (occurCheck(vl2, t2.asInstanceOf[Struct])) {
            //                        //this.isCyclic = true;  //Alberto -> da usare quando si supporteranno i termini ciclici
            return false // da togliere
          }
        } else {
          checkVar(vl2, t2.asInstanceOf[Struct])
        }
      } else if (!t2.isInstanceOf[Number]) {
        return false
      }
      link = t2
      try vl1.add(this)
      catch {
        case e: NullPointerException =>
      }
      true
    } else {
      tt.unify(vl1, vl2, t, isOccursCheckEnabled)
    }
  }

  private def checkVar(vl: util.List[Var], st: Struct): Unit = {
    val arity = st.getArity
    for (c <- 0 until arity) {
      val at = st.getTerm(c)
      if (at.isInstanceOf[Var]) {
        val v = at.asInstanceOf[Var]
        if (v.link == null) {
          vl.add(v)
        }
      } else if (at.isInstanceOf[Struct]) {
        checkVar(vl, at.asInstanceOf[Struct])
      }
    }
  }

  override def isGreater(t: Term): Boolean = {
    val tt = getTerm()
    if (tt eq this) {
      val t2 = t.getTerm
      t2.isInstanceOf[Var] && fingerPrint > t2.asInstanceOf[Var].fingerPrint
    } else {
      tt.isGreater(t)
    }
  }

  /**
    * Gets the string representation of this variable.
    * <p>
    * For bounded variables, the string is <Var Name>/<bound Term>.
    */
  override def toString: String = {
    val tt = getTerm()
    if (name != null) {
      if (tt eq this /* || this.isCyclic*/ ) {
        //if(this.isCyclic) //Alberto
        // return name;
        completeName.toString()
      } else {
        completeName.toString() + " / " + tt.toString
      }
    } else {
      if (tt eq this /*|| this.isCyclic*/ ) {
        Var.ANY + "" + this.fingerPrint //Alberto
      } else {
        tt.toString
      }
    }
  }


  /**
    * Gets the string representation of this variable, providing
    * the string representation of the linked term in the case of
    * bound variable
    */
  def toStringFlattened: String = {
    val tt = getTerm()
    if (name != null) {
      if (tt eq this /*|| this.isCyclic*/ ) {
        //if(this.isCyclic)
        // return name;
        completeName.toString()
      } else {
        tt.toString
      }
    } else {
      if (tt eq this /*|| this.isCyclic*/ ) {
        Var.ANY + "" + this.fingerPrint
      } else {
        tt.toString
      }
    }
  }
}