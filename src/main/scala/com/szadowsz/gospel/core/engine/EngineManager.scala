package com.szadowsz.gospel.core.engine

import com.szadowsz.gospel.util.exception.solution.NoMoreSolutionsException

import java.util.concurrent.locks.ReentrantLock
import java.util.{ArrayList, Hashtable, Iterator, LinkedHashMap, Set}

import com.szadowsz.gospel.core.Prolog
import com.szadowsz.gospel.core.data.util.TermQueue
import com.szadowsz.gospel.core.data.{Term, Var, numeric}
import com.szadowsz.gospel.core.engine.context.ExecutionContext
import com.szadowsz.gospel.core.engine.subgoal.tree.SubGoalTree

@SerialVersionUID(1L)
class EngineManager(m: Prolog) extends java.io.Serializable {
  private val vm: Prolog = m
  private var runners = new Hashtable[Integer, EngineRunner]
  private var threads = new Hashtable[Integer, Integer]
  private val rootID:  scala.Int = 0
  private val er1: EngineRunner= new EngineRunner(vm,rootID)
  private var id:  scala.Int = 0
  private var queues: Hashtable[String, TermQueue] = new Hashtable[String, TermQueue]
  private var locks: Hashtable[String, ReentrantLock] = new Hashtable[String, ReentrantLock]

   def threadCreate(threadID: Term, g: Term): Boolean = {
    id = id + 1
    var goal = g
    if (goal == null)
      return false

    if (goal.isInstanceOf[Var])
      goal = goal.getTerm

    val er: EngineRunner = new EngineRunner(vm,id)
    if (!vm.unify(threadID, new numeric.Int(id))) return false
    er.setGoal(goal)
    addRunner(er, id)
    val t: Thread = new Thread(er)
    addThread(t.getId, id)
    t.start
    return true
  }

  def join(id:  scala.Int): Solution = {
    val er: EngineRunner = findRunner(id)
    if (er == null || er.isDetached) return null
    val solution: Solution = er.read
    removeRunner(id)
    return solution
  }

  def read(id:  scala.Int): Solution = {
    val er: EngineRunner = findRunner(id)
    if (er == null || er.isDetached) return null
    val solution: Solution = er.read
    return solution
  }

  def hasNext(id:  scala.Int): Boolean = {
    val er: EngineRunner = findRunner(id)
    if (er == null || er.isDetached) return false
    return er.hasOpenAlternatives
  }

  def nextSolution(id:  scala.Int): Boolean = {
    val er: EngineRunner = findRunner(id)
    if (er == null || er.isDetached) return false
    val bool: Boolean = er.nextSolution
    return bool
  }

  def detach(id:  scala.Int) {
    val er: EngineRunner = findRunner(id)
    if (er == null) return
    er.detach
  }

  def sendMsg(dest:  scala.Int, msg: Term): Boolean = {
    val er: EngineRunner = findRunner(dest)
    if (er == null) return false
    val msgcopy: Term = msg.copy(new LinkedHashMap[Var, Var], 0)
    er.sendMsg(msgcopy)
    return true
  }

  def sendMsg(name: String, msg: Term): Boolean = {
    val queue: TermQueue = queues.get(name)
    if (queue == null) return false
    val msgcopy: Term = msg.copy(new LinkedHashMap[Var, Var], 0)
    queue.store(msgcopy)
    return true
  }

  def getMsg(id:  scala.Int, msg: Term): Boolean = {
    val er: EngineRunner = findRunner(id)
    if (er == null) return false
    return er.getMsg(msg)
  }

  def getMsg(name: String, msg: Term): Boolean = {
    val er: EngineRunner = findRunner
    if (er == null) return false
    val queue: TermQueue = queues.get(name)
    if (queue == null) return false
    return queue.get(msg, vm, er)
  }

  def waitMsg(id:  scala.Int, msg: Term): Boolean = {
    val er: EngineRunner = findRunner(id)
    if (er == null) return false
    return er.waitMsg(msg)
  }

  def waitMsg(name: String, msg: Term): Boolean = {
    val er: EngineRunner = findRunner
    if (er == null) return false
    val queue: TermQueue = queues.get(name)
    if (queue == null) return false
    return queue.wait(msg, vm, er)
  }

