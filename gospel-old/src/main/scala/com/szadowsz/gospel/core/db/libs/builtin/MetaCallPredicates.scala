/**
  *
  */
package com.szadowsz.gospel.core.db.libs.builtin

import com.szadowsz.gospel.core.data.{Term, Var}
import com.szadowsz.gospel.core.db.Library
import com.szadowsz.gospel.core.db.libs.BuiltIn
import com.szadowsz.gospel.core.engine.context.clause.ClauseInfo
import com.szadowsz.gospel.core.exception.InterpreterError

trait MetaCallPredicates {
  // scalastyle:off method.name
  this : Library =>

  // def apply_2(arg0 : Term, arg1 : Term) : Boolean TODO http://www.swi-prolog.org/pldoc/man?predicate=apply/2


  /**
    * Invoke arg0 as a goal. Note that clauses may have variables as subclauses.
    *
    * @param arg0 the goal term
    * @throws InterpreterError
    * @return true if successful, false otherwise
    */
 // @throws[InterpreterError]
//  def $call_1(arg0: Term): Boolean = {
//    arg0.getTerm match {
//      case null => throw InterpreterError.type_error(getEngine.getEngineManager, 1, "callable", arg0)
//      case v: Var => throw InterpreterError.instantiation_error(getEngine.getEngineManager, 1)
//      case t: Term if !t.isCallable => throw InterpreterError.type_error(getEngine.getEngineManager, 1, "callable", t)
//      case goal: Term =>
//        val termToGoal = BuiltIn.convertTermToGoal(goal)
//        getEngine.getEngineManager.identify(termToGoal)
//        getEngine.getEngineManager.pushSubGoal(ClauseInfo.extractBody(termToGoal))
//        true
//    }
//  }

  // def call_2(arg0 : Term, arg1 : Term) : Boolean TODO http://www.swi-prolog.org/pldoc/man?predicate=call/2

  // def call_cleanup_2(arg0 : Term, arg1 : Term) : Boolean TODO http://www.swi-prolog.org/pldoc/man?predicate=call_cleanup/2

  // def call_cleanup_3(arg0 : Term, arg1 : Term, arg2 : Term) : Boolean TODO http://www.swi-prolog.org/pldoc/man?predicate=call_cleanup/3

  // def call_with_depth_limit_3(arg0 : Term, arg1 : Term, arg2 : Term) : Boolean TODO http://www.swi-prolog.org/pldoc/man?predicate=call_with_depth_limit/3

  // def call_with_inference_limit(arg0 : Term, arg1 : Term, arg2 : Term) : Boolean TODO http://www.swi-prolog.org/pldoc/doc_for?object=call_with_inference_limit/3

  // def setup_call_cleanup_3(arg0 : Term, arg1 : Term, arg2 : Term) : Boolean TODO http://www.swi-prolog.org/pldoc/doc_for?object=setup_call_cleanup/3

  // def setup_call_catcher_cleanup_4(arg0 : Term, arg1 : Term, arg2 : Term, arg3 : Term) : Boolean TODO http://www.swi-prolog.org/pldoc/doc_for?object=setup_call_catcher_cleanup/4

  // def ignore_1(arg0 : Term) : Boolean TODO http://www.swi-prolog.org/pldoc/man?predicate=ignore/1

  // def not_1(arg0 : Term) : Boolean TODO http://www.swi-prolog.org/pldoc/man?predicate=not/1

  // def once_1(arg0 : Term) : Boolean TODO http://www.swi-prolog.org/pldoc/man?predicate=once/1
}
