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
package alice.tuprolog.core.engine.context

import java.util._

import alice.tuprolog.core.data.{Struct, Var}
import alice.tuprolog.core.engine.Engine
import alice.tuprolog.core.engine.subgoal.SubGoalStore
import alice.tuprolog.core.theory.clause.ClauseInfo
import alice.util.OneWayList

/**
 *
 * @author Alex Benini
 */
private[tuprolog] class ExecutionContext (theEngine: Engine, theClause: ClauseInfo, hasAlts: Boolean) {

  private[core] val id = theEngine.incrementDemo()
  private[core] val fatherCtx = doTailRecursionOptimization(theEngine,hasAlts)
  private[core] val depth : Int = if (fatherCtx == null) 0 else fatherCtx.depth+1

  private[engine] val clause = theClause.getClause
  private[engine] val headClause = theClause.getHeadCopy
  private[engine] val goalsToEval = new SubGoalStore(theClause.getBodyCopy)
  private[engine] val choicePointAfterCut = checkForCut(theEngine.choicePoint, theEngine.popAlternative())
  private[engine] val hasAlternatives: Boolean = hasAlts

  private[tuprolog] var currentGoal: Struct = null
  private[tuprolog] var trailingVars: OneWayList[List[Var]] = null

  private[engine] val fatherGoalId = if (fatherCtx != null) fatherCtx.goalsToEval.getCurrentIndex else null
  private[engine] val fatherVarsList = if (fatherCtx != null) fatherCtx.trailingVars else null


  def this(theEngine: Engine, theClause: ClauseInfo) {
    this(theEngine, theClause, false)
  }


  /** The following block encodes cut functionalities, and hardcodes the
    * special treatment that ISO Standard reserves for goal disjunction:
    * section 7.8.6.1 prescribes that ;/2 must be transparent to cut.
    */
  private def checkForCut(theDefaultCut: ChoiceContext, theAlt: ChoiceContext): ChoiceContext = {
    if (theAlt == null) {
      theDefaultCut
    } else {
      checkForCut(theAlt, theAlt.executionContext.currentGoal)
    }
  }

  private def checkForCut(theAlt: ChoiceContext, theGoal: Struct): ChoiceContext = {
    val choiceAfterCut = theAlt.previous
    val depth = theAlt.executionContext.depth

    if (choiceAfterCut != null && theGoal.getName == ";" && theGoal.getArity == 2) {
      val distance = depth - choiceAfterCut.executionContext.depth

      if (distance == 0 && choiceAfterCut != null) {
        checkForCut(choiceAfterCut, theGoal)
      } else if (distance == 1 && choiceAfterCut != null) {
        checkForCut(choiceAfterCut, choiceAfterCut.executionContext.currentGoal)
      } else {
        choiceAfterCut
      }
    } else {
      choiceAfterCut
    }
  }

  /**
   * If no open alternatives, no other term to execute and
   * current context doesn't contain as current goal a catch or java_catch predicate ->
   * current context no more needed ->
   * reused to execute g subgoal =>
   * got TAIL RECURSION OPTIMIZATION!
   */
  private[engine] def doTailRecursionOptimization(theEngine : Engine, hasAlts: Boolean):ExecutionContext = {
    val context = theEngine.context
    if (context != null){
      if (!hasAlts && context.goalsToEval.getCurrentID == null
        && !context.goalsToEval.haveSubGoals
        && !(context.currentGoal.getName.equalsIgnoreCase("catch")
        || context.currentGoal.getName.equalsIgnoreCase("java_catch"))) {

        context.fatherCtx

      } else {
        context
      }
    } else {
      null
    }
  }

  def getCurrentGoal: Struct = currentGoal

  def getClause: Struct = clause

  def getSubGoalStore: SubGoalStore = goalsToEval

  override def toString: String = {
    val builder = new StringBuilder
    builder.append("     id: " + id + "\n")
    builder.append("     currentGoal:  " + currentGoal + "\n")
    builder.append("     clause:       " + clause + "\n")
    builder.append("     subGoalStore: " + goalsToEval + "\n")
    builder.append("     trailingVars: " + trailingVars)
    builder.toString()
  }
}