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

import java.util.{ArrayList, List}

import com.szadowsz.gospel.core.data.{Struct, Var}
import com.szadowsz.gospel.core.engine.context.{ChoiceContext, ExecutionContext}
import com.szadowsz.gospel.core.engine.{Engine, EngineRunner}
import com.szadowsz.gospel.core.theory.clause.ClauseStore
import com.szadowsz.gospel.util.OneWayList

/**
 * @author Alex Benini
 *
 */
class RuleSelectionState(runner : EngineRunner) extends State (runner,"Init"){

  private def getClauseOptions(e: Engine, goal: Struct, alternative: ChoiceContext): Option[ClauseStore] = {
    if (alternative == null) {/* from normal evaluation */
    val varsList: List[Var] = new ArrayList[Var]
      e.context.trailingVars = new OneWayList[List[Var]](varsList, e.context.trailingVars)

      val clauseStore = ClauseStore.build(goal, varsList, runner.find(goal))

      /* If there is no way forward we need to consider previous states -- begin backtracking */
      if (clauseStore == null) {
        e.nextState = runner.BACKTRACK
        return None
      } else
        Some(clauseStore)
    } else { /* from backtracking */
      Some(alternative.compatibleGoals)
    }
  }

  private[gospel] override def doJob(theEngine: Engine) {
    val goal: Struct = theEngine.context.currentGoal

     // not already backtracking if no alternatives already exist
    val fromBacktracking = theEngine.currentAlternative != null

    // get a potentially compatible clause so we can attempt to move forward
    val clauseStore: ClauseStore = {
      val opt = getClauseOptions(theEngine, goal, theEngine.currentAlternative); if (opt == None) return else opt.get
    }
    val clauseInfo = clauseStore.fetch.performCopy(theEngine.nDemoSteps)

    // Build up the new ExecutionContext
    // And handle cuts properly if we have alternatives
    val currCtx = theEngine.context

    val curGoal: Struct = currCtx.currentGoal
    val unifiedVars: List[Var] = currCtx.trailingVars.getHead
    curGoal.unify(unifiedVars, unifiedVars, clauseInfo.getHeadCopy)


    //create or destroy Choice Contexts based on whether we are moving forwards or returning to old Choices
    if (clauseStore.haveAlternatives() && !fromBacktracking) {
      theEngine.addChoicePoint(clauseStore)
    } else if (!clauseStore.haveAlternatives() && fromBacktracking) {
      theEngine.hasChoicePoint // removes empty choice points
    }
    //val ec = new ExecutionContext(id,clause.performCopy(id),theEngine.choicePoint,theAlternative,theClauseStore.haveAlternatives())
    theEngine.context = new ExecutionContext(theEngine, clauseInfo, clauseStore.haveAlternatives())
    theEngine.nextState = runner.GOAL_SELECTION
  }
}