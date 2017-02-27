package com.szadowsz.gospel.core.engine

import alice.tuprolog.json.AbstractEngineState
import java.util
import java.util.concurrent.locks.ReentrantLock

import alice.tuprolog.{Engine, ExecutionContext, NoMoreSolutionException, SolveInfo, SubGoalTree, Term, TermQueue, Var,Int}
import alice.tuprolog.interfaces.IEngineManager
import com.szadowsz.gospel.core.PrologEngine

/**
  * Prolog Interpreter Execution Handler.
  *
  * Created on 27/02/2017.
  *
  * @version Gospel 2.0.0
  */
@SerialVersionUID(1L)
final case class EngineManager(private val wam: PrologEngine ) extends IEngineManager {
  private var runners = new util.Hashtable[Integer, EngineRunner]
  //key: id;  obj: runner
  private var threads = new util.Hashtable[Integer, Integer]
  //key: pid; obj: id
  private val rootID: scala.Int = 0
  private var er1: EngineRunner = new EngineRunner(wam,rootID)
  private var id: scala.Int = 0
  private var queues = new util.Hashtable[String, TermQueue]
  private var locks = new util.Hashtable[String, ReentrantLock]


  override def threadCreate(threadID: Term, goal: Term): Boolean = {
    id = id + 1
    if (goal == null) {
      false
    } else {
      val goalTerm = if (goal.isInstanceOf[Var]) {
        goal.getTerm
      } else {
        goal
      }
      val er: EngineRunner = new EngineRunner(wam,id)
      if (!wam.unify(threadID, new Int(id))) return false
      er.setGoal(goalTerm)
      addRunner(er, id)
      val t: Thread = new Thread(er)
      addThread(t.getId, id)
      t.start()
      true
    }
  }

  override def join(id: scala.Int): SolveInfo = {
    val er: EngineRunner = findRunner(id)
    if (er == null || er.isDetached) {
      null
    } else {
      val solution: SolveInfo = er.read
      removeRunner(id)
      solution
    }
  }

  override def read(id: scala.Int): SolveInfo = {
    val er: EngineRunner = findRunner(id)
    if (er == null || er.isDetached) {
      null
    } else {
      val solution: SolveInfo = er.read
      solution
    }
  }

  override def hasNext(id: scala.Int): Boolean = {
    val er: EngineRunner = findRunner(id)
    if (er == null || er.isDetached) {
      false
    } else {
      er.hasOpenAlternatives
    }
  }

  override def nextSolution(id: scala.Int): Boolean = {
    val er: EngineRunner = findRunner(id)
    if (er == null || er.isDetached) {
      false
    } else {
      val bool: Boolean = er.nextSolution
      bool
    }
  }

  override def detach(id: scala.Int) {
    val er: EngineRunner = findRunner(id)
    if (er != null) {
      er.detach()
    }
  }

  override def sendMsg(dest: scala.Int, msg: Term): Boolean = {
    val er: EngineRunner = findRunner(dest)
    if (er == null) {
      false
    } else {
      val msgcopy: Term = msg.copy(new util.LinkedHashMap[Var, Var], 0)
      er.sendMsg(msgcopy)
      true
    }
  }

  override def sendMsg(name: String, msg: Term): Boolean = {
    val queue: TermQueue = queues.get(name)
    if (queue == null) {
      false
    } else {
      val msgcopy: Term = msg.copy(new util.LinkedHashMap[Var, Var], 0)
      queue.store(msgcopy)
      true
    }
  }

  override def getMsg(id: scala.Int, msg: Term): Boolean = {
    val er: EngineRunner = findRunner(id)
    if (er == null) {
      false
    } else {
      er.getMsg(msg)
    }
  }

  override def getMsg(name: String, msg: Term): Boolean = {
    val er: EngineRunner = findRunner
    if (er == null) {
      false
    } else {
      val queue: TermQueue = queues.get(name)
      if (queue == null) {
        false
      } else {
        queue.get(msg, wam, er)
      }
    }
  }

  override def waitMsg(id: scala.Int, msg: Term): Boolean = {
    val er: EngineRunner = findRunner(id)
    if (er == null) {
      false
    } else {
      er.waitMsg(msg)
    }
  }

  override def waitMsg(name: String, msg: Term): Boolean = {
    val er: EngineRunner = findRunner
    if (er == null) {
      false
    } else {
      val queue: TermQueue = queues.get(name)
      if (queue == null) return false
      queue.wait(msg, wam, er)
    }
  }

  override def peekMsg(id: scala.Int, msg: Term): Boolean = {
    val er: EngineRunner = findRunner(id)
    if (er == null) return false
    er.peekMsg(msg)
  }

  override def peekMsg(name: String, msg: Term): Boolean = {
    val queue: TermQueue = queues.get(name)
    if (queue == null) return false
    queue.peek(msg, wam)
  }

  override def removeMsg(id: scala.Int, msg: Term): Boolean = {
    val er: EngineRunner = findRunner(id)
    if (er == null) return false
    er.removeMsg(msg)
  }

  override def removeMsg(name: String, msg: Term): Boolean = {
    val queue: TermQueue = queues.get(name)
    if (queue == null) return false
    queue.remove(msg, wam)
  }

  private def removeRunner(id: scala.Int) {
    val er: EngineRunner = runners.get(id)
    if (er == null) return
    runners synchronized {
      runners.remove(id)
    }
    val pid: scala.Int = er.getPid
    threads synchronized {
      threads.remove(pid)
    }
  }

  private def addRunner(er: EngineRunner, id: scala.Int) {
    runners synchronized {
      runners.put(id, er)
    }
  }

  private def addThread(pid: scala.Long, id: scala.Int) {
    threads synchronized {
      threads.put(pid.toInt, id)
    }
  }

  override def cut() {
    findRunner.cut()
  }

  override def getCurrentContext: ExecutionContext = {
    val runner: EngineRunner = findRunner
    runner.getCurrentContext
  }

  private[core] def hasOpenAlternatives: Boolean = {
    val runner: EngineRunner = findRunner
    runner.hasOpenAlternatives
  }

  private[core] def isHalted: Boolean = {
    val runner: EngineRunner = findRunner
    runner.isHalted
  }

  override def pushSubGoal(goals: SubGoalTree) {
    val runner: EngineRunner = findRunner
    runner.pushSubGoal(goals)
  }

  override def solve(query: Term): SolveInfo = {
    er1.setGoal(query)
    val s: SolveInfo = er1.solve
    s
  }

  override def solveEnd() {
    er1.solveEnd()
    if (runners.size != 0) {
      val ers: util.Enumeration[EngineRunner] = runners.elements
      while (ers.hasMoreElements) {
        {
          val current: EngineRunner = ers.nextElement
          current.solveEnd()
        }
      }
      runners = new util.Hashtable[Integer, EngineRunner]
      threads = new util.Hashtable[Integer, Integer]
      queues = new util.Hashtable[String, TermQueue]
      locks = new util.Hashtable[String, ReentrantLock]
      id = 0
    }
  }

  override def solveHalt() {
    er1.solveHalt()
    if (runners.size != 0) {
      val ers: util.Enumeration[EngineRunner] = runners.elements
      while (ers.hasMoreElements) {
        {
          val current: EngineRunner = ers.nextElement
          current.solveHalt()
        }
      }
    }
  }

  @throws[NoMoreSolutionException]
  override def solveNext: SolveInfo = {
    er1.solveNext
  }

  private[engine] def spy(action: String, env: Engine) {
    val runner: EngineRunner = findRunner
    runner.spy(action, env)
  }

  /**
    *
    * @return L'EngineRunner associato al thread di id specificato.
    *
    */
  private def findRunner(id: scala.Int): EngineRunner = {
    if (!runners.containsKey(id)) return null
    runners synchronized {
      runners.get(id)
    }
  }

  private def findRunner: EngineRunner = {
    val pid: scala.Int = Thread.currentThread.getId.toInt
    if (!threads.containsKey(pid)) return er1
    threads synchronized {
      runners synchronized {
        val id: scala.Int = threads.get(pid)
        runners.get(id)
      }
    }
  }

  //Ritorna l'identificativo del thread corrente
  override def runnerId: scala.Int = {
    val er: EngineRunner = findRunner
    er.getId
  }

  override def createQueue(name: String): Boolean = {
    queues synchronized {
      if (queues.containsKey(name)) return true
      val newQ: TermQueue = new TermQueue
      queues.put(name, newQ)
    }
    true
  }

  override def destroyQueue(name: String) {
    queues synchronized {
      queues.remove(name)
    }
  }

  override def queueSize(id: scala.Int): scala.Int = {
    val er: EngineRunner = findRunner(id)
    er.msgQSize
  }

  override def queueSize(name: String): scala.Int = {
    val q: TermQueue = queues.get(name)
    if (q == null) return -1
    q.size
  }

  override def createLock(name: String): Boolean = {
    locks synchronized {
      if (locks.containsKey(name)) return true
      val mutex: ReentrantLock = new ReentrantLock
      locks.put(name, mutex)
    }
    true
  }

  override def destroyLock(name: String) {
    locks synchronized {
      locks.remove(name)
    }
  }

  override def mutexLock(name: String): Boolean = {
    val mutex: ReentrantLock = locks.get(name)
    if (mutex == null) {
      createLock(name)
      return mutexLock(name)
    }
    mutex.lock()
    true
  }

  override def mutexTryLock(name: String): Boolean = {
    val mutex: ReentrantLock = locks.get(name)
    if (mutex == null) return false
    mutex.tryLock
  }

  override def mutexUnlock(name: String): Boolean = {
    val mutex: ReentrantLock = locks.get(name)
    if (mutex == null) return false
    try {
      mutex.unlock()
      true
    }
    catch {
      case e: IllegalMonitorStateException => {
        false
      }
    }
  }

  override def isLocked(name: String): Boolean = {
    val mutex: ReentrantLock = locks.get(name)
    if (mutex == null) return false
    mutex.isLocked
  }

  override def unlockAll() {
    locks synchronized {
      val mutexList: util.Set[String] = locks.keySet
      val it: util.Iterator[String] = mutexList.iterator
      while (it.hasNext) {
        {
          val mutex: ReentrantLock = locks.get(it.next)
          var unlocked: Boolean = false
          while (!unlocked) {
            {
              try {
                mutex.unlock()
              }
              catch {
                case e: IllegalMonitorStateException => {
                  unlocked = true
                }
              }
            }
          }
        }
      }
    }
  }

  override def getEnv: Engine = {
    val er: EngineRunner = findRunner
    er.env
  }

  override def identify(t: Term) {
    val er: EngineRunner = findRunner
    er.identify(t)
  }

  //Alberto
  override def serializeQueryState(brain: AbstractEngineState) {
    brain.setQuery(findRunner.getQuery)
    if (findRunner.env == null) {
      brain.setNumberAskedResults(0)
      brain.setHasOpenAlternatives(false)
    }
    else {
      brain.setNumberAskedResults(findRunner.env.getNResultAsked)
      brain.setHasOpenAlternatives(findRunner.env.hasOpenAlternatives)
    }
  }
}