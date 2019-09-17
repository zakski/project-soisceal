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

import com.szadowsz.gospel.core.Interpreter
import com.szadowsz.gospel.core.data.{Int, Struct, Var}
import com.szadowsz.gospel.core.db.theory.Theory
import com.szadowsz.gospel.core.engine.state._
import com.szadowsz.gospel.core.parser.Parser
import org.junit.runner.RunWith
import org.scalatest.OptionValues._
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterEach, FlatSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class ExecutorSpec extends FlatSpec with Matchers with  BeforeAndAfterEach {
  
  behavior of "Executor"
  
  implicit var wam : Interpreter = _
  
  override def beforeEach(): Unit = {
    wam = new Interpreter()
  }
  
  it should "initialise correctly for a simple query" in {
    val query = new Struct("is", new Var("A"), Int(0))
    val exec = new Executor(query)
    
    exec.startGoal shouldBe null
    exec.currentContext shouldBe null
    exec.goalVars shouldBe empty
    exec.nextState shouldBe a [InitState]
   }
  
  it should "transition from InitState to GoalSelectionState correctly for a simple query" in {
    val query = new Struct("is", new Var("A"), Int(0))
    val exec = new Executor(query)
    
    exec.startGoal shouldBe null
    exec.currentContext shouldBe null
    exec.goalVars shouldBe empty
    exec.nextState shouldBe a [InitState]
  
    exec.nextState.doJob(exec) // InitState -> GoalSelectionState
  
    exec.startGoal shouldBe query
    exec.currentContext.id shouldBe 0
    exec.goalVars should have size 1
    exec.nextState shouldBe a [GoalSelectionState]
  }
  
  it should "transition from GoalSelectionState to GoalEvaluationState correctly for a simple query" in {
    val query = new Struct("is", new Var("A"), Int(0))
    val exec = new Executor(query)
  
    exec.nextState.doJob(exec) // InitState -> GoalSelectionState
  
    exec.currentContext.currentGoal shouldBe empty
    exec.nextState shouldBe a [GoalSelectionState]
  
    exec.nextState.doJob(exec) // GoalSelectionState -> GoalEvaluationState
  
    exec.currentContext.currentGoal.value shouldBe query
    exec.nextState shouldBe a [GoalEvaluationState]
  }
  
  it should "transition from GoalSelectionState to a Successful EndState correctly for a simple query" in {
    val query = new Struct("is", new Var("A"), Int(0))
    val exec = new Executor(query)
    
    exec.nextState.doJob(exec) // InitState -> GoalSelectionState
    exec.nextState.doJob(exec) // GoalSelectionState -> GoalEvaluationState
    exec.nextState.doJob(exec) // GoalEvaluationState -> GoalSelectionState
    exec.nextState shouldBe a [GoalSelectionState]
    
    exec.nextState.doJob(exec) // GoalSelectionState -> Success End State
    
    exec.nextState shouldBe EndState(Result.TRUE)
  }
  
  it should "transition from GoalEvaluationState to RuleSelectionState correctly for a simple query" in {
    wam.setTheory(new Theory("validTheory."))
    
    val query = new Struct("validTheory")
    val exec = new Executor(query)
    
    exec.nextState.doJob(exec) // InitState -> GoalSelectionState
  
    exec.nextState.doJob(exec) // GoalSelectionState -> GoalEvaluationState
    exec.nextState shouldBe a [GoalEvaluationState]
    
    exec.nextState.doJob(exec) // GoalEvaluationState -> RuleSelectionState
    
    exec.nextState shouldBe a [RuleSelectionState]
  }
  
  it should "transition from GoalEvaluationState to BacktrackState correctly for a simple query" in {
    val query = new Struct("fail_always", new Var("A"), Int(0))
    
    wam.loadLibrary("TestLibrary")
    
    val exec = new Executor(query)
  
    exec.nextState.doJob(exec) // InitState -> GoalSelectionState
    exec.nextState.doJob(exec) // GoalSelectionState -> GoalEvaluationState
  
    exec.currentContext.currentGoal.value shouldBe query
    exec.nextState shouldBe a [GoalEvaluationState]
  
    exec.nextState.doJob(exec)  // GoalEvaluationState -> BacktrackState
    exec.nextState shouldBe a [BacktrackState]
  }
  
  it should "transition from BacktrackState to GoalEvaluationState correctly for a simple query" in {
    val query = new Parser("not false.")(wam.getOperatorManager).nextTerm(true).asInstanceOf[Struct]
  
    val exec = new Executor(query)
    
     while (!exec.nextState.isInstanceOf[BacktrackState]) {
      exec.nextState.doJob(exec)
    }
    exec.nextState shouldBe a [BacktrackState]
    
    exec.nextState.doJob(exec) // should reset successfully
    exec.nextState shouldBe a [GoalEvaluationState]
  }
  
  
  it should "transition from GoalEvaluationState to ExceptionState correctly for a simple query" in {
    val query = new Struct("throw_always", new Var("A"), Int(0))
  
    wam.loadLibrary("TestLibrary")
  
    val exec = new Executor(query)
  
    exec.nextState.doJob(exec) // InitState -> GoalSelectionState
    exec.nextState.doJob(exec) // GoalSelectionState -> GoalEvaluationState
  
    exec.currentContext.currentGoal.value shouldBe query
    exec.nextState shouldBe a [GoalEvaluationState]
    
    exec.nextState.doJob(exec) // GoalEvaluationState -> ExceptionState
    exec.nextState shouldBe a [ExceptionState]
  }
  
  it should "transition from GoalEvaluationState to GoalSelectionState correctly for a simple query" in {
    val query = new Struct("is", new Var("A"), Int(0))
    val exec = new Executor(query)
  
    exec.nextState.doJob(exec) // InitState -> GoalSelectionState
    exec.nextState.doJob(exec) // GoalSelectionState -> GoalEvaluationState
    exec.nextState shouldBe a [GoalEvaluationState]

    exec.nextState.doJob(exec) // GoalEvaluationState -> GoalSelectionState
    exec.nextState shouldBe a [GoalSelectionState]
  }
  
  it should "transition from RuleSelectionState to GoalSelectionState correctly for a simple query" in {
    wam.setTheory(new Theory("validTheory."))
    
    val query = new Struct("validTheory")
    val exec = new Executor(query)
    
    exec.nextState.doJob(exec) // InitState -> GoalSelectionState
    
    exec.nextState.doJob(exec) // GoalSelectionState -> GoalEvaluationState
    exec.nextState.doJob(exec) // GoalEvaluationState -> RuleSelectionState
    exec.nextState shouldBe a [RuleSelectionState]
   
    exec.nextState.doJob(exec) // RuleSelectionState -> GoalSelectionState
    exec.nextState shouldBe a [GoalSelectionState]
  }
  
  it should "transition from BacktrackState to Failed EndState correctly for an unsatisfiable query" in {
    val query = new Struct("fail_always", new Var("A"), Int(0))
    
    val prolog = new Interpreter()
    prolog.loadLibrary("TestLibrary")
    
    val exec = new Executor(query)(prolog)
    
    exec.nextState.doJob(exec) // InitState -> GoalSelectionState
    exec.nextState.doJob(exec) // GoalSelectionState -> GoalEvaluationState
    exec.nextState.doJob(exec)  // GoalEvaluationState -> BacktrackState
    
    exec.nextState shouldBe a [BacktrackState]
  
    exec.nextState.doJob(exec)  // GoalEvaluationState -> Failed EndState
    exec.nextState shouldBe EndState(Result.FALSE)
  }
  
  it should "transition from ExceptionState to Halt EndState correctly for non-recoverable queries" in {
    val query = new Struct("throw_always", new Var("A"), Int(0))
    
    val prolog = new Interpreter()
    prolog.loadLibrary("TestLibrary")
    
    val exec = new Executor(query)(prolog)
  
    exec.nextState.doJob(exec) // InitState -> GoalSelectionState
    exec.nextState.doJob(exec) // GoalSelectionState -> GoalEvaluationState
    exec.nextState.doJob(exec)  // GoalEvaluationState -> ExceptionState
  
    exec.nextState shouldBe a [ExceptionState]
  
    exec.nextState.doJob(exec)  // ExceptionState -> Halt EndState
    exec.nextState shouldBe EndState(Result.HALT)
  }
  
  it should "transition from ExceptionState to GoalSelectionState correctly for recoverable queries" in {
    val query = "catch(X is V, error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val goal = new Parser(query)(wam.getOperatorManager).nextTerm(false).asInstanceOf[Struct]

    val exec = new Executor(goal)
  
    
    while (!exec.nextState.isInstanceOf[ExceptionState]){
      exec.nextState.doJob(exec)
    }
    exec.nextState shouldBe a [ExceptionState]
  
    exec.nextState.doJob(exec)  // ExceptionState -> GoalSelectionState
    exec.nextState shouldBe a [GoalSelectionState]
  }

  
  it should "break from GoalEvaluationState in the case of a \"Fatal\" Exception" in {
    val query = new Struct("throw_interrupt_always", new Var("A"), Int(0))
    
    val prolog = new Interpreter()
    prolog.loadLibrary("TestLibrary")
    
    val exec = new Executor(query)(prolog)
  
    exec.nextState.doJob(exec) // InitState -> GoalSelectionState
    exec.nextState.doJob(exec) // GoalSelectionState -> GoalEvaluationState
  
    exec.currentContext.currentGoal.value shouldBe query
    exec.nextState shouldBe a [GoalEvaluationState]
    
    
    intercept[InterruptedException]{
      exec.nextState.doJob(exec)  // GoalEvaluationState -> FATAL EXCEPTION
    }
  }
  
  it should "transition to A Successful ChoicePoint End State correctly when there are multiple correct answers" in {
    wam.setTheory(new Theory(
      """
        |loves(burgers).
        |loves(summer).
      """.stripMargin))
    
    val query = new Struct("loves", new Var("X"))
    val exec = new Executor(query)
  
    while (!exec.nextState.isInstanceOf[EndState]){
      exec.nextState.doJob(exec)
    }
    
    exec.nextState shouldBe EndState(Result.TRUE_CP)
  }
}
