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
package com.szadowsz.gospel.core.engine.context

import com.szadowsz.gospel.core.engine.clause.ClauseStore

/**
 * Class to represent a decision point in the Prolog Demonstration where we possibly could process alternatives.
 *
 * Merged ChoicePointContext and ChoicePoint Store into one class, with Engine taking on a couple of functions.
 *
 * @author Zak Szadowski
 */
private[engine] class ChoiceContext(theExecCtx: ExecutionContext, theGoals: ClauseStore, thePrev: ChoiceContext) {


  val executionContext = theExecCtx
  val compatibleGoals = theGoals
  val previous = thePrev
  val indexSubGoal = theExecCtx.goalsToEval.getCurrentIndex
  val varsToDeunify = theExecCtx.trailingVars

  /**
   * Used to remove choice points which have been already used and are now empty.
   *
   * @return a ChoiceContext with a compatible alternative clause
   */
  def backtrack(): Option[ChoiceContext] = {
    if (compatibleGoals.existCompatibleClause)
      Some(this)
    else if (previous == null)
      None
    else {
      previous.backtrack()
    }
  }

  /**
   * Method to describe the ChoiceContext stack
   *
   * @return a list of ChoiceContexts, starts with the latest
   */
  def toList: List[ChoiceContext] = {
    val list = List[ChoiceContext](this)
    if (previous != null){
      list ::: previous.toList
    } else {
      list
    }
  }

  /**
   * String Representation of the ChoiceContext
   *
   * TODO output a more sensible string
   *
   * @return ChoiceContext as a String
   */
  override def toString: String = {
    "     ChoicePointId: " + executionContext.id + ":" + indexSubGoal + "\n" + "     compGoals:     " + compatibleGoals + "\n"
  }
}