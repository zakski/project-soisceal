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
package com.szadowsz.gospel.core.engine.state

import com.szadowsz.gospel.core.db.theory.clause.Clause
import com.szadowsz.gospel.core.engine.Executor
import com.szadowsz.gospel.core.engine.context.ExecutionContext

/**
  * Initial finite state of demonstration.
  */
private[engine] final class InitState extends State {
  
  override protected val stateName = "Goal"
  
  /**
    * Initialises the supplied executor to prepare it for the demonstration
    * @param e the supplied Executor
    */
  override def doJob(e: Executor): Unit = {
    // Initialize executor goals based on user query
    e.prepareGoal()
   
    // Initialize first executionContext, using executor's goals
    val eCtx = ExecutionContext(0, e.query)
    eCtx.goalsToEval.load(Clause.extractBody(e.startGoal))
  
    // Supply the Executor with the first context.
    e.initialise(eCtx)
   
    // Set the future state to determining what goal to solve for first
    e.nextState = new GoalSelectionState
  }
}