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

import alice.tuprolog.{Struct, Var}
import com.szadowsz.gospel.core.engine.context.clause.{ClauseInfo, ClauseStore}
import com.szadowsz.gospel.core.engine.context.subgoal.SubGoalStore
import com.szadowsz.gospel.core.engine.context.{ChoicePointContext, ExecutionContext}
import com.szadowsz.gospel.core.engine.{Engine, EngineRunner}

/**
  * @author Alex Benini
  *
  */
private[engine] final case class RuleSelectionState(override protected val runner: EngineRunner) extends State {
  protected override val stateName = "Init"

  override def doJob(e: Engine): Unit = {
    /*----------------------------------------------------
     * Individuo compatibleGoals e
     * stabilisco se derivo da Backtracking.
     */
    val goal: Struct = e.currentContext.currentGoal
    var fromBacktracking: Boolean = true
    val alternative: ChoicePointContext = e.currentAlternative
    var clauseStore: ClauseStore = null
    e.currentAlternative = null
    if (alternative == null) {
      /* from normal evaluation */ fromBacktracking = false
      val varsList: util.List[Var] = new util.ArrayList[Var]
      e.currentContext.trailingVars = new OneWayList[util.List[Var]](varsList, e.currentContext.trailingVars)
      clauseStore = ClauseStore.build(goal, varsList, runner.find(goal))
      if (clauseStore == null) {
        e.nextState = runner.BACKTRACK
        return
      }
    } else {
      clauseStore = alternative.compatibleGoals
    }
    /*-----------------------------------------------------
     * Scelgo una regola fra quelle potenzialmente compatibili.
     */
    val clause: ClauseInfo = clauseStore.fetch
    /*-----------------------------------------------------
     * Build ExecutionContext and ChoicePointContext
     */
    e.nDemoSteps += 1
    val ec: ExecutionContext = new ExecutionContext(e.nDemoSteps - 1)
    val curCtx: ExecutionContext = e.currentContext
    ec.clause = clause.getClause
    //head and body with refresh variables (clause copied)
    clause.performCopy(ec.getId)
    ec.headClause = clause.getHeadCopy
    ec.goalsToEval = new SubGoalStore()
    ec.goalsToEval.load(clause.getBodyCopy)
    // The following block encodes cut functionalities, and hardcodes the
    // special treatment that ISO Standard reserves for goal disjunction:
    // section 7.8.6.1 prescribes that ;/2 must be transparent to cut.
    ec.choicePointAfterCut = e.choicePointSelector.getPointer
    if (alternative != null) {
      var choicePoint: ChoicePointContext = alternative
      val depth: Int = alternative.executionContext.depth
      ec.choicePointAfterCut = choicePoint.prevChoicePointContext
      var currentGoal: Struct = choicePoint.executionContext.currentGoal
      var shouldBreak = false
      while (!shouldBreak&& currentGoal.getName == ";" && currentGoal.getArity == 2) {
          if (choicePoint.prevChoicePointContext != null) {
            val distance: Int = depth - choicePoint.prevChoicePointContext.executionContext.depth
            while (distance == 0 && choicePoint.prevChoicePointContext != null) {
                ec.choicePointAfterCut = choicePoint.prevChoicePointContext.prevChoicePointContext
                choicePoint = choicePoint.prevChoicePointContext
            }
            if (distance == 1 && choicePoint.prevChoicePointContext != null) {
              ec.choicePointAfterCut = choicePoint.prevChoicePointContext.prevChoicePointContext
              currentGoal = choicePoint.prevChoicePointContext.executionContext.currentGoal
              choicePoint = choicePoint.prevChoicePointContext
            } else {
              shouldBreak = true
            } //todo: break is not supported
          } else {
            shouldBreak = true
          } //todo: break is not supported
      }
    }
    val curGoal: Struct = curCtx.currentGoal
    val unifiedVars: util.List[Var] = e.currentContext.trailingVars.getHead
    curGoal.unify(unifiedVars, unifiedVars, ec.headClause, runner.getMediator.getFlagManager.isOccursCheckEnabled)
    ec.haveAlternatives = clauseStore.hasAlternatives
    //creazione cpc
    if (ec.haveAlternatives && !fromBacktracking) {
      val cpc: ChoicePointContext = new ChoicePointContext
      cpc.compatibleGoals = clauseStore
      cpc.executionContext = curCtx
      cpc.indexSubGoal = curCtx.goalsToEval.getCurrentIndex
      cpc.varsToDeunify = e.currentContext.trailingVars
      e.choicePointSelector.add(cpc)
    }
    //distruzione cpc
    if (!ec.haveAlternatives && fromBacktracking) {
      e.choicePointSelector.removeUnusedChoicePoints()
    }
    //Alberto
    if (!ec.tryToPerformTailRecursionOptimization(e)) ec.updateContextAndDepth(e)
    ec.saveParentState()
    e.currentContext = ec
    e.nextState = runner.GOAL_SELECTION
  }
}