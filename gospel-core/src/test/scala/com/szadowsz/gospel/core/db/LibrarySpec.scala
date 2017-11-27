package com.szadowsz.gospel.core.db

import com.szadowsz.gospel.core.data.Int
import com.szadowsz.gospel.core.db.libs.TestLibrary
import com.szadowsz.gospel.core.listener.TestOutputListener
import com.szadowsz.gospel.core.{BaseEngineSpec, PrologEngine, Solution}
import org.scalatest.FlatSpec

class LibrarySpec extends FlatSpec with BaseEngineSpec {

  override protected def init(): PrologEngine = new PrologEngine()

  behavior of "Library"

  it should "add a functor successfully" in {
    prolog.loadLibrary(new TestLibrary)
    val goal = prolog.solve("N is sum(1, 3).")
    goal.isSuccess shouldBe true
    goal.getVarValue("N") should be (new Int(4))

  }

  it should "add a predicate successfully" in {
    prolog.loadLibrary(new TestLibrary)
    val l = new TestOutputListener
    prolog.addOutputListener(l)
    prolog.solve("println(sum(5)).")
    l.output should be ("sum(5)")
  }
}
