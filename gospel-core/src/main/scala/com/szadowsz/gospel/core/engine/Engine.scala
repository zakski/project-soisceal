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
package com.szadowsz.gospel.core.engine

import alice.tuprolog.interfaces.IEngine
import java.util

import alice.tuprolog.{ChoicePointContext, ChoicePointStore, ExecutionContext, Struct, Term, Var}
import com.szadowsz.gospel.core.engine.state.{EndState, State}

import scala.util.Try
/**
  * @author Alex Benini
  */
private[engine] class Engine(val manager: EngineRunner, var query: Term) extends IEngine {
  this.manager.getTheoryManager.clearRetractDB()
  var nDemoSteps: scala.Int = 0
  var nResultAsked: scala.Int = 0
  var hasOpenAlts: Boolean = false
  var mustStop: Boolean = false
  var nextState: State = manager.INIT
  var startGoal: Struct = null
  var goalVars: util.Collection[Var] = null
  var currentContext: ExecutionContext = null
  var currentAlternative: ChoicePointContext = null
  var choicePointSelector: ChoicePointStore = null

  //Alberto
  override def getNDemoSteps: scala.Int = nDemoSteps

  //Alberto
  override def getNResultAsked: scala.Int = nResultAsked

  //Alberto
  override def hasOpenAlternatives: Boolean = hasOpenAlts

  override def toString: String = {
    Try("ExecutionStack: \n" + currentContext + "\n" + "ChoicePointStore: \n" + choicePointSelector + "\n\n").toOption.getOrElse("")
  }

  override def requestStop(): Unit = {
    mustStop = true
  }

  /**
    * Core of engine. Finite State Machine
    */
  def run(): EndState = {
    var action: String = null
    do {
          if (mustStop) {
            nextState = manager.END_FALSE
          } else {
            action = nextState.toString
            nextState.doJob(this)
            manager.spy(action, this)
          }
    } while (!nextState.isInstanceOf[EndState])
    nextState.doJob(this)
    nextState.asInstanceOf[EndState]
  }

  override def getQuery: Term = query

  override def getNumDemoSteps: scala.Int = nDemoSteps


  def getExecutionStack: util.List[ExecutionContext] = {
    val l: util.ArrayList[ExecutionContext] = new util.ArrayList[ExecutionContext]
    var t: ExecutionContext = currentContext
    while (t != null) {
      {
        l.add(t)
        t = t.fatherCtx
      }
    }
    l
  }

  override def getChoicePointStore: ChoicePointStore = choicePointSelector

  override def prepareGoal() {
    val goalVars: util.LinkedHashMap[Var, Var] = new util.LinkedHashMap[Var, Var]
    startGoal = query.copyGoal(goalVars, 0).asInstanceOf[Struct]
    this.goalVars = goalVars.values
  }

  override def initialize(eCtx: ExecutionContext) {
    currentContext = eCtx
    choicePointSelector = new ChoicePointStore
    nDemoSteps = 1
    currentAlternative = null
  }

  override def getNextStateName: String = nextState.toString

  override def getContext: ExecutionContext = currentContext
}