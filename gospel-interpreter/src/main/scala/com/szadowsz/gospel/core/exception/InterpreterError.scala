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
package com.szadowsz.gospel.core.exception

import com.szadowsz.gospel.core.data.{Int, Struct, Term}
import com.szadowsz.gospel.core.engine.Executor

object InterpreterError {
  private def buildDescMsg(error: String, argNo: scala.Int, e: Executor) = {
    s"$error error in argument $argNo of ${e.currentContext.currentGoal.get}"
  }
 
  def buildEvaluationError(e : Executor, argNo : scala.Int, error : String): InterpreterError = {
    val errorTerm = new Struct("evaluation_error", new Struct(error))
    val tuPrologTerm = new Struct("evaluation_error", e.currentContext.currentGoal.get, Int(argNo), new Struct(error))
    
    val descriptionError =  buildDescMsg("Evaluation",argNo,e)
    new InterpreterError(new Struct("error", errorTerm, tuPrologTerm), descriptionError)
  }
  
  def buildInstantiationError(e: Executor, argNo: scala.Int): InterpreterError = {
    val errorTerm = new Struct("instantiation_error")
    val tuPrologTerm = new Struct("instantiation_error", e.currentContext.currentGoal.get, Int(argNo))
    
    val descriptionError = buildDescMsg("Instantiation", argNo, e)
    new InterpreterError(new Struct("error", errorTerm, tuPrologTerm), descriptionError)
  }
  
  def buildTypeError(e: Executor, argNo: scala.Int, validType: String, culprit: Term): InterpreterError = {
    val errorTerm = new Struct("type_error", new Struct(validType), culprit)
    val tuPrologTerm = new Struct("type_error", e.currentContext.currentGoal.get, Int(argNo), new Struct(validType), culprit)
    
    val descriptionError = buildDescMsg("Type", argNo, e)
    new InterpreterError(new Struct("error", errorTerm, tuPrologTerm), descriptionError)
  }
}

/**
  * Represents Prolog Exceptions
  */
class InterpreterError(protected val error: Term, message: String) extends Exception(message) {
  
  def getError: Term = error
  
  override def toString: String = getMessage
}