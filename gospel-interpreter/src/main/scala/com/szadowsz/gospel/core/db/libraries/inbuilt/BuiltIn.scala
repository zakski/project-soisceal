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
package com.szadowsz.gospel.core.db.libraries.inbuilt

import com.szadowsz.gospel.core.Interpreter
import com.szadowsz.gospel.core.data.{Number, Struct, Term, Var}
import com.szadowsz.gospel.core.db.libraries.{Library, predicate}
import com.szadowsz.gospel.core.db.theory.Theory
import com.szadowsz.gospel.core.db.theory.clause.Clause
import com.szadowsz.gospel.core.exception.{CutException, InterpreterError}

class BuiltIn(wam: Interpreter) extends Library(wam)
  with BuiltInSrcControl
  with BuiltInArithmetic
  with BuiltInTermComparison
  with BuiltInFlags {
  // scalastyle:off method.name
  
  /**
    * A callable term is an atom of a compound term. See the ISO Standard definition in section 3.24.
    */
  private def isCallable(goal: Term): Boolean = goal.isAtom || goal.isCompound
  
  /**
    * Convert a term to a goal before executing it by means of call/1. See section 7.6.2 of the ISO Standard for
    * details.
    *
    * If T is a variable then G is the control construct call, whose argument is T.
    *
    * If the principal functor of T is t ,?/2 or ;/2 or ->/2, then each argument of T shall also be converted to a goal.
    *
    * If T is an atom or compound term with principal functor FT, then G is a predication whose predicate indicator is
    * FT, and the arguments, if any, of T and G are identical
    *
    * Note that a variable X and a term call(X) are converted to identical bodies. Also note that if T is a number, then
    * there is no goal which corresponds to T.
    */
  private def convertTermToGoal(term: Term): Term = {
    term.getBinding match {
      case n: Number => null
      case v: Var => new Struct("call", term)
      case s: Struct =>
        val pi: String = s.getPredicateIndicator
        if (pi == ";/2" || pi == ",/2" || pi == "->/2") {
          for (i <- 0 until s.getArity) {
            val t: Term = s(i)
            val arg: Term = convertTermToGoal(t)
            if (arg == null) return null
            s(i) = arg
          }
        }
        s
      case t => t
    }
  }
  
  override def getTheory: Option[Theory] = {
    Some(new Theory(
      """
        |% call/1 is coded both in Prolog, to feature the desired opacity to cut, and in Java as a primitive built-in, to
        |% account for goal transformations that should be performed before execution as mandated by ISO Standard, see 
        |% section 7.8.3.1
        |call(G) :- call_guard(G), '$call'(G).
        |
        |catch(Goal, Catcher, Handler) :- call(Goal).
        |
        |member(E,L) :- member_guard(E,L), member0(E,L).
        |member0(E,[E|_]).
        |member0(E,[_|L]):- member0(E,L).
        |
        |% True if Goal cannot be proven. Retained for compatibility only. New code should use \+/1.
        |not(G) :- G,!,fail.
        |not(_).
        |                  
    """.stripMargin
        + getFlagTheoryString
        + getArithmeticTheoryString
        + getTermCompareTheoryString
    ))
  }
  
  
  @throws[InterpreterError]
  @predicate(1)
  def call_guard_1: Term => Boolean = {
    goal: Term =>
      val e = goal.getExecutor
      goal.getBinding match {
        case v: Var => throw InterpreterError.buildInstantiationError(e, 1)
        case uncallable: Term if !isCallable(uncallable) => throw InterpreterError.buildTypeError(e, 1, "callable", uncallable)
        case _ => true
      }
  }
  
  @throws[InterpreterError]
  @predicate(2)
  def member_guard_2: (Term, Term) => Boolean = { (arg0: Term, arg1: Term) =>
    if (!arg1.getBinding.isInstanceOf[Var] && !arg1.getBinding.isList) {
      throw InterpreterError.buildTypeError(arg1.getExecutor, 2, "list", arg1.getBinding)
    } else {
      true
    }
  }
  
  /**
    * Invoke Goal as a goal. The same as call/1, but it is not opaque to cut.
    */
  @predicate(1)
  @throws(classOf[InterpreterError])
  def $call_1: Term => Boolean = {
    goal: Term =>
      val e = goal.getExecutor
      goal.getBinding match {
        case v: Var => throw InterpreterError.buildInstantiationError(e, 1)
        case uncallable: Term if !isCallable(uncallable) => throw InterpreterError.buildTypeError(e, 1, "callable", uncallable)
        case binding: Term =>
          val termToGoal = convertTermToGoal(goal)
          e.identifyPredicate(termToGoal)
          e.pushSubGoal(Clause.extractBody(termToGoal))
          true
      }
  }
  
  /**
    * Always fail. The predicate fail/0 is translated into a single virtual machine instruction.
    *
    * @return always returns false.
    */
  @predicate(0)
  def fail_0: () => Boolean = () => false

  /**
    * Always fail. The predicate false/0 is translated into a single virtual machine instruction.
    *
    * @return always returns false
    */
  @predicate(0)
  def false_0: () => Boolean = () => false
  
  
  /**
    * Always succeed. The predicate true/0 is translated into a single virtual machine instruction.
    *
    * @return always returns true
    */
  @predicate(0)
  def true_0: () => Boolean = () => true
  
  /**
    * Unify Term1 with Term2. True if the unification succeeds. For behaviour on cyclic terms see the Prolog flag
    * occurs_check. It acts as if defined by the following fact:
    *
    * '='(Term, Term)
    *
    * @return true if unification between term1 with term2 succeeds
    */
  @predicate(2, "=")
  def unify_2: (Term, Term) => Boolean = (arg0, arg1) => {
    arg0.unify(arg1)
  }
  
  
  /**
    * Stops the executor from backtracking.
    *
    * @return always returns true.
    */
  @predicate(0, true, "!")
  def cut_0: () => Boolean = {
    /**
      * Use an Exception to signfy we want to perform a cut. Unfortunate byproduct of how we are separating the executor from the lambda
      * functions
      */
    () => throw new CutException()
  }
}
