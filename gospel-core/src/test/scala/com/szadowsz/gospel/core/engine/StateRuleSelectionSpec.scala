package com.szadowsz.gospel.core.engine

import com.szadowsz.gospel.core.listener.TestWarningListener
import com.szadowsz.gospel.core.{BaseEngineSpec, PrologEngine, Theory}
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class StateRuleSelectionSpec extends FlatSpec with BaseEngineSpec {

  override protected def init(): PrologEngine = new PrologEngine()

  behavior of "State Rule Selection"

  it should "warn about unknown predicate in query" in {
    val warningListener = new TestWarningListener
    prolog.addWarningListener(warningListener)
    val query = "p(X)."
    prolog.solve(query)
    warningListener.warning.indexOf("p/1") > 0 shouldBe true
    warningListener.warning.indexOf("is unknown") > 0  shouldBe true
  }

  it should "warn about unknown predicate in theory" in {
    val warningListener = new TestWarningListener
    prolog.addWarningListener(warningListener)
    val theory = "p(X) :- a, b. \nb."
    prolog.setTheory(new Theory(theory))
    val query = "p(X)."
    prolog.solve(query)
    warningListener.warning.indexOf("a/0") > 0  shouldBe true
    warningListener.warning.indexOf("is unknown") > 0  shouldBe true
  }
}
