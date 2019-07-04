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
package com.szadowsz.gospel.core.db.libraries.inbuilt

import com.szadowsz.gospel.core.data.{Term, Var}
import com.szadowsz.gospel.core.db.libraries.{Library, predicate}
import com.szadowsz.gospel.core.engine.Executor
import com.szadowsz.gospel.core.exception.InterpreterError

import scala.util.control.NonFatal

trait BuiltInArithmetic {
  this: Library =>
  // scalastyle:off method.name
  
  @predicate(2)
  @throws(classOf[InterpreterError])
  def is_2: (Term, Term) => Boolean = {
    (arg0: Term, arg1: Term) =>
     val e = implicitly[Executor]
      arg1.getBinding match {
        case v: Var => throw InterpreterError.buildInstantiationError(e, 2)
        case _ =>
          var val1: Option[Term] = None
          try {
            val1 = evalExpression(arg1)
          } catch {
            case cause: ArithmeticException if cause.getMessage == "/ by zero" =>
              throw InterpreterError.buildEvaluationError(e,2, "zero_divisor")
            case NonFatal(_) =>
          }
          val1 match {
            case None => throw InterpreterError.buildTypeError(e,2, "evaluable", arg1.getBinding)
            case Some(result) => arg0.getBinding.unify(result)
          }
      }
  }
}
