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

import com.szadowsz.gospel.core.data.Term
import com.szadowsz.gospel.core.db.JLibrary
import com.szadowsz.gospel.core.exception.InterpreterError

/**
  * Comparison and unification of arbitrary terms. Terms are ordered in the so-called "standard order". This order is
  * defined as follows:
  *
  * Variables < Numbers < Strings < Atoms < Compound Terms
  *
  * Variables are sorted by address.
  *
  * Numbers are compared by value. Mixed integer/float are compared as floats. If the comparison is equal, the float is
  * considered the smaller value. If the Prolog flag iso is defined, all floating point numbers precede all integers.
  *
  * Strings are compared alphabetically.
  *
  * Atoms are compared alphabetically.
  *
  * Compound terms are first checked on their arity, then on their functor name (alphabetically) and finally recursively
  * on their arguments, leftmost argument first.
  *
  * Although variables are ordered, there are some unexpected properties one should keep in mind when relying on
  * variable ordering. This applies to the predicates below as to predicate such as sort/2 as well as libraries that
  * reply on ordering such as library library(assoc) and library library(ordsets). Obviously, an established relation
  * A @< B no longer holds if A is unified with e.g., a number. Also unifying A with B invalidates the relation because
  * they become equivalent (==/2) after unification.
  *
  * As stated above, variables are sorted by address, which implies that they are sorted by age', where "older"
  * variables are ordered before newer' variables. If two variables are unified their `shared' age is the age of oldest
  * variable. This implies we can examine a list of sorted variables with `newer' (fresh) variables without invalidating
  * the order. Attaching an attribute, see section 8.1, turns an `old' variable into a `new' one as illustrated below.
  *
  * Note that the first always succeeds as the first argument of a term is always the oldest. This only applies for the
  * first attribute, i.e., further manipulation of the attribute list does not change the "age".
  */
trait TermEquality {
  // scalastyle:off method.name
  this : JLibrary =>

  @throws[InterpreterError]
  def term_equality_2(arg0: Term, arg1: Term): Boolean = arg0.getTerm.isEqual(arg1.getTerm)

//  @throws[InterpreterError]
//  def term_inequality_2(arg0: Term, arg1: Term): Boolean = !arg0.getTerm.isEqual(arg1.getTerm) TODO http://www.swi-prolog.org/pldoc/doc_for?object=(%5C%3D%3D)/2

  @throws[InterpreterError]
  def term_less_than_2(arg0: Term, arg1: Term): Boolean = !(arg0.getTerm.isGreater(arg1.getTerm) || arg0.getTerm.isEqual(arg1.getTerm))

//  @throws[InterpreterError]
//  def term_less_than_or_equal_to_2(arg0: Term, arg1: Term): Boolean = !arg0.getTerm.isGreater(arg1.getTerm) TODO http://www.swi-prolog.org/pldoc/doc_for?object=(@%3D%3C)/2


  @throws[InterpreterError]
  def term_greater_than_2(arg0: Term, arg1: Term): Boolean = arg0.getTerm.isGreater(arg1.getTerm)

  // @throws[InterpreterError]
  //  def term_greater_than_or_equal_to_2(arg0: Term, arg1: Term): Boolean = (arg0.getTerm.isGreater(arg1.getTerm) || arg0.getTerm.isEqual(arg1.getTerm)) TODO http://www.swi-prolog.org/pldoc/doc_for?object=(@%3E%3D)/2

  //def compare_3(?Order, @Term1, @Term2) TODO http://www.swi-prolog.org/pldoc/doc_for?object=compare/3
}
