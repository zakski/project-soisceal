/**
  * tuProlog - Copyright (C) 2001-2002  aliCE team at deis.unibo.it
  * <p>
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  * <p>
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * <p>
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
package com.szadowsz.gospel.core.db.libs.builtin

import com.szadowsz.gospel.core.data
import com.szadowsz.gospel.core.data.{Number, Term, Var}
import com.szadowsz.gospel.core.db.JavaLibrary

/**
  * Type tests are semi-deterministic predicates that succeed if the argument satisfies the requested type. Type-test
  * predicates have no error condition and do not instantiate their argument.
  */
trait VerifyTermType {
  // scalastyle:off method.name
  this : JavaLibrary =>

  // def acyclic_term TODO http://www.swi-prolog.org/pldoc/doc_for?object=acyclic_term/1

  /**
    * Predicate to check if a term is an atom.
    *
    * @param t term to check
    * @return true if Term t is currently bound to an atom, false otherwise.
    */
  def atom_1(t: Term): Boolean = t.getTerm.isAtom

  /**
    * Predicate to check if a term is atomic.
    *
    * @param t term to check
    * @return true if Term t is currently bound and is not a compound, false otherwise.
    */
  def atomic_1(t: Term): Boolean = t.getTerm.isAtomic

  // def blob/2 TODO http://www.swi-prolog.org/pldoc/doc_for?object=blob/2

  // def callable/1 TODO http://www.swi-prolog.org/pldoc/doc_for?object=callable/1

  /**
    * Predicate to check if a term is a compound.
    *
    * @param t term to check
    * @return true if Term t is currently bound and is a compound, false otherwise.
    */
  def compound_1(t: Term): Boolean = t.getTerm.isCompound

  // def cyclic_term TODO http://www.swi-prolog.org/pldoc/doc_for?object=cyclic_term/1

  /**
    * Predicate to check if a term is a float.
    *
    * @param t term to check
    * @return true if Term t is currently bound to a float, false otherwise.
    */
  def float_1(t: Term): Boolean = t.getTerm.isInstanceOf[data.Number] && t.getTerm.asInstanceOf[data.Number].isReal

  /**
    * Predicate to check if a term has no free variables.
    *
    * @param t term to check
    * @return true if parts of Term t are currently bound, false otherwise.
    */
  def ground_1(t: Term): Boolean = t.getTerm.isGround

  /**
    * Predicate to check if a term is an integer.
    *
    * @param t term to check
    * @return true if Term t is currently bound to an integer, false otherwise.
    */
  def integer_1(t: Term): Boolean = t.getTerm.isInstanceOf[data.Number] && t.getTerm.asInstanceOf[data.Number].isInteger


  /**
    * Predicate to check if a term is not a var.
    *
    * @param t term to check
    * @return true if Term t is currently not a free variable, false otherwise.
    */
  def nonvar_1(t: Term): Boolean = !var_1(t)

  /**
    * Predicate to check if a term is a number.
    *
    * @param t term to check
    * @return true if Term t is currently bound to a number, false otherwise.
    */
  def number_1(t: Term): Boolean = t.getTerm.isInstanceOf[data.Number]

  // def rational/1 TODO http://www.swi-prolog.org/pldoc/doc_for?object=rational/1

  // def rational/3 TODO http://www.swi-prolog.org/pldoc/doc_for?object=rational/3

  // def string/1 TODO http://www.swi-prolog.org/pldoc/doc_for?object=string/1

  /**
    * Predicate to check if a term is a var.
    *
    * @param t term to check
    * @return true if Term t is currently a free variable, false otherwise.
    */
  def var_1(t: Term): Boolean = t.getTerm.isInstanceOf[Var]

}
