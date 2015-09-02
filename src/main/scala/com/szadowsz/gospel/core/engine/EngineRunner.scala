/*
 *
 *
 */
package com.szadowsz.gospel.core.engine

import com.szadowsz.gospel.util.exception.solution.NoMoreSolutionsException

import java.util.concurrent.locks.{Condition, Lock, ReentrantLock}
import java.{util => ju}

import com.szadowsz.gospel.core.Prolog
import com.szadowsz.gospel.core.data.Term
import com.szadowsz.gospel.core.data.util.TermQueue
import com.szadowsz.gospel.core.engine.context.ExecutionContext
import com.szadowsz.gospel.core.engine.state._
import com.szadowsz.gospel.core.engine.subgoal.tree.SubGoalTree
import com.szadowsz.gospel.core.lib.{LibraryManager, PrimitiveManager}
import com.szadowsz.gospel.core.theory.TheoryManager
import com.szadowsz.gospel.core.theory.clause.ClauseInfo

/**
 * @author Alex Benini
 *
 *         Core engine
 */
@SerialVersionUID(1L)
object EngineRunner {
  val HALT: scala.Int = -1
  val FALSE: scala.Int = 0
  val TRUE: scala.Int = 1
  val TRUE_CP: scala.Int = 2
}

@SerialVersionUID(1L)
class EngineRunner(vm: Prolog, id: scala.Int) extends java.io.Serializable with Runnable {
  private val mediator: Prolog = vm

  private lazy val theoryManager: TheoryManager = mediator.getTheoryManager
  private lazy val primitiveManager: PrimitiveManager = mediator.getPrimitiveManager
  private lazy val libraryManager: LibraryManager = mediator.getLibraryManager
  private lazy val engineManager: EngineManager = mediator.getEngineManager

  private var relinkVar: Boolean = false
  private var bagOFres: ju.ArrayList[Term] = null
  private var bagOFresString: ju.ArrayList[String] = null
  private var bagOFvarSet: Term = null
  private var bagOfgoal: Term = null
  private var bagOfBag: Term = null
  private val _id: scala.Int = id
  private var pid: scala.Int = 0
  private var detached: Boolean = false
  private var solving: Boolean = false
  private var query: Term = null
  private val msgs: TermQueue = new TermQueue
  private val next: ju.ArrayList[Boolean] = new ju.ArrayList[Boolean]
  private var countNext: scala.Int = 0
  private val lockVar: Lock = new ReentrantLock
  private val cond: Condition = lockVar.newCondition
  private val semaphore: AnyRef  = new AnyRef

  /* Current environment */
  private[gospel] var env: Engine = null

  /* Last environment used */
  private var last_env: Engine = null

  /* Stack environments of nidicate solving */
  private var stackEnv: ju.LinkedList[Engine] = new ju.LinkedList[Engine]

  private var sinfo: Solution = null
  private var sinfoSetOf: String = null

  /**
   * States
   */
  private[gospel] final val INIT: State = new InitState(this)
  private[gospel] final val GOAL_EVALUATION: State = new GoalEvaluationState(this)
  private[gospel] final val EXCEPTION: State = new ExceptionState(this)
  private[gospel] final val RULE_SELECTION: State = new RuleSelectionState(this)
  private[gospel] final val GOAL_SELECTION: State = new GoalSelectionState(this)
  private[gospel] final val BACKTRACK: State = new BacktrackState(this)
  private[gospel] final val END_FALSE: State = new EndState(this, EngineRunner.FALSE)
  private[gospel] final val END_TRUE: State = new EndState(this, EngineRunner.TRUE)
  private[gospel] final val END_TRUE_CP: State = new EndState(this, EngineRunner.TRUE_CP)
  private[gospel] final val END_HALT: State= new EndState(this, EngineRunner.HALT)

  private[gospel] def spy(action: String, env: Engine) {
    mediator.spy(action, env)
  }

  private[gospel] def warn(message: String) {
    mediator.warn(message)
  }

  private[gospel] def exception(message: String) {
    mediator.exception(message)
  }

  def detach {
    detached = true
  }

  def isDetached: Boolean = {
    return detached
  }

  /**
   * Solves a query
   *
   * @return the result of the demonstration
   * @see Solution
   **/
  private def threadSolve {
    sinfo = solve
    solving = false
    lockVar.lock
    try {
      cond.signalAll
    } finally {
      lockVar.unlock
    }
    if (sinfo.hasOpenAlternatives) {
      if (next.isEmpty || !next.get(countNext)) {
        semaphore synchronized {
          try {
            semaphore.wait // start waiting for any other requests
          }
          catch {
            case e: InterruptedException => {
              e.printStackTrace
            }
          }
        }
      }
    }
  }

  def solve: Solution = {
    try {
      query.resolveTerm
      libraryManager.onSolveBegin(query)
      primitiveManager.identifyPredicate(query)
      freeze
      env = new Engine(this, query)
      val result: EndState = env.run
      defreeze
      sinfo = new Solution(query, result.getResultGoal, result.getResultDemo, result.getResultVars,sinfoSetOf)
      if (!sinfo.hasOpenAlternatives) solveEnd
      return sinfo
    }
    catch {
      case ex: Exception => {
        ex.printStackTrace
        return new Solution(query)
      }
    }
  }

