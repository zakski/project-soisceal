/*
 * tuProlog - Copyright (C) 2001-2002  aliCE team at deis.unibo.it
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
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

import com.szadowsz.gospel.core.data.{Struct, Term}
import com.szadowsz.gospel.core.engine.{Engine, EngineRunner}

/**
  * State to select a valid available clause to evaluate.
  *
  * @param runner the runner the state occurred in.
  */
class GoalSelectionState(protected override val runner: EngineRunner) extends State {

  /**
    * the name of the engine state.
    */
  protected val stateName: String = "Call"

  private def fetchGoal(theEngine: Engine): Option[Term] = {
    theEngine.context.goalsToEval.fetch() match {
      case None =>
        if (theEngine.context.fatherCtx == null) {
          None // demonstration termination
        } else {
          theEngine.context = theEngine.context.fatherCtx  // drop back to the parent execution context
          fetchGoal(theEngine)
        }
      case s: Some[Term] => s
    }
  }

  private[engine] override def doJob(theEngine: Engine): Unit = {
     fetchGoal(theEngine) match {
      case None =>  theEngine.nextState = if (theEngine.hasChoicePoint) runner.END_TRUE_CP else runner.END_TRUE
      case Some(goal) =>
        var curGoal = goal
        val goal_app: Term = curGoal.getTerm

      if (!goal_app.isInstanceOf[Struct]) {
        theEngine.nextState = runner.END_FALSE
      } else {
        /*
         Code inserted to allow evaluation of meta-clause
         such as p(X) :- X. When evaluating directly terms,
         they are converted to execution of a call/1 predicate.
         This enables the dynamic linking of built-ins for
         terms coming from outside the demonstration context.
        */
        if (curGoal ne goal_app) {
          curGoal = new Struct("call", goal_app)
        }

        theEngine.context.currentGoal = curGoal.asInstanceOf[Struct]
        theEngine.nextState = runner.GOAL_EVALUATION
      }
    }
  }
}