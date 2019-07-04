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
package com.szadowsz.gospel.core.engine

import java.util

import com.szadowsz.gospel.core.Interpreter
import com.szadowsz.gospel.core.data.{Struct, Term, Var}
import com.szadowsz.gospel.core.db.theory.clause.Clause
import com.szadowsz.gospel.core.engine.context.goal.tree.SubGoalBranch
import com.szadowsz.gospel.core.engine.context.{ChoicePointContext, ChoicePointStore, ExecutionContext}
import com.szadowsz.gospel.core.engine.state.{EndState, InitState, State}
import org.slf4j.{Logger, LoggerFactory}

/**
  * Prolog Interpreter Executor.
  */
private[core] class Executor(val query : Struct)(implicit val wam : Interpreter) {
  
  private val logger : Logger = LoggerFactory.getLogger(classOf[Executor])
  
  var startGoal: Struct = _
 
  var goalVars: util.Collection[Var] = new util.ArrayList[Var]()
  
  /**
    * The context of what state we are currently in
    */
  var currentContext: ExecutionContext = _ // Todo Convert To Option
  
  /**
    * The Next State of the Finite State Machine to Execute.
    */
  var nextState: State = new InitState
  
  /**
    * Flag to halt execution
    */
  var mustStop : Boolean = false
  
  /**
    * The number of execution steps completed
    */
  var nDemoSteps: scala.Int = 0
 
  var nResultAsked: scala.Int = 0
  var hasOpenAlts: Boolean = false
  var currentAlternative: Option[ChoicePointContext] = None
  var choicePointSelector: ChoicePointStore = _
  
  def identifyPredicate(t: Term) {
    wam.getPrimitiveManager.identifyPredicate(t)
  }
  
  /**
    * Setup the start goal and extract the variables of the query
    */
  def prepareGoal() {
    identifyPredicate(query)
    val goalVars: util.LinkedHashMap[Var, Var] = new util.LinkedHashMap[Var, Var]
    startGoal = query.copy(goalVars, 0).asInstanceOf[Struct]
    this.goalVars = goalVars.values
  }
  
  /**
    * Setup the choice point stack for the engine
    */
  def initialise(context : ExecutionContext): Unit = {
    currentContext = context
    choicePointSelector = new ChoicePointStore
    nDemoSteps = 1
    currentAlternative = None
  }
  
  def cut() {
    choicePointSelector.cut(currentContext.choicePointAfterCut)
  }
  
  /**
    * Core of engine. Finite State Machine that transitions from an initial state, to an end state.
    */
  def run(): EndState = {
    var action: String = null
    do {
      if (mustStop) {
        nextState = EndState(Result.FALSE)
      } else {
        action = nextState.toString
        nextState.doJob(this)
      }
    } while (!nextState.isInstanceOf[EndState])
    nextState.doJob(this)
    nextState.asInstanceOf[EndState]
  }
  
  def isOccursCheckEnabled(): Boolean = {
    wam.getFlagManager.isOccursCheckEnabled
  }
  
  /**
    * Checks the Theory Manager's DBs to see if we have any knowledge of a given predicate
    *
    * @param predicateIndicator the predicate identifier
    * @return true if found, false otherwise
    */
  def checkExistence(predicateIndicator: String) : Boolean = {
    wam.getTheoryManager.checkExistence(predicateIndicator)
  }
  
  /**
    * Returns a family of clauses with functor and arity equals to the functor and arity of the term passed as a
    * parameter.
    */
  def findClauses(t: Term): List[Clause] = wam.getTheoryManager.find(t)
  
  
  def pushSubGoal(goal: SubGoalBranch) {
    currentContext.goalsToEval.pushSubGoal(goal)
  }
  
  def logException(exception: Throwable): Unit ={
    logger.error("Exception Thrown During Resolution of Goal " + startGoal,exception)
  }
  
  def logWarning(msg: String): Unit ={
    logger.warn(msg)
  }
}