  def peekMsg(id:  scala.Int, msg: Term): Boolean = {
    val er: EngineRunner = findRunner(id)
    if (er == null) return false
    return er.peekMsg(msg)
  }

  def peekMsg(name: String, msg: Term): Boolean = {
    val queue: TermQueue = queues.get(name)
    if (queue == null) return false
    return queue.peek(msg, vm)
  }

  def removeMsg(id:  scala.Int, msg: Term): Boolean = {
    val er: EngineRunner = findRunner(id)
    if (er == null) return false
    return er.removeMsg(msg)
  }

  def removeMsg(name: String, msg: Term): Boolean = {
    val queue: TermQueue = queues.get(name)
    if (queue == null) return false
    return queue.remove(msg, vm)
  }

  private def removeRunner(id:  scala.Int) {
    val er: EngineRunner = runners.get(id)
    if (er == null) return
    runners synchronized {
      runners.remove(id)
    }
    val pid:  scala.Int = er.getPid
    threads synchronized {
      threads.remove(pid)
    }
  }

  private def addRunner(er: EngineRunner, id:  scala.Int) {
    runners synchronized {
      runners.put(id, er)
    }
  }

  private def addThread(pid: Long, id:  scala.Int) {
    threads synchronized {
      threads.put(pid.toInt, id)
    }
  }

  private[gospel] def cut(): Unit = {
    findRunner.cut
  }

  private[gospel] def getCurrentContext: ExecutionContext = {
    val runner: EngineRunner = findRunner
    return runner.getCurrentContext
  }

  private[gospel] def hasOpenAlternatives: Boolean = {
    val runner: EngineRunner = findRunner
    return runner.hasOpenAlternatives
  }

  private[gospel] def isHalted: Boolean = {
    val runner: EngineRunner = findRunner
    return runner.isHalted
  }

  private[gospel] def pushSubGoal(goals: SubGoalTree) {
    val runner: EngineRunner = findRunner
    runner.pushSubGoal(goals)
  }

  def solve(query: Term): Solution = {
    synchronized{
      this.clearSinfoSetOf
      er1.setGoal(query)
      val s: Solution = er1.solve
      return s
    }
  }

  def solveEnd {
    er1.solveEnd
    if (runners.size != 0) {
      val ers = runners.elements
      while (ers.hasMoreElements) {
        val current: EngineRunner = ers.nextElement
        current.solveEnd
      }
      runners = new Hashtable[Integer, EngineRunner]
      threads = new Hashtable[Integer, Integer]
      queues = new Hashtable[String, TermQueue]
      locks = new Hashtable[String, ReentrantLock]
      id = 0
    }
  }

  def solveHalt {
    er1.solveHalt
    if (runners.size != 0) {
      val ers = runners.elements
      while (ers.hasMoreElements) {
        val current: EngineRunner = ers.nextElement
        current.solveHalt
      }
    }
  }

  @throws(classOf[NoMoreSolutionsException])
  def solveNext: Solution = {
    synchronized {
      return er1.solveNext
    }
  }

  private[gospel] def spy(action: String, env: Engine) {
    val runner: EngineRunner = findRunner
    runner.spy(action, env)
  }

  /**
   *
   * @return The EngineRunner associated with the thread of the specified id .
   *
   */
  private def findRunner(id:  scala.Int): EngineRunner = {
    if (!runners.containsKey(id)) return null
    runners synchronized {
      return runners.get(id)
    }
  }

  private def findRunner: EngineRunner = {
    val pid:  scala.Int = Thread.currentThread.getId.toInt
    if (!threads.containsKey(pid)) return er1
    threads synchronized {
      runners synchronized {
        val id:  scala.Int = threads.get(pid)
        return runners.get(id)
      }
    }
  }

  // Returns the identifier of the current thread
  def runnerId:  scala.Int = {
    val er: EngineRunner = findRunner
    return er.getId
  }

  def createQueue(name: String): Boolean = {
    queues synchronized {
      if (queues.containsKey(name)) return true
      val newQ: TermQueue = new TermQueue
      queues.put(name, newQ)
    }
    return true
  }

