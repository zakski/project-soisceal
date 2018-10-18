/**
  * tuProlog - Copyright (C) 2001-2002  aliCE team at deis.unibo.it
  *
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

import java.util
import java.util.concurrent.locks.ReentrantLock

import com.szadowsz.gospel.core.data.{Int, Term, Var}
import com.szadowsz.gospel.core.{PrologEngine, Solution}
import com.szadowsz.gospel.core.engine.context.ExecutionContext
import com.szadowsz.gospel.core.engine.context.subgoal.tree.SubGoalTree
import com.szadowsz.gospel.core.json.EngineState

/**
  * Prolog Interpreter Execution Handler.
  *
  * Created on 27/02/2017.
  *
  * @version Gospel 2.0.0
  */
@SerialVersionUID(1L)
final case class EngineManager(private val wam: PrologEngine) extends java.io.Serializable {
  private var runners = new util.Hashtable[Integer, EngineRunner]
  //key: id;  obj: runner
  private var threads = new util.Hashtable[Integer, Integer]
  //key: pid; obj: id
  private val rootID = 0
  private var er1 = new EngineRunner(wam, rootID)
  private var id = 0
  private var queues = new util.Hashtable[String, TermQueue]
  private var locks = new util.Hashtable[String, ReentrantLock]


  def threadCreate(threadID: Term, goal: Term): Boolean = {
    id = id + 1
    if (goal == null) false else {
      val goalTerm = if (goal.isInstanceOf[Var]) goal.getTerm else goal
      val er = new EngineRunner(wam, id)
      if (!wam.unify(threadID, new Int(id))) return false
      er.setGoal(goalTerm)
      addRunner(er, id)
      val t = new Thread(er)
      addThread(t.getId, id)
      t.start()
      true
    }
  }

  def join(id: scala.Int): Solution = {
    val er = findRunner(id)
    if (er == null || er.isDetached) null else {
      val solution = er.read
      removeRunner(id)
      solution
    }
  }

  def read(id: scala.Int): Solution = {
    val er = findRunner(id)
    if (er == null || er.isDetached) null else {
      val solution = er.read
      solution
    }
  }

  def hasNext(id: scala.Int): Boolean = {
    val er = findRunner(id)
    if (er == null || er.isDetached) false else er.hasOpenAlternatives
  }

  def nextSolution(id: scala.Int): Boolean = {
    val er = findRunner(id)
    if (er == null || er.isDetached) false else {
      val bool = er.nextSolution
      bool
    }
  }

  def detach(id: scala.Int) {
    val er = findRunner(id)
    if (er != null) er.detach()
  }

  def sendMsg(dest: scala.Int, msg: Term): Boolean = {
    val er = findRunner(dest)
    if (er == null) false else {
      val msgcopy = msg.copy(new util.LinkedHashMap[Var, Var], 0)
      er.sendMsg(msgcopy)
      true
    }
  }

  def sendMsg(name: String, msg: Term): Boolean = {
    val queue = queues.get(name)
    if (queue == null) false else {
      val msgcopy = msg.copy(new util.LinkedHashMap[Var, Var], 0)
      queue.store(msgcopy)
      true
    }
  }

  def getMsg(id: scala.Int, msg: Term): Boolean = {
    val er = findRunner(id)
    if (er == null) false else er.getMsg(msg)
  }

  def getMsg(name: String, msg: Term): Boolean = {
    val er = findRunner
    if (er == null) false else {
      val queue = queues.get(name)
      if (queue == null) false else queue.get(msg, wam, er)
    }
  }

  def waitMsg(id: scala.Int, msg: Term): Boolean = {
    val er = findRunner(id)
    if (er == null) false else er.waitMsg(msg)
  }

  def waitMsg(name: String, msg: Term): Boolean = {
    val er = findRunner
    if (er == null) false else {
      val queue = queues.get(name)
      if (queue == null) return false
      queue.wait(msg, wam, er)
    }
  }

  def peekMsg(id: scala.Int, msg: Term): Boolean = {
    val er = findRunner(id)
    if (er == null) return false
    er.peekMsg(msg)
  }

  def peekMsg(name: String, msg: Term): Boolean = {
    val queue = queues.get(name)
    if (queue == null) return false
    queue.peek(msg, wam)
  }

  def removeMsg(id: scala.Int, msg: Term): Boolean = {
    val er = findRunner(id)
    if (er == null) return false
    er.removeMsg(msg)
  }

  def removeMsg(name: String, msg: Term): Boolean = {
    val queue = queues.get(name)
    if (queue == null) return false
    queue.remove(msg, wam)
  }

  private def removeRunner(id: scala.Int) {
    val er = runners.get(id)
    if (er == null) return
    runners synchronized {
      runners.remove(id)
    }
    val pid = er.getPid
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

  def cut() {
    findRunner.cut()
  }

  def getCurrentContext: ExecutionContext = {
    val runner = findRunner
    runner.getCurrentContext
  }

  private[core] def hasOpenAlternatives = {
    val runner = findRunner
    runner.hasOpenAlternatives
  }

  private[core] def isHalted = {
    val runner = findRunner
    runner.isHalted
  }

  def pushSubGoal(goals: SubGoalTree) {
    val runner = findRunner
    runner.pushSubGoal(goals)
  }

  def solve(query: Term): Solution = {
    er1.setGoal(query)
    val s = er1.solve
    s
  }

  def solveEnd() {
    er1.solveEnd()
    if (runners.size != 0) {
      val ers = runners.elements
      while (ers.hasMoreElements) {
        val current = ers.nextElement
        current.solveEnd()
      }
      runners = new util.Hashtable[Integer, EngineRunner]
      threads = new util.Hashtable[Integer, Integer]
      queues = new util.Hashtable[String, TermQueue]
      locks = new util.Hashtable[String, ReentrantLock]
      id = 0
    }
  }

  def solveHalt() {
    er1.solveHalt()
    if (runners.size != 0) {
      val ers = runners.elements
      while (ers.hasMoreElements) {
        val current = ers.nextElement
        current.solveHalt()
      }
    }
  }

  def solveNext: Solution = er1.solveNext

  /**
    *
    * @return L'EngineRunner associato al thread di id specificato.
    *
    */
  private def findRunner(id: scala.Int) : EngineRunner = {
    if (!runners.containsKey(id)) return null
    runners synchronized {
      runners.get(id)
    }
  }

  private def findRunner : EngineRunner  = {
    val pid = Thread.currentThread.getId.toInt
    if (!threads.containsKey(pid)) return er1
    threads synchronized {
      runners synchronized {
        val id = threads.get(pid)
        runners.get(id)
      }
    }
  }

  //Ritorna l'identificativo del thread corrente
  def runnerId: scala.Int = {
    val er = findRunner
    er.getId
  }

  def createQueue(name: String): Boolean = {
    queues synchronized {
      if (queues.containsKey(name)) return true
      val newQ = new TermQueue
      queues.put(name, newQ)
    }
    true
  }

  def destroyQueue(name: String) {
    queues synchronized {
      queues.remove(name)
    }
  }

  def queueSize(id: scala.Int): scala.Int = {
    val er = findRunner(id)
    er.msgQSize
  }

  def queueSize(name: String): scala.Int = {
    val q = queues.get(name)
    if (q == null) return -1
    q.size
  }

  def createLock(name: String): Boolean = {
    locks synchronized {
      if (locks.containsKey(name)) return true
      val mutex = new ReentrantLock
      locks.put(name, mutex)
    }
    true
  }

  def destroyLock(name: String) {
    locks synchronized {
      locks.remove(name)
    }
  }

  def mutexLock(name: String): Boolean = {
    val mutex = locks.get(name)
    if (mutex == null) {
      createLock(name)
      return mutexLock(name)
    }
    mutex.lock()
    true
  }

  def mutexTryLock(name: String): Boolean = {
    val mutex = locks.get(name)
    if (mutex == null) return false
    mutex.tryLock
  }

  def mutexUnlock(name: String): Boolean = {
    val mutex = locks.get(name)
    if (mutex == null) return false
    try {
      mutex.unlock()
      true
    } catch {
      case e: IllegalMonitorStateException => false
    }
  }

  def isLocked(name: String): Boolean = {
    val mutex = locks.get(name)
    if (mutex == null) return false
    mutex.isLocked
  }

  def unlockAll() {
    locks synchronized {
      val mutexList = locks.keySet
      val it = mutexList.iterator
      while (it.hasNext) {
        val mutex = locks.get(it.next)
        var unlocked = false
        while (!unlocked) try
          mutex.unlock()

        catch {
          case e: IllegalMonitorStateException => {
            unlocked = true
          }
        }
      }
    }
  }

  def getEnv: Engine = {
    val er = findRunner
    er.env
  }

  def identify(t: Term) {
    val er = findRunner
    er.identify(t)
  }

  //Alberto
  def serializeQueryState(brain: EngineState) {
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

  def spy(action: String, env: Engine): Unit = wam.spy(action, env)
}