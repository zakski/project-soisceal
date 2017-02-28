package com.szadowsz.gospel.core.engine

import alice.tuprolog._
import alice.tuprolog.interfaces._
import java.util
import java.util.NoSuchElementException
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

import com.szadowsz.gospel.core.engine.state._

object EngineRunner {
  val HALT: scala.Int = -1
  val FALSE: scala.Int = 0
  val TRUE: scala.Int = 1
  val TRUE_CP: scala.Int = 2
}

/**
  * Prolog Interpreter Executor.
  *
  * Created on 27/02/2017.
  *
  * @version Gospel 2.0.0
  */
@SerialVersionUID(1L)
class EngineRunner(val wam: Prolog, var id: scala.Int) extends IEngineRunner {
  final private[engine] val INIT = InitState(this)
  final private[engine] val GOAL_EVALUATION = GoalEvaluationState(this)
  final private[engine] val EXCEPTION = ExceptionState(this)
  final private[engine] val RULE_SELECTION = RuleSelectionState(this)
  final private[engine] val GOAL_SELECTION = GoalSelectionState(this)
  final private[engine] val BACKTRACK = BacktrackState(this)
  final private[engine] val END_FALSE = EndState(this, EngineRunner.FALSE)
  final private[engine] val END_TRUE = EndState(this, EngineRunner.TRUE)
  final private[engine] val END_TRUE_CP = EndState(this, EngineRunner.TRUE_CP)
  final private[engine] val END_HALT = EndState(this, EngineRunner.HALT)

  private lazy val theoryManager: ITheoryManager = wam.getTheoryManager
  private lazy val primitiveManager: IPrimitiveManager = wam.getPrimitiveManager
  private lazy val ILibraryManager: ILibraryManager = wam.getLibraryManager
  private lazy val engineManager: IEngineManager = wam.getEngineManager
  private var pid: scala.Int = 0
  private var detached: Boolean = false
  private var solving: Boolean = false
  private var query: Term = null
  private var msgs = new TermQueue
  private var next = new util.ArrayList[Boolean]
  private var countNext: scala.Int = 0
  private var lockVar: Lock = new ReentrantLock
  private var cond: Condition = lockVar.newCondition
  private var semaphore: AnyRef = new AnyRef
  var env: Engine = null /* Current environment */
  private var last_env: Engine = null /* Last environment used */
  private val stackEnv: util.LinkedList[Engine] = new util.LinkedList[Engine] /* Stack environments of nidicate solving */
  private var sinfo: SolveInfo = null


  override def spy(action: String, env: IEngine) {
    wam.spy(action, env)
  }

  private[engine] def warn(message: String) {
    wam.warn(message)
  }

  private[engine] def exception(message: String) {
    wam.exception(message)
  }

  override def detach() {
    detached = true
  }

  override def isDetached: Boolean = detached

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

  override def solve: SolveInfo = {
    try {
      query.resolveTerm()
      ILibraryManager.onSolveBegin(query)
      primitiveManager.identifyPredicate(query)
      freeze()
      env = new Engine(this, query)
      val result: EndState = env.run()
      defreeze()
      sinfo = new SolveInfo(query, result.getResultGoal, result.getResultDemo, result.getResultVars)
      //Alberto
      env.hasOpenAlts = sinfo.hasOpenAlternatives
      if (!sinfo.hasOpenAlternatives) solveEnd()
      //Alberto
      env.nResultAsked = 0
      return sinfo
    } catch {
      case ex: Exception =>
        ex.printStackTrace()
        new SolveInfo(query)
    }
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
  override def solveNext: SolveInfo = {
    if (hasOpenAlternatives) {
      refreeze()
      env.nextState = BACKTRACK
      val result: EndState = env.run()
      defreeze()
      sinfo = new SolveInfo(env.query, result.getResultGoal, result.getResultDemo, result.getResultVars)
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

  /**
    * Halts current solve computation
    */
  override def solveHalt() {
    env.requestStop()
    ILibraryManager.onSolveHalt()
  }

  /**
    * Accepts current solution
    */
  override def solveEnd() {
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

  private def refreeze() {
    freeze()
    env = last_env
  }

  private def defreeze() {
    last_env = env
    if (stackEnv.isEmpty) return
    env = stackEnv.removeLast()
  }

  private[engine] def find(t: Term): util.List[ClauseInfo] = theoryManager.find(t)

  override def identify(t: Term) {
    primitiveManager.identifyPredicate(t)
  }

  override def pushSubGoal(goals: SubGoalTree) {
    env.currentContext.goalsToEval.pushSubGoal(goals)
  }

  override def cut() {
    env.choicePointSelector.cut(env.currentContext.choicePointAfterCut)
  }

  override def getCurrentContext: ExecutionContext = {
    if (env == null) null else env.currentContext
  }

  /**
    * Asks for the presence of open alternatives to be explored
    * in current demostration process.
    *
    * @return true if open alternatives are present
    */
  override def hasOpenAlternatives: Boolean = {
    if (sinfo == null) {
      false
    } else {
      sinfo.hasOpenAlternatives
    }
  }

  /**
    * Checks if the demonstration process was stopped by an halt command.
    *
    * @return true if the demonstration was stopped
    */
  override def isHalted: Boolean = {
    if (sinfo == null) {
      false
    } else {
      sinfo.isHalted
    }
  }

  override def run() {
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

  override def getId: scala.Int = id

  override def getPid: scala.Int = pid

  override def getSolution: SolveInfo = sinfo

  override def setGoal(goal: Term) {
    this.query = goal
  }

  override def nextSolution: Boolean = {
    solving = true
    next.add(true)
    semaphore synchronized {
      semaphore.notify()
    }
    true
  }

  override def read: SolveInfo = {
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

  override def setSolving(solved: Boolean) {
    solving = solved
  }

  override def sendMsg(t: Term) {
    msgs.store(t)
  }

  override def getMsg(t: Term): Boolean = {
    msgs.get(t, wam, this)
    true
  }

  override def peekMsg(t: Term): Boolean = msgs.peek(t, wam)

  override def removeMsg(t: Term): Boolean = msgs.remove(t, wam)

  override def waitMsg(msg: Term): Boolean = {
    msgs.wait(msg, wam, this)
    true
  }

  override def msgQSize: scala.Int = msgs.size

  override def getTheoryManager: ITheoryManager = theoryManager

  override def getEngineMan: IEngineManager = this.engineManager

  private[engine] override def getMediator: Prolog = wam

  override def getQuery: Term = this.query
}