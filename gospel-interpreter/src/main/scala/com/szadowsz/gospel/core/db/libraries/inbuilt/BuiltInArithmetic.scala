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

import com.szadowsz.gospel.core.data.{Float, Int, Number, Term, Var}
import com.szadowsz.gospel.core.db.libraries.{Library, functor, predicate}
import com.szadowsz.gospel.core.exception.InterpreterError

import scala.util.control.NonFatal

trait BuiltInArithmetic {
  this: Library =>
  // scalastyle:off method.name
  
  def getArithmeticTheoryString: String = {
    """
      |
      |'=\='(X,Y):- not X =:= Y.
      |
    """.stripMargin
  }
  
  @functor(0)
  def pi_0: () => Term = {
    () => Float(Math.PI)
  }
  
  @throws[InterpreterError]
  @predicate(2, true, "=:=")
  def expression_equality_2: (Term, Term) => Boolean = {
    (arg0, arg1) => {
      val e = arg0.getExecutor
      arg0.getBinding match {
        case v: Var => throw InterpreterError.buildInstantiationError(e, 1);
        case _ =>
          arg1.getBinding match {
            case v: Var => throw InterpreterError.buildInstantiationError(e, 2);
            case _ =>
              val val0 = evalExpression(arg0)
              val val1 = evalExpression(arg1)
              val0 match {
                case Some(n0: Number) =>
                  val1 match {
                    case Some(n1: Number) =>
                      if (n0.isInteger && n1.isInteger) {
                        n0.longValue == n1.longValue
                      } else {
                        n0.doubleValue == n1.doubleValue
                      }
                    case _ => throw InterpreterError.buildTypeError(e, 2, "evaluable", arg0.getBinding);
                  }
                case _ => throw InterpreterError.buildTypeError(e, 1, "evaluable", arg0.getBinding);
              }
          }
      }
    }
  }
  
  @throws[InterpreterError]
  @predicate(2, true, "<")
  def expression_less_than_2: (Term, Term) => Boolean = {
    (arg0, arg1) => {
      val e = arg0.getExecutor
      arg0.getBinding match {
        case v: Var => throw InterpreterError.buildInstantiationError(e, 1);
        case _ =>
          arg1.getBinding match {
            case v: Var => throw InterpreterError.buildInstantiationError(e, 2);
            case _ =>
              val val0 = evalExpression(arg0)
              val val1 = evalExpression(arg1)
              val0 match {
                case Some(n0: Number) =>
                  val1 match {
                    case Some(n1: Number) =>
                      if (n0.isInteger && n1.isInteger) {
                        n0.longValue < n1.longValue
                      } else {
                        n0.doubleValue < n1.doubleValue
                      }
                  }
              }
          }
      }
    }
  }
  
  @predicate(2, true, "=<")
  def expression_less_or_equal_than_2: (Term, Term) => Boolean = {
    (arg0, arg1) => {
      val e = arg0.getExecutor
      arg0.getBinding match {
        case v: Var => throw InterpreterError.buildInstantiationError(e, 1);
        case _ =>
          arg1.getBinding match {
            case v: Var => throw InterpreterError.buildInstantiationError(e, 2);
            case _ =>
              val val0 = evalExpression(arg0)
              val val1 = evalExpression(arg1)
              val0 match {
                case Some(n0: Number) =>
                  val1 match {
                    case Some(n1: Number) =>
                      if (n0.isInteger && n1.isInteger) {
                        n0.longValue <= n1.longValue
                      } else {
                        n0.doubleValue <= n1.doubleValue
                      }
                  }
              }
          }
      }
    }
  }
  
  @throws[InterpreterError]
  @predicate(2, true, ">")
  def expression_greater_than_2: (Term, Term) => Boolean = {
    (arg0, arg1) => {
      val e = arg0.getExecutor
      arg0.getBinding match {
        case v: Var => throw InterpreterError.buildInstantiationError(e, 1);
        case _ =>
          arg1.getBinding match {
            case v: Var => throw InterpreterError.buildInstantiationError(e, 2);
            case _ =>
              val val0 = evalExpression(arg0)
              val val1 = evalExpression(arg1)
              val0 match {
                case Some(n0: Number) =>
                  val1 match {
                    case Some(n1: Number) =>
                      if (n0.isInteger && n1.isInteger) {
                        n0.longValue > n1.longValue
                      } else {
                        n0.doubleValue > n1.doubleValue
                      }
                  }
              }
          }
      }
    }
  }
  
