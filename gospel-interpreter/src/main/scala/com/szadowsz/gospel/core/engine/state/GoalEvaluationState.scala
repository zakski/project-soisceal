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
package com.szadowsz.gospel.core.engine.state

import com.szadowsz.gospel.core.data.Struct
import com.szadowsz.gospel.core.engine.Executor
import com.szadowsz.gospel.core.exception.{InterpreterError, JvmException}

class GoalEvaluationState extends State {
  /**
    * the name of the engine state.
    */
  override protected val stateName: String = "Eval"
  
  /**
    * Evaluates whether the current goal is able to be resolved as a primitive or as a prolog rule
    * @param e the supplied Executor
    */
  override def doJob(e: Executor): Unit = {
    val curGoal = e.currentContext.currentGoal.get
    if (curGoal.isPrimitive) { // Execute JVM-Backed Predicate
      try {
        e.nextState = if (curGoal.evalAsPredicate()) {
          new GoalSelectionState
        } else {
          new BacktrackState
        }
      } catch {
        case error: InterpreterError =>
          // Replace the goal in which the error occurred with subgoal throw/1
          e.currentContext.currentGoal = new Struct("throw", error.getError)
          e.logException(error)
          e.nextState = new ExceptionState
        case exception: JvmException =>
          // Replace the goal in which the error occurred with subgoal java_throw/1
          e.currentContext.currentGoal = Some(new Struct("java_throw", exception.getException))
          e.logException(exception)
          e.nextState = new ExceptionState
      }
      e.nDemoSteps += 1 // Increment the demonstration steps counter
    } else {
      e.nextState = new RuleSelectionState
    }
  }
}
