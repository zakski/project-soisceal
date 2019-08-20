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

import java.util

import com.szadowsz.gospel.core.data.{Struct, Var}
import com.szadowsz.gospel.core.engine.Executor
import com.szadowsz.gospel.core.engine.context.goal.SubGoalStore
import com.szadowsz.gospel.core.engine.context.{ChoicePointContext, ClauseStore, ExecutionContext}

class RuleSelectionState extends State {
  /**
    * the name of the engine state.
    */
  override protected val stateName: String = "RuleInit"
  
  override def doJob(e: Executor): Unit = {
    /*----------------------------------------------------
 * Individuo compatibleGoals e
 * stabilisco se derivo da Backtracking.
 */
    val goal: Struct = e.currentContext.currentGoal.get
    val (alternative, clauseStore, fromBacktracking) = getClauseStore(e, goal)
    
    if (!clauseStore.existCompatibleClause) {
      e.nextState = new BacktrackState
    } else {
      // choose one of the potentially compatible rules
      val clauseInfo = clauseStore.fetch
      
      // build ExecutionContext and ChoicePointContext
      e.nDemoSteps += 1
      val ec: ExecutionContext = ExecutionContext(e.nDemoSteps - 1, clauseInfo.clause)
      val curCtx: ExecutionContext = e.currentContext
      
      // copy head and body, refreshing variables
      val clauseCopy = clauseInfo.performCopy(e,ec.id)
      ec.headClause = Option(clauseCopy.head)
      ec.goalsToEval = new SubGoalStore()
      ec.goalsToEval.load(clauseCopy.body)
      
      /** The following block encodes cut functionalities, and hardcodes the special treatment that ISO Standard
        * reserves for goal disjunction: section 7.8.6.1 prescribes that ;/2 must be transparent to cut.
        */
      ec.choicePointAfterCut = e.choicePointSelector.getPointer
      alternative.foreach(choicePoint => handleGoalDisjunction(choicePoint, ec)) // handle the choice if one exists
  
      unifyGoal(e, clauseStore, fromBacktracking, ec, curCtx)
      //Alberto
      if (!ec.tryToPerformTailRecursionOptimization(e)) ec.updateContextAndDepth(e)
      ec.saveParentState()
      e.currentContext = ec
      e.nextState = new GoalSelectionState
    }
  }
  
  private def unifyGoal(e: Executor, clauseStore: ClauseStore, fromBacktracking: Boolean, ec: ExecutionContext, curCtx: ExecutionContext): Unit = {
    val curGoal = curCtx.currentGoal.get
    val unifiedVars: util.List[Var] = e.currentContext.trailingVars.head
    curGoal.unify(unifiedVars, unifiedVars, ec.headClause.get, e.isOccursCheckEnabled())
    ec.haveAlternatives = clauseStore.hasAlternatives()
   
    // create a choice point if there are alternatives and we didn't backtrack
    if (ec.haveAlternatives && !fromBacktracking) {
      e.choicePointSelector.add(new ChoicePointContext(clauseStore,curCtx, curCtx.goalsToEval.getCurrentIndex, e.currentContext.trailingVars))
      
    } else if (!ec.haveAlternatives && fromBacktracking) {
      e.choicePointSelector.removeUnusedChoicePoints()
    }
  }
  
  private def findDisjunctionGoal(choicePoint: ChoicePointContext, ec: ExecutionContext, currentGoal: Option[Struct], depth: Int): Unit = {
    currentGoal match {
      case None => // Finished
      case Some(goal) =>
        if (choicePoint.prevContext.isDefined) {
          val distance: Int = depth - choicePoint.prevContext.map(prev => prev.execContext.depth).getOrElse(0)
          var nextChoicePoint = choicePoint
          while (distance == 0 && choicePoint.prevContext.isDefined) {
            ec.choicePointAfterCut = choicePoint.prevContext.get.prevContext
            nextChoicePoint = choicePoint.prevContext.get
          }
          if (distance == 1 && choicePoint.prevContext.isDefined) {
            ec.choicePointAfterCut = choicePoint.prevContext.get.prevContext
            val nextGoal = choicePoint.prevContext.get.execContext.currentGoal.orNull
            nextChoicePoint = choicePoint.prevContext.get
            if (nextGoal.getName != ";" || nextGoal.getArity != 2) {
              findDisjunctionGoal(nextChoicePoint, ec, Option(nextGoal), depth)
            }
          }
        }
    }
  }
  
  
  private def handleGoalDisjunction(choicePoint: ChoicePointContext, ec: ExecutionContext): Unit = {
    val depth: Int = choicePoint.execContext.depth
    ec.choicePointAfterCut = choicePoint.prevContext
    val currentGoal = choicePoint.execContext.currentGoal
    
    findDisjunctionGoal(choicePoint, ec, currentGoal, depth)
  }
  
  private def getClauseStore(e: Executor, goal: Struct): (Option[ChoicePointContext], ClauseStore, Boolean) = {
    val alternative = e.currentAlternative
    e.currentAlternative = None // pop current alternative in order to consume it
    alternative match {
      case Some(alt) => (alternative, alt.compatibleGoals, true) // backtracking
      case None => // from normal evaluation
        val varsList: util.List[Var] = new util.ArrayList[Var]
        e.currentContext.trailingVars = varsList +: e.currentContext.trailingVars
        val clauseStore = new ClauseStore(goal, varsList, e.findClauses(goal))
        (alternative, clauseStore, false)
    }
  }
}
