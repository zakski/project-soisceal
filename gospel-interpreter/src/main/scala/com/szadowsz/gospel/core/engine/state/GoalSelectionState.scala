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

import com.szadowsz.gospel.core.data.{Struct, Term}
import com.szadowsz.gospel.core.engine.context.ExecutionContext
import com.szadowsz.gospel.core.engine.{Executor, Result}

class GoalSelectionState extends State {
 
  
  override protected val stateName: String = "Call"
  
  /**
    * Selects a goal for the supplied executor to prepare to resolve
    * @param e the supplied Executor
    */
  override def doJob(e: Executor): Unit = {
    var curGoal: Option[Term] = None
    var continue = true
    while (continue && curGoal.isEmpty) {
      curGoal = e.currentContext.goalsToEval.fetch()
      curGoal match {
         case Some(goal) => // Goal Identification Case
          val goalToProcess: Term = goal.getBinding
          goalToProcess match {
            case struct: Struct =>
  
              /**
                * Code inserted to allow evaluation of meta-clause such as p(X) :- X. When evaluating directly terms,
                * they are converted to execution of a call/1 predicate.
                *
                * This enables the dynamic linking of built-ins for terms coming from outside the demonstration context.
                */
              if (goal ne goalToProcess) {
                e.currentContext.currentGoal = Some(new Struct("call", goalToProcess))
              } else {
                e.currentContext.currentGoal = Some(struct)
              }
              e.nextState = new GoalEvaluationState
              continue = false
            case _ =>
              e.nextState = EndState(Result.FALSE)
              continue = false
          }
         case None =>
           // Terminate The Demonstration if we can no longer backtrack
           if (e.currentContext.parent.isEmpty) {
    
             // Determine Endstate based on existence of further ChoicePoints
             e.nextState = if (e.choicePointSelector.findValidChoice.isDefined) {
               EndState(Result.TRUE_CP)
             } else {
               EndState(Result.TRUE)
             }
             continue = false
           } else {
             e.currentContext = e.currentContext.parent.get // Return to the parent context
           }
      }
    }
    logContextState(e.currentContext)
  }
  
  private def logContextState(context: ExecutionContext) = {
    var cur = context
    logger.debug("Goal Stack:")
    var list : List[String] = Nil
    while (cur != null){
      list = list :+ cur.currentGoal.map(_.toString).getOrElse("None")
      cur = cur.parent.orNull
    }
    
    list.reverse.zipWithIndex.foreach{ case (g, i) =>
      val spaces : String = Array.fill(i*3)(' ').mkString
      logger.debug(spaces + g)// + s" :(${cur.haveAlternatives})")
    }
  }
}
