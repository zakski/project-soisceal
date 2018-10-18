package com.szadowsz.gospel.core.db.theory

import com.szadowsz.gospel.core.data.{Int, Struct}
import com.szadowsz.gospel.core._
import com.szadowsz.gospel.core.listener.{TestOutputListener, TestWarningListener}
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner

/**
  * Created on 18/02/2017.
  */
@RunWith(classOf[JUnitRunner])
class TheoryManagerSpec extends FlatSpec with BaseEngineSpec {

  override protected def init(): PrologEngine = new PrologEngine()

  behavior of "Theory Manager"

  it should "warn about unknown directives" in {
    val theory = ":- unidentified_directive(unknown_argument)."
    val warningListener = new TestWarningListener
    prolog.addWarningListener(warningListener)
    prolog.setTheory(new Theory(theory))

    warningListener.warning.indexOf("unidentified_directive/1") should be > 0
    warningListener.warning.indexOf("is unknown") should be > 0
  }

  it should "warn about failed directives" in {
    val theory = ":- load_library('UnknownLibrary')."
    val warningListener = new TestWarningListener
    prolog.addWarningListener(warningListener)
    prolog.setTheory(new Theory(theory))

    warningListener.warning.indexOf("load_library/1") should be > 0
    warningListener.warning.indexOf("Failed to Load Library") should be > 0
  }

  it should "not allow asserts to be backtracked" in {
    val solution = prolog.solve("assertz(a(z)).")
    solution.isSuccess shouldBe true
    solution.hasOpenAlternatives shouldBe false
  }

  it should "abolish predicates correctly via method call" in {
    val theory = "test(A, B) :- A is 1+2, B is 2+3."
    prolog.setTheory(new Theory(theory))

    val manager = prolog.getTheoryManager
    val testTerm = new Struct("test", new Struct("a"), new Struct("b"))
    var testClauses  = manager.find(testTerm)
    testClauses should have size 1

    manager.abolish(new Struct("/", new Struct("test"), new Int(2)))
    testClauses = manager.find(testTerm)

    testClauses should have size 0
  }

  it should "abolish predicates correctly via abolish predicate" in {
    prolog.setTheory(new Theory("fact(new).\nfact(other).\n"))

    var solution = prolog.solve("abolish(fact/1).")
    solution.isSuccess shouldBe true

    solution = prolog.solve("fact(V).")
    solution.isSuccess shouldBe false
  }

  it should "retract all predicates correctly" in {
    var solution = prolog.solve("assert(takes(s1,c2)), assert(takes(s1,c3)).")
    solution.isSuccess shouldBe true
    solution = prolog.solve("takes(s1, N).")
    solution.isSuccess shouldBe true
    solution.hasOpenAlternatives shouldBe true
    solution.getVar("N").toString should be("c2")
    solution = prolog.solveNext()
    solution.isSuccess shouldBe true
    solution.getVar("N").toString should be("c3")

    solution = prolog.solve("retractall(takes(s1,c2)).")
    solution.isSuccess shouldBe true
    solution = prolog.solve("takes(s1, N).")
    solution.isSuccess shouldBe true
    solution.hasOpenAlternatives shouldBe false
    solution.getVar("N").toString should be("c3")
  }

  // TODO test retractall: ClauseDatabase#get(f/a) should return an empty list

  it should "retract a predicate successfully" in {
    val listener = new TestOutputListener()
    prolog.addOutputListener(listener)
    prolog.setTheory(new Theory("insect(ant). insect(bee)."))
    val solution = prolog.solve("retract(insect(I)), write(I), retract(insect(bee)), fail.")
    solution.isSuccess shouldBe false
    listener.output should be ("antbee")
  }
}
