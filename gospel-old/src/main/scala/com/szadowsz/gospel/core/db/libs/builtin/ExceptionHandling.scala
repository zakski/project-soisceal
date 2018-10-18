/**
  *
  */
package com.szadowsz.gospel.core.db.libs.builtin

import com.szadowsz.gospel.core.data.Term
import com.szadowsz.gospel.core.db.Library
import com.szadowsz.gospel.core.exception.InterpreterError

trait ExceptionHandling {
  // scalastyle:off method.name
  this : Library =>

  // def catch_3(arg0 : Term, arg1 : Term, arg2 : Term) : Boolean TODO http://www.swi-prolog.org/pldoc/man?predicate=catch/3

  /**
    * Raise an exception. The system looks for the innermost catch/3 ancestor for which Exception unifies with the
    * Catcher argument of the catch/3 call. See catch/3 for details.
    *
    * ISO demands that throw/1 make a copy of Exception, walk up the stack to a catch/3 call, backtrack and try to unify
    * the copy of Exception with Catcher. SWI-Prolog delays backtracking until it actually finds a matching
    * catch/3 goal. The advantage is that they can start the debugger at the first possible location while preserving
    * the entire exception context if there is no matching catch/3 goal. This approach can lead to different
    * behaviour if Goal and Catcher of catch/3 call shared variables. they assume this to be highly unlikely
    * and could not think of a scenario where this is useful.
    *
    * @param error the exception term
    * @throws InterpreterError containing the exception term
    * @return
    */
  @throws[InterpreterError]
  def throw_1(error: Term): Boolean = throw new InterpreterError(error)
}
