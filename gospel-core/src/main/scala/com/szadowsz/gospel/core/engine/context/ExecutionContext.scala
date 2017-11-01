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
package com.szadowsz.gospel.core.engine.context

import java.util

import alice.util.OneWayList
import com.szadowsz.gospel.core.data.{Struct, Var}
import com.szadowsz.gospel.core.engine.Engine
import com.szadowsz.gospel.core.engine.context.subgoal.{SubGoalId, SubGoalStore}

/**
  *
  * @author Alex Benini
  */
final case class ExecutionContext(id: scala.Int) {
  var depth: scala.Int = 0
  var currentGoal: Struct = _
  var fatherCtx: ExecutionContext = _
  var fatherGoalId: SubGoalId = _
  var clause: Struct = _
  var headClause: Struct = _
  var goalsToEval: SubGoalStore = _
  var trailingVars: OneWayList[util.List[Var]] = _
  var fatherVarsList: OneWayList[util.List[Var]] = _
  var choicePointAfterCut: ChoicePointContext = _
  var haveAlternatives: Boolean = false

  def getId: scala.Int = id

  def getDepth: scala.Int = depth

  def getCurrentGoal: Struct = currentGoal

  def getFatherGoalId: SubGoalId = fatherGoalId

  def getClause: Struct = clause

  def getHeadClause: Struct = headClause

  def getSubGoalStore: SubGoalStore = goalsToEval

  def isHaveAlternatives: Boolean = haveAlternatives

  def getTrailingVars: util.List[util.List[Var]] = {
    val l: util.ArrayList[util.List[Var]] = new util.ArrayList[util.List[Var]]
    var t: OneWayList[util.List[Var]] = trailingVars
    while (t != null) {
      l.add(t.getHead)
      t = t.getTail
    }
    l
  }

  /**
    * Save the state of the parent context to later bring the ExectutionContext
    * objects tree in a consistent state after a backtracking step.
    */
  def saveParentState() {
    if (fatherCtx != null) {
      fatherGoalId = fatherCtx.goalsToEval.getCurrentIndex
      fatherVarsList = fatherCtx.trailingVars
    }
  }

  /**
    * If no open alternatives, no other term to execute and
    * current context doesn't contain as current goal a catch or java_catch predicate ->
    * current context no more needed ->
    * reused to execute g subgoal =>
    * got TAIL RECURSION OPTIMIZATION!
    */
  //Alberto
  def tryToPerformTailRecursionOptimization(e: Engine): Boolean = {
    if (!haveAlternatives &&
      e.getContext.goalsToEval.getCurrentID == null &&
      !e.getContext.goalsToEval.haveSubGoals &&
      !(e.getContext.currentGoal.getName.equalsIgnoreCase("catch") || e.getContext.currentGoal.getName.equalsIgnoreCase("java_catch"))) {
      fatherCtx = e.getContext.fatherCtx
      depth = e.getContext.depth
      true
    } else {
      false
    }
  }

  //Alberto
  def updateContextAndDepth(e: Engine) {
    fatherCtx = e.getContext
    depth = e.getContext.depth + 1
  }

  override def toString: String = {
    s"""|        id: $id
        |        currentGoal: $currentGoal
        |        clause: $clause
        |        subGoalStore: $goalsToEval
        |        trailingVars: $trailingVars
        |        """.stripMargin
  }
}