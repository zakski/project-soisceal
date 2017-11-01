package com.szadowsz.gospel.core.engine

import java.util
import java.util.NoSuchElementException
import java.util.concurrent.locks.{Condition, Lock, ReentrantLock}

import com.szadowsz.gospel.core.data.{Term, TermQueue}
import com.szadowsz.gospel.core.db.theory.TheoryManager
import com.szadowsz.gospel.core.engine.context.ExecutionContext
import com.szadowsz.gospel.core.engine.context.clause.ClauseInfo
import com.szadowsz.gospel.core.engine.context.subgoal.tree.SubGoalTree
import com.szadowsz.gospel.core.engine.state._
import com.szadowsz.gospel.core.error.NoMoreSolutionException
import com.szadowsz.gospel.core.{PrologEngine, Solution}

/**
  * Prolog Interpreter Executor.
  *
  * Created on 27/02/2017.
  *
  * @version Gospel 2.0.0
  */
@SerialVersionUID(1L)
class EngineRunner(val wam: PrologEngine, var id: scala.Int) extends java.io.Serializable with Runnable {
  final private[engine] val INIT = InitState(this)
  final private[engine] val GOAL_EVALUATION = GoalEvaluationState(this)
  final private[engine] val EXCEPTION = ExceptionState(this)
  final private[engine] val RULE_SELECTION = RuleSelectionState(this)
  final private[engine] val GOAL_SELECTION = GoalSelectionState(this)
  final private[engine] val BACKTRACK = BacktrackState(this)
  final private[engine] val END_FALSE = EndState(this, ExecutionResultType.FALSE)
  final private[engine] val END_TRUE = EndState(this, ExecutionResultType.TRUE)
  final private[engine] val END_TRUE_CP = EndState(this, ExecutionResultType.TRUE_CP)
  final private[engine] val END_HALT = EndState(this, ExecutionResultType.HALT)

  private lazy val theoryManager = wam.getTheoryManager
  private lazy val primitiveManager = wam.getPrimitiveManager
  private lazy val ILibraryManager = wam.getLibraryManager
  private lazy val engineManager = wam.getEngineManager
  /* Last environment used */
  private val stackEnv: util.LinkedList[Engine] = new util.LinkedList[Engine]
  var env: Engine = _
  private var pid: scala.Int = 0
  private var detached: Boolean = false
  private var solving: Boolean = false
  private var query: Term = _
  private var msgs = new TermQueue
  private var next = new util.ArrayList[Boolean]
  private var countNext: scala.Int = 0
  private var lockVar: Lock = new ReentrantLock
  private var cond: Condition = lockVar.newCondition
  private var semaphore: AnyRef = new AnyRef
  /* Current environment */
  private var last_env: Engine = _
  /* Stack environments of nidicate solving */
  private var sinfo: Solution = _


  def spy(action: String, env: Engine) {
    wam.spy(action, env)
  }

  def detach() {
    detached = true
  }

  def isDetached: Boolean = detached

  /**
    * Halts current solve computation
    */
  def solveHalt() {
    env.requestStop()
    ILibraryManager.onSolveHalt()
  }

  def identify(t: Term) {
    primitiveManager.identifyPredicate(t)
  }

  def pushSubGoal(goal: SubGoalTree) {
    env.currentContext.goalsToEval.pushSubGoal(goal)
  }

  def cut() {
    env.choicePointSelector.cut(env.currentContext.choicePointAfterCut)
  }

  def getCurrentContext: ExecutionContext = {
    if (env == null) null else env.currentContext
  }

  /**
    * Checks if the demonstration process was stopped by an halt command.
    *
    * @return true if the demonstration was stopped
    */
  def isHalted: Boolean = {
    if (sinfo == null) {
      false
    } else {
      sinfo.isHalted
    }
  }

  def run() {
    solving = true
    pid = Thread.currentThread.getId.toInt
    if (sinfo == null) {
      threadSolve()
    }
    try {
      while (hasOpenAlternatives) {
        if (next.get(countNext)) threadSolveNext()
      }
    } catch {
      case e: NoMoreSolutionException => e.printStackTrace()
    }
  }

  /**
    * Solves a query
    *
    * @param g the term representing the goal to be demonstrated
    * @return the result of the demonstration
    * @see SolveInfo
    **/
  private def threadSolve() {
    sinfo = solve
    solving = false
    lockVar.lock()
    try {
      cond.signalAll()
    } finally {
      lockVar.unlock()
    }
    if (sinfo.hasOpenAlternatives) {
      if (next.isEmpty || !next.get(countNext)) {
        semaphore synchronized {
          try {
            semaphore.wait() //Mi metto in attesa di eventuali altre richieste
          } catch {
            case e: InterruptedException => e.printStackTrace()
          }
        }
      }
    }
  }

