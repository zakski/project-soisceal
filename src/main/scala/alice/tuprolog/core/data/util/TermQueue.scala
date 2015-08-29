package alice.tuprolog.core.data.util


import java.{util => ju}

import alice.tuprolog.core.Prolog
import alice.tuprolog.core.data.Term
import alice.tuprolog.core.engine.EngineRunner

class TermQueue {
  private val _queue = new ju.LinkedList[Term]

  def get(t: Term, engine: Prolog, er: EngineRunner): Boolean = {
    synchronized{searchLoop(t, engine, true, true, er)}
  }

  private def searchLoop(t: Term, engine: Prolog, block: Boolean, remove: Boolean, er: EngineRunner): Boolean = {
    synchronized {var found: Boolean = false
      var interrupted = false
      do {
        found = search(t, engine, remove)
        if (found)
          return true

        er.setSolving(false)
        try {
          wait()
        } catch {
          case e: InterruptedException => {
            interrupted = true
          }
        }
      } while (block && !interrupted)
      false
    }
  }

  private def search(t: Term, engine: Prolog, remove: Boolean): Boolean = {
    synchronized{
      var found: Boolean = false
      var msg: Term = null
      val it : ju.ListIterator[Term]= _queue.listIterator
      while (!found) {
        if (it.hasNext) {
          msg = it.next
        }
        else {
          return false
        }
        found = engine.unify(t, msg)
      }
      if (remove) {
        _queue.remove(msg)
      }
      return true
    }
  }

  def peek(t: Term, engine: Prolog): Boolean = {
    synchronized{return search(t, engine, false)}
  }

  def remove(t: Term, engine: Prolog): Boolean = {
    synchronized{return search(t, engine, true)}
  }

  def wait(t: Term, engine: Prolog, er: EngineRunner): Boolean = {
    synchronized{return searchLoop(t, engine, true, false, er)}
  }

  def store(t: Term) {
    synchronized{_queue.addLast(t)
      notifyAll()}
  }

  def size: Int = {
    synchronized{return _queue.size}
  }

  def clear() {
    synchronized{_queue.clear()}
  }
}