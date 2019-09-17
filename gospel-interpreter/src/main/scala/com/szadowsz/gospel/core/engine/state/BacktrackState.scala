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

import java.util

import com.szadowsz.gospel.core.data.{Struct, Term, Var}
import com.szadowsz.gospel.core.engine.{Executor, Result}
import com.szadowsz.gospel.core.engine.context.{ChoicePointContext, ExecutionContext}

class BacktrackState extends State {
  /**
    * the name of the engine state.
    */
  override protected val stateName: String = "Back"
  
  override def doJob(e: Executor): Unit = {
    e.choicePointSelector.findValidChoice match {
      case None =>
        e.nextState = EndState(Result.FALSE)
        
        val goal = e.currentContext.currentGoal.get // TODO Review Possibility of goal being none.
        if (!e.checkExistence(goal.getPredicateIndicator)) {
          e.logWarning("The predicate " + goal.getPredicateIndicator + " is unknown.")
        }
      case Some(curChoice) => doBacktrack(e,curChoice)
    }
  }
  
  private def doBacktrack(e : Executor, curChoice : ChoicePointContext): Unit = {
    e.currentAlternative = Some(curChoice)
    //deunify variables and reload old goal
    e.currentContext = curChoice.execContext
    val curGoal: Term = e.currentContext.goalsToEval.backTo(curChoice.indexSubGoal).orNull.getBinding
    if (!curGoal.isInstanceOf[Struct]) {
      e.nextState = EndState(Result.FALSE)
    } else {
      e.currentContext.currentGoal = Some(curGoal.asInstanceOf[Struct])
      // Rende coerente l'execution_stack
       resetParentContexts(e,curChoice)
    }
  }
  
  private def resetParentContexts(e : Executor, curChoice : ChoicePointContext): Unit ={
    // bring parent contexts to a previous state in the demonstration
    var stopDeunify = curChoice.varsToDeunify
    val varsToDeunify: util.List[Var] = stopDeunify.head
    varsToDeunify.forEach(_.freeVars())
    varsToDeunify.clear()
    var shouldContinue = true
    var curCtx: ExecutionContext = e.currentContext
    var pointer = curCtx.trailingVars
    do {
      // deunify variables in sibling contexts
      while (pointer ne stopDeunify) {
        pointer.head.forEach(_.freeVars())
        pointer = pointer.tail
      }
      curCtx.trailingVars = pointer
      curCtx.parent match {
        case None => shouldContinue = false
        case Some(parent) =>
          stopDeunify = curCtx.parentVarList.get
          val fatherIndex = curCtx.parentGoalId.get
          curCtx = parent
          val curGoal = curCtx.goalsToEval.backTo(fatherIndex).orNull.getBinding
          if (!curGoal.isInstanceOf[Struct]) {
            e.nextState = EndState(Result.FALSE)
            shouldContinue = false
          } else {
            curCtx.currentGoal = Some(curGoal.asInstanceOf[Struct])
            pointer = curCtx.trailingVars
          }
      }
    } while (shouldContinue)
    e.nextState = new GoalEvaluationState
  }
}
