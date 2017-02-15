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
package com.szadowsz.gospel.core.engine

import com.szadowsz.gospel.core.engine.clause.ClauseStore

import java.util._

import com.szadowsz.gospel.core.data.{Struct, Term, Var}
import com.szadowsz.gospel.core.engine.context.{ChoiceContext, ExecutionContext, GoalContext}
import com.szadowsz.gospel.core.engine.state.State
import com.szadowsz.gospel.util.LoggerCategory
import org.slf4j.LoggerFactory

/**
 * @author Alex Benini
 */
class Engine(runner: EngineRunner, q: Term)  {
  private lazy val _logger = LoggerFactory.getLogger(LoggerCategory.ENGINE)

  private var _mustStop: Boolean = false

  private[gospel] var nextState: State = runner.INIT
  private[gospel] var query: Term  = q
  private[gospel] var startGoal: Struct = null
  private[gospel] var goalVars: Collection[Var] = null
  private[gospel] var nDemoSteps: scala.Int = 0
  private[gospel] var context: ExecutionContext = null
  private[gospel] var currentAlternative: ChoiceContext = null
  private[gospel] var choicePoint: ChoiceContext = null
  private[gospel] val manager: EngineRunner = runner

  this.manager.getTheoryManager.clearRetractDB


  override def toString: String = {
    val builder = new StringBuilder()
    builder.append("ExecutionStack: ")
    builder.append((if (context != null) "\n" + context else "Not Initialised"))

    if(choicePoint != null)
      builder.append("ChoicePointStore: \n" + choicePoint)

    builder.toString()
  }

  private[gospel] def mustStop() {
    _mustStop = true
  }

  /**
   * Core of engine. Finite State Machine
   */
  private[gospel] def run: state.EndState = {
    var action: String = null
    do {
      _logger.debug("ExecutionStack: " + (if (context != null) "\n" + context else "Not Initialised"))
      if (_mustStop) {
        nextState = manager.END_FALSE
      } else {
        action = nextState.toString
        nextState.doJob(this)
        manager.spy(action, this)
      }
    } while (!_mustStop && !nextState.isInstanceOf[state.EndState])

    nextState.doJob(this)
    nextState.asInstanceOf[state.EndState]
  }


  def getExecutionStack: List[ExecutionContext] = {
    val l: ArrayList[ExecutionContext] = new ArrayList[ExecutionContext]
    var t: ExecutionContext = context
    while (t != null) {
      l.add(t)
      t = t.fatherCtx
    }
    return l
  }

  private[gospel] def prepareGoal() = {
    val goals = new LinkedHashMap[Var, Var]
    startGoal = query.copy(goals, nDemoSteps).asInstanceOf[Struct]
    goalVars = goals.values
    new GoalContext(query.asInstanceOf[Struct], startGoal)
  }

  def getNextStateName: String = {
    return nextState.toString
  }

  private[engine] def incrementDemo(): Int = {
    nDemoSteps += 1
    nDemoSteps - 1
  }

  /**
   * Return the correct choice-point
   */
  private[engine] def fetchChoicePoint: ChoiceContext = {
    if (hasChoicePoint) choicePoint else null
  }

  private[engine] def cut(pointerAfterCut: ChoiceContext) {
    choicePoint = pointerAfterCut
  }

  private[engine] def addChoicePoint(theGoals: ClauseStore) {
    choicePoint = new ChoiceContext(context, theGoals, choicePoint)
  }

  /**
   * Check if a choice point exists in the store and removes choice points
   * which have been already used and are now empty.
   * @return true is we have a valid choiceContext, false otherwise
   */
  private[engine] def hasChoicePoint: Boolean = {
    if (choicePoint == null)
      false
    else {
      choicePoint = choicePoint.backtrack().orNull
      choicePoint != null
    }
  }

  private[engine] def popAlternative(): ChoiceContext = {
    val alt = currentAlternative
    currentAlternative = null
    alt
  }
}