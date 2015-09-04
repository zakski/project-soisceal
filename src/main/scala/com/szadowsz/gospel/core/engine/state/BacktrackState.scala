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

import com.szadowsz.gospel.core.data.{Struct, Term, Var}
import com.szadowsz.gospel.core.engine.context.ChoiceContext
import com.szadowsz.gospel.core.engine.subgoal.SubGoalId
import com.szadowsz.gospel.core.engine.{Engine, EngineRunner}

/**
 * @author Alex Benini
 *
 */
class BacktrackState(runner : EngineRunner) extends State(runner,"Back") {

  private def verifyChoicePoint(e: Engine, curChoice: ChoiceContext): Boolean = {
    if (curChoice == null) {
      e.nextState = runner.END_FALSE
      val goal = e.context.currentGoal
      _logger.warn("The predicate TODO is unknown.",goal.getPredicateIndicator)
      return false
    }
    true
  }

  private def makeCoherentExecStack(e: Engine, cGoal: Term, curChoice: ChoiceContext) {
    var curGoal = cGoal
    e.context.currentGoal = curGoal.asInstanceOf[Struct]
    var curCtx = e.context
    var pointer = curCtx.trailingVars
    var stopDeunify = curChoice.varsToDeunify
    val varsToDeunify  = stopDeunify.getHead
    Var.free(varsToDeunify)
    varsToDeunify.clear
    var fatherIndex: SubGoalId = null

    // bring parent contexts to a previous state in the demonstration
    do {
      // deunify variables in sibling contexts
      while (pointer != stopDeunify) {
        Var.free(pointer.getHead)
        pointer = pointer.getTail
      }
      curCtx.trailingVars = pointer

      if (curCtx.fatherCtx != null) {

        stopDeunify = curCtx.fatherVarsList
        fatherIndex = curCtx.fatherGoalId
        curCtx = curCtx.fatherCtx
        curGoal = curCtx.goalsToEval.backTo(fatherIndex).getTerm
        if (!(curGoal.isInstanceOf[Struct])) {
          e.nextState = runner.END_FALSE
          return
        }
        curCtx.currentGoal = curGoal.asInstanceOf[Struct]
        pointer = curCtx.trailingVars
      }
    } while (curCtx.fatherCtx != null)
    //set next state
    e.nextState = runner.GOAL_EVALUATION
  }

  private[gospel] def doJob(e: Engine) {
    val curChoice: ChoiceContext = e.fetchChoicePoint
    if (!verifyChoicePoint(e, curChoice))
      return

    // deunify variables and reload old goal
    e.currentAlternative = curChoice
    e.context = curChoice.executionContext
    val curGoal = e.context.goalsToEval.backTo(curChoice.indexSubGoal).getTerm

    if (!(curGoal.isInstanceOf[Struct])) {
      e.nextState = runner.END_FALSE
      return
    }

    // do the backtrack
    makeCoherentExecStack(e,curGoal,curChoice)
  }
}