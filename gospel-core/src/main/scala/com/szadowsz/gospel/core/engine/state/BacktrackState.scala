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
 *//*
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

import alice.util.OneWayList
import java.util

import com.szadowsz.gospel.core.data.{Struct, Term, Var}
import com.szadowsz.gospel.core.engine.context.subgoal.SubGoalId
import com.szadowsz.gospel.core.engine.context.{ChoicePointContext, ExecutionContext}
import com.szadowsz.gospel.core.engine.{Engine, EngineRunner}

/**
  * @author Alex Benini
  *
  */
private[engine] final case class BacktrackState(override protected val runner: EngineRunner) extends State {
  protected override val stateName = "Back"

  override def doJob(e: Engine): Unit = {
    val curChoice: ChoicePointContext = e.choicePointSelector.fetch
    if (curChoice == null) {
      e.nextState = runner.END_FALSE
      val goal: Struct = e.currentContext.currentGoal
      if (!runner.getTheoryManager.checkExistence(goal.getPredicateIndicator)) //Alberto
        runner.warn("The predicate " + goal.getPredicateIndicator + " is unknown.")
      return
    }
    e.currentAlternative = curChoice
    //deunify variables and reload old goal
    e.currentContext = curChoice.executionContext
    var curGoal: Term = e.currentContext.goalsToEval.backTo(curChoice.indexSubGoal).orNull.getTerm
    if (!curGoal.isInstanceOf[Struct]) {
      e.nextState = runner.END_FALSE
      return
    }
    e.currentContext.currentGoal = curGoal.asInstanceOf[Struct]
    // Rende coerente l'execution_stack
    var curCtx: ExecutionContext = e.currentContext
    var pointer: OneWayList[util.List[Var]] = curCtx.trailingVars
    var stopDeunify: OneWayList[util.List[Var]] = curChoice.varsToDeunify
    val varsToDeunify: util.List[Var] = stopDeunify.getHead
    Var.free(varsToDeunify)
    varsToDeunify.clear()
    var fatherIndex: SubGoalId = null
    // bring parent contexts to a previous state in the demonstration
    var noParent = false
    do {
      // deunify variables in sibling contexts
      while (pointer ne stopDeunify) {
        Var.free(pointer.getHead)
        pointer = pointer.getTail
      }
      curCtx.trailingVars = pointer
      if (curCtx.fatherCtx == null) {
        noParent = true //todo: break is not supported
      } else {
        stopDeunify = curCtx.fatherVarsList
        fatherIndex = curCtx.fatherGoalId
        curCtx = curCtx.fatherCtx
        curGoal = curCtx.goalsToEval.backTo(fatherIndex).orNull.getTerm
        if (!curGoal.isInstanceOf[Struct]) {
          e.nextState = runner.END_FALSE
          return
        }
        curCtx.currentGoal = curGoal.asInstanceOf[Struct]
        pointer = curCtx.trailingVars
      }
    } while (!noParent)
    //set next state
    e.nextState = runner.GOAL_EVALUATION
  }
}