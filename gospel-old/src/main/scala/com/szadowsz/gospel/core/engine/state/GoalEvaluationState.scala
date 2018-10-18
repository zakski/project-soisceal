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
package com.szadowsz.gospel.core.engine.state

import com.szadowsz.gospel.core.data.Struct
import com.szadowsz.gospel.core.engine.{Engine, EngineRunner}
import com.szadowsz.gospel.core.error.JavaException
import com.szadowsz.gospel.core.exception.InterpreterError

/**
  * @author Alex Benini
  */
private[engine] final case class GoalEvaluationState(override protected val runner: EngineRunner) extends State {
  protected override val stateName = "Eval"

  override def doJob(e: Engine): Unit = {
    if (e.currentContext.currentGoal.isPrimitive) {
      // Recupero primitiva
      val primitive = e.currentContext.currentGoal.getPrimitive
      try {
        e.nextState = if (primitive.evalAsPredicate(e.currentContext.currentGoal)) runner.GOAL_SELECTION else runner.BACKTRACK
      } catch {
        case error: InterpreterError =>
          // sostituisco il gol in cui si ? verificato l'errore con il
          // subgoal throw/1
          e.currentContext.currentGoal = new Struct("throw", error.getError)
          e.manager.exception(error.toString)
          e.nextState = runner.EXCEPTION
        case exception: JavaException =>
          // sostituisco il gol in cui si ? verificato l'errore con il
          // subgoal java_throw/1
          e.currentContext.currentGoal = new Struct("java_throw", exception.getException)
          e.manager.exception(exception.getException.toString)
          e.nextState = runner.EXCEPTION
      }
      // Incremento il counter dei passi di dimostrazione
      e.nDemoSteps += 1
    } else {
      e.nextState = runner.RULE_SELECTION
    }
  }
}