  @predicate(2, true, ">=")
  def expression_greater_or_equal_than_2: (Term, Term) => Boolean = {
    (arg0, arg1) => {
      val e = arg0.getExecutor
      arg0.getBinding match {
        case v: Var => throw InterpreterError.buildInstantiationError(e, 1);
        case _ =>
          arg1.getBinding match {
            case v: Var => throw InterpreterError.buildInstantiationError(e, 2);
            case _ =>
              val val0 = evalExpression(arg0)
              val val1 = evalExpression(arg1)
              val0 match {
                case Some(n0: Number) =>
                  val1 match {
                    case Some(n1: Number) =>
                      if (n0.isInteger && n1.isInteger) {
                        n0.longValue >= n1.longValue
                      } else {
                        n0.doubleValue >= n1.doubleValue
                      }
                  }
              }
          }
      }
    }
  }
  
  @predicate(2)
  @throws(classOf[InterpreterError])
  def is_2: (Term, Term) => Boolean = {
    (arg0: Term, arg1: Term) =>
     val e = arg0.getExecutor
      arg1.getBinding match {
        case v: Var => throw InterpreterError.buildInstantiationError(e, 2)
        case _ =>
          var val1: Option[Term] = None
          try {
            val1 = evalExpression(arg1)
          } catch {
            case cause: ArithmeticException if cause.getMessage == "/ by zero" =>
              throw InterpreterError.buildEvaluationError(e, 2, "zero_divisor")
            case NonFatal(_) =>
          }
          val1 match {
            case None => throw InterpreterError.buildTypeError(e, 2, "evaluable", arg1.getBinding)
            case Some(result) => arg0.getBinding.unify(result)
          }
      }
    }
  
  @functor(2, true,"+")
  def expression_plus_2: (Term, Term) => Term = {
    (arg0: Term, arg1: Term) => {
      val val0 = evalExpression(arg0)
      val val1 = evalExpression(arg1)
      
      val0 match {
        case Some(n0: Number) =>
          val1 match {
            case Some(n1: Number) =>
              if (n0.isInteger && n1.isInteger) {
                Int(Math.addExact(n0.longValue,n1.longValue))
              } else {
                Float(n0.doubleValue + n1.doubleValue)
              }
            case _ => null
          }
        case _ => null
      }
    }
  }
  
  @functor(2, true,"*")
  def expression_multiply_2: (Term, Term) => Term = {
    (arg0: Term, arg1: Term) => {
      val val0 = evalExpression(arg0)
      val val1 = evalExpression(arg1)
      
      val0 match {
        case Some(n0: Number) =>
          val1 match {
            case Some(n1: Number) =>
              if (n0.isInteger && n1.isInteger) {
                Int(Math.multiplyExact(n0.longValue,n1.longValue))
              } else {
                Float(n0.doubleValue * n1.doubleValue)
              }
            case _ => null
          }
        case _ => null
      }
    }
  }
  
  @functor(2, true,"/")
  def expression_div_2: (Term, Term) => Term = {
    (arg0: Term, arg1: Term) => {
      val val0 = evalExpression(arg0)
      val val1 = evalExpression(arg1)
      
      val0 match {
        case Some(n0: Number) =>
          val1 match {
            case Some(n1: Number) =>
              if (n0.isInteger && n1.isInteger) {
                Int(n0.longValue / n1.longValue)
              } else {
                Float(n0.doubleValue / n1.doubleValue)
              }
            case _ => null
          }
        case _ => null
      }
    }
  }
  
  @functor(1, true,"-")
  def expression_minus_1: Term => Term = {
    (arg0: Term) => {
      evalExpression(arg0) match {
        case Some(n: Int) => Int(n.value * -1)
        case Some(n: Float) => Float(n.value * -1)
        case _ => null
      }
    }
  }
  
  @functor(2,true, "-")
  def expression_minus_2: (Term, Term) => Term = {
    (arg0: Term, arg1: Term) => {
      val val0 = evalExpression(arg0)
      val val1 = evalExpression(arg1)
      
      val0 match {
        case Some(n0: Number) =>
          val1 match {
            case Some(n1: Number) =>
              if (n0.isInteger && n1.isInteger) {
                Int(Math.subtractExact(n0.longValue,n1.longValue))
              } else {
                Float(n0.doubleValue - n1.doubleValue)
              }
            case _ => null
          }
        case _ => null
      }
    }
  }
  
  @functor(2)
  def mod_2: (Term, Term) => Term = {
    (arg0: Term, arg1: Term) => {
      val val0 = evalExpression(arg0)
      val val1 = evalExpression(arg1)
      
      val0 match {
        case Some(n0: Number) =>
          val1 match {
            case Some(n1: Number) =>
              if (n0.isInteger && n1.isInteger) {
                Int(n0.longValue - Math.floor(n0.doubleValue / n1.doubleValue).toLong * n1.longValue)
              } else {
                Float(n0.doubleValue - Math.floor(n0.doubleValue / n1.doubleValue) * n1.doubleValue)
              }
            case _ => null
          }
        case _ => null
      }
    }
  }
  
