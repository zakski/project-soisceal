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
import com.szadowsz.gospel.core.db.Library

trait TermUnification {
  // scalastyle:off method.name
  this : Library =>

  /**
    * Equivalent to \+Term1 = Term2.
    * This predicate is logically sound if its arguments are sufficiently instantiated. In other cases, such as
    * ?- X \= Y., the predicate fails although there are solutions. This is due to the incomplete nature of \+/1.
    *
    * To make your programs work correctly also in situations where the arguments are not yet sufficiently instantiated,
    * use dif/2 instead.
    *
    * @param arg0 first term
    * @param arg1 second term
    * @return true if unification between term1 with term2 fails
    */
  def deunify_2(arg0: Term, arg1: Term): Boolean = !unify(arg0, arg1)


  /**
    * Unify Term1 with Term2. True if the unification succeeds. For behaviour on cyclic terms see the Prolog flag
    * occurs_check. It acts as if defined by the following fact:
    *
    * '='(Term, Term)
    *
    * @param arg0 first term
    * @param arg1 second term
    * @return true if unification between term1 with term2 succeeds
    */
  def unify_2(arg0: Term, arg1: Term): Boolean = unify(arg0, arg1)

}
