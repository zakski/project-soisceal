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
package com.szadowsz.gospel.core.engine.state

import com.szadowsz.gospel.core.data.Struct
import com.szadowsz.gospel.core.engine.{Engine, EngineRunner}
import com.szadowsz.gospel.core.exception.interpreter.HaltException
import com.szadowsz.gospel.core.exception.{JVMException, PrologException}
import com.szadowsz.gospel.core.lib.PrimitiveInfo

/**
 * @author Alex Benini
 */
class GoalEvaluationState(runner: EngineRunner) extends State(runner,"Eval"){


  private def handleHaltException(halt: HaltException, theEngine: Engine): Unit = {
    theEngine.nextState = runner.END_HALT
  }

  private def handlePrologException(error: PrologException, theEngine: Engine): Unit = {
    // Replace the goals that the error occurred in with the subgoal throw/1
    theEngine.context.currentGoal = new Struct("throw", error.getError()) // TODO handle string
    theEngine.manager.exception(error.toString())
    theEngine.nextState = runner.EXCEPTION // because of this move to the exception state
  }

  private def handleJVMException(error: JVMException, theEngine: Engine): Unit = {
    // Replace the goals that the error occurred in with the subgoal java_throw/1
    theEngine.context.currentGoal = new Struct("java_throw", error.getException()) // TODO handle string
    theEngine.manager.exception(error.getException().toString)
    theEngine.nextState = runner.EXCEPTION // because of this move to the exception state
  }

  private def handleGeneralException(error: Throwable, theEngine: Engine): Unit = {
    theEngine.nextState = runner.EXCEPTION // because of this move to the exception state
  }

  private[engine] override def doJob(theEngine: Engine) {
    if (theEngine.context.currentGoal.isPrimitive) {
      val primitive: PrimitiveInfo = theEngine.context.currentGoal.getPrimitive
      try {
        val evaluated = primitive.evalAsPredicate(theEngine.context.currentGoal)
        theEngine.nextState = if (evaluated) runner.GOAL_SELECTION else runner.BACKTRACK
      }
      catch {
        case halting: HaltException => handleHaltException(halting, theEngine)
        case prolog: PrologException => handlePrologException(prolog, theEngine)
        case jvm: JVMException => handleJVMException(jvm, theEngine)
        case thrown: Throwable => handleGeneralException(thrown, theEngine)
      }
      theEngine.nDemoSteps += 1 // Increment the demonstration step counter
    } else {
      theEngine.nextState = runner.RULE_SELECTION
    }
  }
}