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
package com.szadowsz.gospel.core.data

import java.util
import java.util.concurrent.atomic.AtomicInteger

import com.szadowsz.gospel.core.exception.InvalidTermException

object Var {
  private val ANY = "_"
  /* Identify kind of renaming */
  private val ORIGINAL = -1
  private val PROGRESSIVE = -2
  
  //static version as global counter
  private val fingerprint = new AtomicInteger
  
  //called by Var constructors
  private def getFingerprint = {
    fingerprint.addAndGet(1)
  }
  
  
}

class Var(val name: String, id: scala.Int, count: Long) extends Term {
  
  //fingerPrint is a unique id (per run) used for var comparison
  private var fingerPrint = Var.getFingerprint
  
  /* internalTimestamp is used for fix vars order (resolveTerm()) */
  private var internalTimestamp = count
  
  /* id of ExecCtx owners of this var util for renaming*/
  private var ctxid = if (id < 0) Var.ORIGINAL else id
  
  private var completeName = new StringBuilder()
  
  /**
    * term used for unification process
    */
  private var binding: Option[Term] = None
  
  def this() {
    this(Var.ANY, Var.ORIGINAL, 0L)
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
  
  private def this(id: scala.Int, alias: scala.Int, count: scala.Long /*, boolean isCyclic*/) {
    this(Var.ANY, if (id < 0) Var.ORIGINAL else id, count)
    rename(id, alias)
  }
  
  override def isAtomic: Boolean = binding.exists(_.isAtomic)
  
  def isAnonymous: Boolean = name == Var.ANY
  
  override def isCompound: Boolean = binding.exists(_.isCompound)
  
  override def isEmptyList: Boolean = binding.exists(_.isEmptyList)
  
  override def isGround: Boolean = binding.exists(_.isGround)
  
  override def isList: Boolean = binding.exists(_.isList)
  
  override def isEquals(term: Term): Boolean = {
    term match {
      case v: Var => name == v.name && binding.sameElements(v.binding)
      case _ => false
    }
  }
  
  def getOriginalName: String = {
    if (name != null) {
      name
    } else {
      Var.ANY + this.fingerPrint
    }
  }
  
  override def getBinding: Term = binding.getOrElse(this)
  
  /**
    * Get the term bound directly to the Var.
    *
    * @return the direct term, or null if unbound.
    */
  def getDirectBinding: Term = binding.orNull
  
  def setBinding(t: Term): Unit = {
    binding = Option(t)
  }
  
  /**
    * Resolves variables inside the term
    *
    * If the variables has been already resolved, no renaming is done.
    */
  override def resolveVars(): Unit = ???
  
  override def freeVars(): Unit = {
    binding = None
  }
  
  /**
    * Rename variable (assign completeName)
    */
  private def buildCompleteName(): Unit = {
    if (name != null) {
      if (Character.isUpperCase(name.charAt(0)) || name.startsWith(Var.ANY)) {
        completeName = new StringBuilder(name)
      } else {
        throw new InvalidTermException("Illegal variable name", name)
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
  
  override def copy(vMap: util.AbstractMap[Var, Var], idExecCtx: scala.Int): Term = {
    val tt = getBinding
    if (tt eq this) {
      val v = vMap.computeIfAbsent(this, k => this) // No occurrence of v before
      v
    } else {
      tt.copy(vMap, idExecCtx)
    }
  }
  
  /**
    * gets a copy for result.
    */
  override private[data] def copy(vMap: util.AbstractMap[Var, Var], substMap: util.AbstractMap[Term, Var]) = {
    var v: Var = null
    val temp = vMap.get(this)
    if (temp == null) {
      v = new Var(Var.PROGRESSIVE, vMap.size(), internalTimestamp)
      vMap.put(this, v)
    } else {
      v = temp
    }
    
    getBinding match {
      case v2: Var =>
        val tt = substMap.get(v2)
        if (tt == null) {
          substMap.put(v2, v)
          v.binding = None
        } else {
          v.binding = Option(if (tt ne v) tt else null)
        }
      case s: Struct =>
        v.binding = Some(s.copy(vMap, substMap))
      case n: Number =>
        v.binding = Some(n)
      case _ =>
    }
    v
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
    *
    * @param vl1 Vars unified in myself
    * @param vl2 Vars unified in term t
    * @param t
    * @param isOccursCheckEnabled
    * @return true if the term is unifiable with this one
    */
  override def unify(vl1: util.List[Var], vl2: util.List[Var], t: Term, isOccursCheckEnabled: Boolean): Boolean = {
    binding match {
      case Some(term) => term.unify(vl1, vl2, t, isOccursCheckEnabled)
      case None =>
        t.getBinding match {
          case v: Var =>
            v.fingerPrint = this.fingerPrint
            if (this eq v) {
              vl1.add(this)
              true
            } else {
              setUnifyBinding(vl1, v)
            }
          case struct: Struct =>
            if (isOccursCheckEnabled) {
              if (occurCheck(vl2, struct)) {
                false // da togliere
              } else {
                setUnifyBinding(vl1, struct)
              }
            } else {
              checkVar(vl2, struct)
              setUnifyBinding(vl1, struct)
            }
          case n: Number =>
            setUnifyBinding(vl1, n)
          case _ => false
        }
    }
  }
  
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
          if (v.binding.isEmpty) {
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
  
  private def checkVar(vl: util.List[Var], st: Struct): Unit = {
    val arity = st.getArity
    for (c <- 0 until arity) {
      val at = st.getTerm(c)
      at match {
        case v: Var =>
          if (v.binding.isEmpty) {
            vl.add(v)
          }
        case struct: Struct =>
          checkVar(vl, struct)
        case _ =>
      }
    }
  }
  
  private def setUnifyBinding(vl1: util.List[Var], t: Term) = {
    binding = Option(t)
    vl1.add(this)
    true
  }
}
