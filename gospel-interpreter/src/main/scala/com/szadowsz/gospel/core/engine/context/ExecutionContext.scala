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
package com.szadowsz.gospel.core.engine.context

import com.szadowsz.gospel.core.data.{Struct, Var}
import com.szadowsz.gospel.core.engine.context.goal.{SubGoalId, SubGoalStore}
import java.util

import com.szadowsz.gospel.core.engine.Executor

private[engine] final case class ExecutionContext(id: scala.Int, clause: Struct) {
  
  var parent: Option[ExecutionContext] = None
  var depth: scala.Int = 0
  var haveAlternatives: Boolean = false
  var goalsToEval: SubGoalStore = new SubGoalStore()
  var currentGoal: Option[Struct] = None
  var trailingVars: List[util.List[Var]] = Nil
  var choicePointAfterCut: Option[ChoicePointContext] = None
  var headClause: Option[Struct] = None
  
  var parentGoalId: Option[SubGoalId] = None
  var parentVarList: Option[List[util.List[Var]]] = None
  
  /**
    * If no open alternatives, no other term to execute and
    * current context doesn't contain as current goal a catch or java_catch predicate ->
    * current context no more needed ->
    * reused to execute g subgoal =>
    * got TAIL RECURSION OPTIMIZATION!
    */
  def tryToPerformTailRecursionOptimization(e: Executor): Boolean = {
    if (!haveAlternatives &&
      e.currentContext.goalsToEval.getCurrentID == null &&
      !e.currentContext.goalsToEval.haveSubGoals &&
      !e.currentContext.currentGoal.exists(g => g.getName.equalsIgnoreCase("catch") || g.getName.equalsIgnoreCase("java_catch"))) {
      
      parent = e.currentContext.parent
      depth = e.currentContext.depth
      true
    } else {
      false
    }
  }
  
  def updateContextAndDepth(e: Executor) {
    parent = Option(e.currentContext)
    depth = e.currentContext.depth + 1
  }
  
  /**
    * Save the state of the parent context to later bring the ExecutionContext objects tree in a consistent state after 
    * a backtracking step.
    */
  def saveParentState() {
    parent match {
      case Some(father) =>
        parentGoalId = Option(father.goalsToEval.getCurrentIndex)
        parentVarList = Option(father.trailingVars)
      
      case None =>
    }
  }
}
