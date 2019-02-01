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
import scala.collection.JavaConverters._


//noinspection ScalaStyle
abstract class Term extends Serializable {
  
  /**
    * gets a copy (with renamed variables) of the term.
    *
    * The list argument passed contains the list of variables to be renamed (if empty list then no renaming).
    *
    * Used By The engine to initialise it's stack
    *
    * @param vMap      variables to rename
    * @param idExecCtx Execution Context identifier
    * @return Copy of Term
    */
  def copy(vMap: util.AbstractMap[Var, Var], idExecCtx: scala.Int): Term
  
  /**
    * gets a copy for result.
    */
  private[data] def copy(vMap: util.AbstractMap[Var, Var], substMap: util.AbstractMap[Term, Var]): Term
  
  /**
    * gets a copy of this term for the output
    *
    * @param goalVars
    * @param resultVars
    * @return
    */
  def copyResult(goalVars: util.Collection[Var], resultVars: util.List[Var]): Term = {
    val originals = new util.IdentityHashMap[Var, Var]
    for (key <- goalVars.asScala) {
      val clone = if (!key.isAnonymous) new Var(key.getOriginalName) else new Var
      originals.put(key, clone)
      resultVars.add(clone)
    }
    copy(originals, new util.IdentityHashMap[Term, Var])
  }
  
  
  /**
    * Resolves variables inside the term
    *
    * If the variables has been already resolved, no renaming is done.
    */
  def resolveVars(): Unit
  
  /**
    * Unlink variables inside the term
    */
  def freeVars(): Unit = {
  }
  
  /**
    * Checks if the term is atomic
    *
    * SWI-Prolog defines the following atomic datatypes: atom (atom/1), string (string/1), integer (integer/1), 
    * floating point number (float/1) and blob (blob/2). In addition, the symbol [] (empty list) is atomic, but not an 
    * atom.
    *
    * @return True if Term is bound (i.e., not a variable) and is not compound, false otherwise
    */
  def isAtomic: Boolean = false
  
  def isAtom: Boolean = false
  
  /**
    * Check if this struct is a clause.
    *
    * @return true if this is a clause, false otherwise
    */
  def isClause: Boolean = false
  
  /**
    * Checks if the term is compound
    *
    * @return True if Term is bound to a compound term.
    */
  def isCompound: Boolean = false
  
  def isGround: Boolean
  
  def isEmptyList: Boolean = false
  
  def isEquals(term: Term): Boolean
  
  def isList: Boolean = false
  
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
  def isUnifiable(t: Term,isOccursCheckEnabled: Boolean = true): Boolean = {
    resolveVars()
    t.resolveVars()
    val v1 = new util.LinkedList[Var]
    val v2 = new util.LinkedList[Var]
    val ok = unify(v1, v2, t, isOccursCheckEnabled)
    v1.asScala.foreach(_.freeVars())
    v2.asScala.foreach(_.freeVars())
    ok
  }
  
  final override def equals(obj: Any): Boolean = obj.isInstanceOf[Term] && isEquals(obj.asInstanceOf[Term])
  
  /**
    * Gets the actual term referred by this Term.
    *
    * @return if the Term is a bound variable, the method gets the Term linked to the variable, otherwise returns itself.
    */
  def getBinding: Term = this
  
  /**
    * Tries to unify two terms, given a demonstration context identified by the mark integer.
    *
    * Try the unification among the term and the term specified
    *
    * @param varsUnifiedArg1 Vars unified in myself
    * @param varsUnifiedArg2 Vars unified in term t
    * @param isOccursCheckEnabled
    * @return true if the term is unifiable with this one
    */
  def unify(varsUnifiedArg1: util.List[Var], varsUnifiedArg2: util.List[Var], t: Term, isOccursCheckEnabled: Boolean): Boolean
  
}