  def solve: Solution = {
    try {
      query.resolveTerm()
      ILibraryManager.onSolveBegin(query)
      primitiveManager.identifyPredicate(query)
      freeze()
      env = new Engine(this, query)
      val result: EndState = env.run()
      defreeze()
      sinfo = new Solution(query, result.getResultGoal, result.getResultType, result.getResultVars)
      //Alberto
      env.hasOpenAlts = sinfo.hasOpenAlternatives
      if (!sinfo.hasOpenAlternatives) solveEnd()
      //Alberto
      env.nResultAsked = 0
      sinfo
    } catch {
      case ex: Exception =>
        ex.printStackTrace()
        new Solution(query)
    }
  }

  /**
    * Accepts current solution
    */
  def solveEnd() {
    ILibraryManager.onSolveEnd()
  }

  private def freeze() {
    if (env == null) return
    try {
      if (stackEnv.getLast eq env) return
    }
    catch {
      case e: NoSuchElementException => {
      }
    }
    stackEnv.addLast(env)
  }

  private def defreeze() {
    last_env = env
    if (stackEnv.isEmpty) return
    env = stackEnv.removeLast()
  }

  /**
    * Gets next solution
    *
    * @return the result of the demonstration
    * @throws NoMoreSolutionException if no more solutions are present
    * @see SolveInfo
    **/
  @throws[NoMoreSolutionException]
  private def threadSolveNext() {
    solving = true
    next.set(countNext, false)
    countNext += 1
    sinfo = solveNext
    solving = false
    lockVar.lock()
    try {
      cond.signalAll()
    } finally {
      lockVar.unlock()
    }
    if (sinfo.hasOpenAlternatives) {
      if (countNext > (next.size - 1) || !next.get(countNext)) {
        try {
          semaphore synchronized {
            semaphore.wait()
          }
        } catch {
          case e: InterruptedException =>
        }
      }
    }
  }

  @throws[NoMoreSolutionException]
  def solveNext: Solution = {
    if (hasOpenAlternatives) {
      refreeze()
      env.nextState = BACKTRACK
      val result: EndState = env.run()
      defreeze()
      sinfo = new Solution(env.query, result.getResultGoal, result.getResultType, result.getResultVars)
      //Alberto
      env.hasOpenAlts = sinfo.hasOpenAlternatives
      if (!sinfo.hasOpenAlternatives) {
        solveEnd()
      }
      //Alberto
      env.nResultAsked = env.nResultAsked + 1
      sinfo
    } else {
      throw new NoMoreSolutionException
    }
  }

  private def refreeze() {
    freeze()
    env = last_env
  }

  /**
    * Asks for the presence of open alternatives to be explored
    * in current demostration process.
    *
    * @return true if open alternatives are present
    */
  def hasOpenAlternatives: Boolean = {
    if (sinfo == null) {
      false
    } else {
      sinfo.hasOpenAlternatives
    }
  }

  def getId: scala.Int = id

  def getPid: scala.Int = pid

  def getSolution: Solution = sinfo

  def setGoal(goal: Term) {
    this.query = goal
  }

  def nextSolution: Boolean = {
    solving = true
    next.add(true)
    semaphore synchronized {
      semaphore.notify()
    }
    true
  }

  def read: Solution = {
    lockVar.lock()
    try {
      while (solving || sinfo == null) {
        try {
          cond.await()
        } catch {
          case e: InterruptedException => e.printStackTrace()
        }
      }
    } finally {
      lockVar.unlock()
    }
    sinfo
  }

  def setSolving(solved: Boolean) {
    solving = solved
  }

  def sendMsg(t: Term) {
    msgs.store(t)
  }

  def getMsg(t: Term): Boolean = {
    msgs.get(t, wam, this)
    true
  }

  def peekMsg(t: Term): Boolean = msgs.peek(t, wam)

  def removeMsg(t: Term): Boolean = msgs.remove(t, wam)

  def waitMsg(msg: Term): Boolean = {
    msgs.wait(msg, wam, this)
    true
  }

  def msgQSize: scala.Int = msgs.size

  def getTheoryManager: TheoryManager = theoryManager

  def getEngineMan: EngineManager = engineManager

  def getQuery: Term = this.query

  private[engine] def warn(message: String) {
    wam.warn(message)
  }

  private[engine] def exception(message: String) {
    wam.exception(message)
  }

  private[engine] def find(t: Term): util.List[ClauseInfo] = theoryManager.find(t)

  private[engine] def getWam: PrologEngine = wam
}