  /**
   * Gets next solution
   *
   * @return the result of the demonstration
   * @throws NoMoreSolutionsException if no more solutions are present
   * @see Solution
   **/
  @throws[NoMoreSolutionsException]
  private def threadSolveNext {
    solving = true
    next.set(countNext, false)
    countNext += 1
    sinfo = solveNext
    solving = false
    lockVar.lock
    try {
      cond.signalAll
    } finally {
      lockVar.unlock
    }
    if (sinfo.hasOpenAlternatives) {
      if (countNext > (next.size - 1) || !next.get(countNext)) {
        try {
          semaphore synchronized {
            semaphore.wait // start waiting for any other requests
          }
        }
        catch {
          case e: InterruptedException => {
          }
        }
      }
    }
  }

  @throws(classOf[NoMoreSolutionsException])
  def solveNext: Solution = {
    if (hasOpenAlternatives) {
      refreeze
      env.nextState = BACKTRACK
      val result: EndState = env.run
      defreeze
      sinfo = new Solution(env.query, result.getResultGoal, result.getResultDemo, result.getResultVars,sinfoSetOf)
      if (!sinfo.hasOpenAlternatives) {
        solveEnd
      }
      return sinfo
    }
    else throw new NoMoreSolutionsException
  }

  /**
   * Halts current solve computation
   */
  def solveHalt {
    env.mustStop
    libraryManager.onSolveHalt
  }

  /**
   * Accepts current solution
   */
  def solveEnd {
    libraryManager.onSolveEnd
  }

  private def freeze {
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

  private def refreeze {
    freeze
    env = last_env
  }

  private def defreeze {
    last_env = env
    if (stackEnv.isEmpty)
      return

    env = stackEnv.removeLast
  }

  private[gospel] def find(t: Term): List[ClauseInfo] = {
    return theoryManager.find(t)
  }

  private[gospel] def identify(t: Term) {
    primitiveManager.identifyPredicate(t)
  }

  private[gospel] def pushSubGoal(goals: SubGoalTree) {
    env.context.goalsToEval.pushSubGoal(goals)
  }

  private[gospel] def cut {
    env.cut(env.context.choicePointAfterCut)
  }

  private[gospel] def getCurrentContext: ExecutionContext = {
    return if ((env == null)) null else env.context
  }

  /**
   * Asks for the presence of open alternatives to be explored
   * in current demostration process.
   *
   * @return true if open alternatives are present
   */
  private[gospel] def hasOpenAlternatives: Boolean = {
    if (sinfo == null)
      return false
    return sinfo.hasOpenAlternatives
  }

  /**
   * Checks if the demonstration process was stopped by an halt command.
   *
   * @return true if the demonstration was stopped
   */
  private[gospel] def isHalted: Boolean = {
    if (sinfo == null) return false
    return sinfo.isHalted
  }

  def run {
    solving = true
    pid = Thread.currentThread.getId.toInt
    if (sinfo == null) {
      threadSolve
    }
    try {
      while (hasOpenAlternatives) if (next.get(countNext)) threadSolveNext
    }
    catch {
      case e: NoMoreSolutionsException => {
        e.printStackTrace
      }
    }
  }

  def getId: scala.Int = {
    return _id
  }

  def getPid: scala.Int = {
    return pid
  }

  def getSolution: Solution = {
    return sinfo
  }

  def setGoal(goal: Term) {
    this.query = goal
  }

  def nextSolution: Boolean = {
    solving = true
    next.add(true)
    semaphore synchronized {
      semaphore.notify
    }
    return true
  }

  def read: Solution = {
    lockVar.lock
    try {
      while (solving || sinfo == null) try {
        cond.await
      }
      catch {
        case e: InterruptedException => {
          e.printStackTrace
        }
      }
    } finally {
      lockVar.unlock
    }
    return sinfo
  }

  def setSolving(solved: Boolean) {
    solving = solved
  }

  def sendMsg(t: Term) {
    msgs.store(t)
  }

  def getMsg(t: Term): Boolean = {
    msgs.get(t, mediator, this)
    return true
  }

  def peekMsg(t: Term): Boolean = {
    return msgs.peek(t, mediator)
  }

  def removeMsg(t: Term): Boolean = {
    return msgs.remove(t, mediator)
  }

  def waitMsg(msg: Term): Boolean = {
    msgs.wait(msg, mediator, this)
    return true
  }

  def msgQSize: scala.Int = {
    return msgs.size
  }

  private[gospel] def getTheoryManager: TheoryManager = {
    return theoryManager
  }

  def getRelinkVar: Boolean = {
    return this.relinkVar
  }

  def setRelinkVar(b: Boolean) {
    this.relinkVar = b
  }

  def getBagOFres: ju.ArrayList[Term] = {
    return this.bagOFres
  }

  def setBagOFres(l: ju.ArrayList[Term]) {
    this.bagOFres = l
  }

  def getBagOFresString: ju.ArrayList[String] = {
    return this.bagOFresString
  }

  def setBagOFresString(l: ju.ArrayList[String]) {
    this.bagOFresString = l
  }

  def getBagOFvarSet: Term = {
    return this.bagOFvarSet
  }

  def setBagOFvarSet(l: Term) {
    this.bagOFvarSet = l
  }

  def getBagOFgoal: Term = {
    return this.bagOfgoal
  }

  def setBagOFgoal(l: Term) {
    this.bagOfgoal = l
  }

  def getBagOFBag: Term = {
    return this.bagOfBag
  }

  def setBagOFBag(l: Term) {
    this.bagOfBag = l
  }

  def getEngineMan: EngineManager = {
    return this.engineManager
  }

  def getSetOfSolution: String = {
    if (sinfo != null) return sinfo.getSetOfSolution
    else return null
  }

  def setSetOfSolution(s: String) {
    //if (sinfo != null) sinfo.setSetOfSolution(s)
    this.sinfoSetOf = s
  }

  def clearSinfoSetOf {
    this.sinfoSetOf = null
  }
}