  @functor(2)
  def rem_2: (Term, Term) => Term = {
    (arg0: Term, arg1: Term) => {
      val val0 = evalExpression(arg0)
      val val1 = evalExpression(arg1)
      
      val0 match {
        case Some(n0: Number) =>
          val1 match {
            case Some(n1: Number) =>
              if (n0.isInteger && n1.isInteger) {
                Int(n0.longValue % n1.longValue)
              } else {
                Float(Math.IEEEremainder(n0.doubleValue, n1.doubleValue))
              }
            case _ => null
          }
        case _ => null
      }
    }
  }
  
  @functor(1)
  def floor_1: (Term) => Term = {
    (arg0: Term) => {
      val val0 = evalExpression(arg0)
      val0 match {
        case Some(n0: Number) =>
          Int(Math.floor(n0.doubleValue).toLong)
        case _ => null
      }
    }
  }
  
  @functor(1)
  def round_1: (Term) => Term = {
    (arg0: Term) => {
      val val0 = evalExpression(arg0)
      val0 match {
        case Some(n0: Number) =>
          Int(Math.round(n0.doubleValue))
        case _ => null
      }
    }
  }
  
  @functor(1)
  def ceiling_1: (Term) => Term = {
    (arg0: Term) => {
      val val0 = evalExpression(arg0)
      val0 match {
        case Some(n0: Number) =>
          Int(Math.ceil(n0.doubleValue).toLong)
        case _ => null
      }
    }
  }
  
  @functor(1)
  def truncate_1: (Term) => Term = {
    (arg0: Term) => {
      val val0 = evalExpression(arg0)
      val0 match {
        case Some(n0: Number) =>
          if (n0.doubleValue >= 0) {
            Int(Math.floor(n0.doubleValue).toLong)
          } else {
            Int(Math.ceil(n0.doubleValue).toLong)
          }
        case _ => null
      }
    }
  }
  
  @functor(1)
  def float_1: (Term) => Term = {
    (arg0: Term) => {
      val val0 = evalExpression(arg0)
      val0 match {
        case Some(n0: Number) =>
          Float(n0.doubleValue)
        case _ => null
      }
    }
  }
  
  @functor(1)
  def abs_1: (Term) => Term = {
    (arg0: Term) => {
      val val0 = evalExpression(arg0)
      val0 match {
        case Some(i0: Int) =>
          Int(Math.abs(i0.value))
        case Some(f0: Float) =>
          Float(Math.abs(f0.value))
        case _ => null
      }
    }
  }
  
  @functor(2, true,"**")
  def expression_pow_2: (Term, Term) => Term = {
    (arg0: Term, arg1: Term) => {
      val val0 = evalExpression(arg0)
      val val1 = evalExpression(arg1)
      
      val0 match {
        case Some(n0: Number) =>
          val1 match {
            case Some(n1: Number) =>
              Float(Math.pow(n0.doubleValue, n1.doubleValue))
            case _ => null
          }
        case _ => null
      }
    }
  }
  
  @functor(1)
  def sin_1: (Term) => Term = {
    (arg0: Term) => {
      val val0 = evalExpression(arg0)
      val0 match {
        case Some(n0: Number) => Float(Math.sin(n0.doubleValue))
        case _ => null
      }
    }
  }
  
  @functor(1)
  def cos_1: (Term) => Term = {
    (arg0: Term) => {
      val val0 = evalExpression(arg0)
      val0 match {
        case Some(n0: Number) => Float(Math.cos(n0.doubleValue))
        case _ => null
      }
    }
  }
  
  @functor(1)
  def atan_1: (Term) => Term = {
    (arg0: Term) => {
      val val0 = evalExpression(arg0)
      val0 match {
        case Some(n0: Number) => Float(Math.atan(n0.doubleValue))
        case _ => null
      }
    }
  }
  
  @functor(1)
  def exp_1: (Term) => Term = {
    (arg0: Term) => {
      val val0 = evalExpression(arg0)
      val0 match {
        case Some(n0: Number) => Float(Math.exp(n0.doubleValue))
        case _ => null
      }
    }
  }
  
  @functor(1)
  def log_1: (Term) => Term = {
    (arg0: Term) => {
      val val0 = evalExpression(arg0)
      val0 match {
        case Some(n0: Number) => Float(Math.log(n0.doubleValue))
        case _ => null
      }
    }
  }
  
  @functor(1)
  def sqrt_1: (Term) => Term = {
    (arg0: Term) => {
      val val0 = evalExpression(arg0)
      val0 match {
        case Some(n0: Number) => Float(Math.sqrt(n0.doubleValue))
        case _ => null
      }
    }
  }
}
