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

import com.szadowsz.gospel.core.data.{Struct, Term}
import com.szadowsz.gospel.core.db.Library
import com.szadowsz.gospel.core.engine.context.clause.ClauseInfo

trait ControlPredicates {
  // scalastyle:off method.name
  this : Library =>

  /**
    * ','. True if both `Goal1' and `Goal2' can be proved.
    *
    * @param arg0 first term to prove
    * @param arg1 second term
    * @return True if both `Goal1' and `Goal2' can be proved, false otherwise
    */
  def conjunction_2(arg0: Term, arg1: Term): Boolean = {
    val s: Struct = new Struct(",", arg0.getTerm, arg1.getTerm)
    getEngine.getEngineManager.pushSubGoal(ClauseInfo.extractBody(s))
    true
  }

  /**
    * Discard all choice points created since entering the predicate in which the cut appears. In other words, commit to
    * the clause in which the cut appears and discard choice points that have been created by goals to the left of the
    * cut in the current clause. Meta calling is opaque to the cut. This implies that cuts that appear in a term that is
    * subject to meta-calling (call/1) only affect choice points created by the meta-called term. The following control
    * structures are transparent to the cut: ;/2, ->/2 and *->/2.
    *
    * Cuts appearing in the condition part of ->/2 and *->/2 are opaque to the cut.
    *
    * Stops the enginge from backtracking.
    *
    * @return always returns true.
    */
  def cut_0: Boolean = {
    getEngine.getEngineManager.cut()
    true
  }

  /**
    * Always fail. The predicate fail/0 is translated into a single virtual machine instruction.
    *
    * @return always returns false.
    */
  def fail_0: Boolean = false

  /**
    *
    * Same as fail, but the name has a more declarative connotation.
    *
    * @return always returns false.
    */
  def false_0: Boolean = false

  // def if_then_else_2(arg0: Term, arg1: Term): Boolean // TODO http://www.swi-prolog.org/pldoc/doc_for?object=(-%3E)/2

  // def negation_1(arg0 : term) : Boolean // TODO http://www.swi-prolog.org/pldoc/doc_for?object=(%5C%2B)/1

  // def or_2(arg0: Term, arg1: Term): Boolean // TODO http://www.swi-prolog.org/pldoc/doc_for?object=(%3B)/2

  // def repeat_0: Boolean = true // TODO http://www.swi-prolog.org/pldoc/man?predicate=repeat/0

  // soft_cut_2(arg0: Term, arg1: Term): Boolean // TODO http://www.swi-prolog.org/pldoc/doc_for?object=(*-%3E)/2

  /**
    * Always succeed. The predicate true/0 is translated into a single virtual machine instruction.
    *
    * @return always returns true
    */
  def true_0: Boolean = true

}