  def destroyQueue(name: String) {
    queues synchronized {
      queues.remove(name)
    }
  }

  def queueSize(id:  scala.Int):  scala.Int = {
    val er: EngineRunner = findRunner(id)
    return er.msgQSize
  }

  def queueSize(name: String):  scala.Int = {
    val q: TermQueue = queues.get(name)
    if (q == null) return -1
    return q.size
  }

  def createLock(name: String): Boolean = {
    locks synchronized {
      if (locks.containsKey(name)) return true
      val mutex: ReentrantLock = new ReentrantLock
      locks.put(name, mutex)
    }
    return true
  }

  def destroyLock(name: String) {
    locks synchronized {
      locks.remove(name)
    }
  }

  def mutexLock(name: String): Boolean = {
    val mutex: ReentrantLock = locks.get(name)
    if (mutex == null) {
      createLock(name)
      return mutexLock(name)
    }
    mutex.lock
    return true
  }

  def mutexTryLock(name: String): Boolean = {
    val mutex: ReentrantLock = locks.get(name)
    if (mutex == null) return false
    return mutex.tryLock
  }

  def mutexUnlock(name: String): Boolean = {
    val mutex: ReentrantLock = locks.get(name)
    if (mutex == null) return false
    try {
      mutex.unlock
      return true
    }
    catch {
      case e: IllegalMonitorStateException => {
        return false
      }
    }
  }

  def isLocked(name: String): Boolean = {
    val mutex: ReentrantLock = locks.get(name)
    if (mutex == null) return false
    return mutex.isLocked
  }

  def unlockAll {
    locks synchronized {
      val mutexList: Set[String] = locks.keySet
      val it: Iterator[String] = mutexList.iterator
      while (it.hasNext) {
        val mutex: ReentrantLock = locks.get(it.next)
        var unlocked: Boolean = false
        while (!unlocked) {
          try {
            mutex.unlock
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

  private[gospel] def getEnv: Engine = {
    val er: EngineRunner = findRunner
    return er.env
  }

  def identify(t: Term) {
    val er: EngineRunner = findRunner
    er.identify(t)
  }

  def getRelinkVar: Boolean = {
    val r: EngineRunner = this.findRunner
    return r.getRelinkVar
  }

  def setRelinkVar(b: Boolean) {
    val r: EngineRunner = this.findRunner
    r.setRelinkVar(b)
  }

  def getBagOFres: ArrayList[Term] = {
    val r: EngineRunner = this.findRunner
    return r.getBagOFres
  }

  def setBagOFres(l: ArrayList[Term]) {
    val r: EngineRunner = this.findRunner
    r.setBagOFres(l)
  }

  def getBagOFresString: ArrayList[String] = {
    val r: EngineRunner = this.findRunner
    return r.getBagOFresString
  }

  def setBagOFresString(l: ArrayList[String]) {
    val r: EngineRunner = this.findRunner
    r.setBagOFresString(l)
  }

  def getBagOFvarSet: Term = {
    val r: EngineRunner = this.findRunner
    return r.getBagOFvarSet
  }

  def setBagOFvarSet(l: Term) {
    val r: EngineRunner = this.findRunner
    r.setBagOFvarSet(l)
  }

  def getBagOFgoal: Term = {
    val r: EngineRunner = this.findRunner
    return r.getBagOFgoal
  }

  def setBagOFgoal(l: Term) {
    val r: EngineRunner = this.findRunner
    r.setBagOFgoal(l)
  }

  def getBagOFbag: Term = {
    val r: EngineRunner = this.findRunner
    return r.getBagOFBag
  }

  def setBagOFbag(l: Term) {
    val r: EngineRunner = this.findRunner
    r.setBagOFBag(l)
  }

  def getSetOfSolution: String = {
    val r: EngineRunner = this.findRunner
    return r.getSetOfSolution
  }

  def setSetOfSolution(s: String) {
    val r: EngineRunner = this.findRunner
    r.setSetOfSolution(s)
  }

  def clearSinfoSetOf {
    val r: EngineRunner = this.findRunner
    r.clearSinfoSetOf
  }
}