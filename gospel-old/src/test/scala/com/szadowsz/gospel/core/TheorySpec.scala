package com.szadowsz.gospel.core

import com.szadowsz.gospel.core.data.{Struct, Term}
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TheorySpec extends FlatSpec with BaseEngineSpec {

  override protected def init(): PrologEngine = new PrologEngine()

  behavior of "Theory Object"

  it should "be able to handle strings with parenthesis" in {
    val before = "a :- b, (d ; e)."
    val theory = new Theory(before)
    val after = theory.toString
    new Theory(after).toString shouldBe theory.toString
  }

  it should "be able to append clause lists" in {
    val clauseList = Array[Term](new Struct("p"), new Struct("q"), new Struct("r"))
    val otherClauseList = Array[Term](new Struct("a"), new Struct("b"), new Struct("c"))
    val theory = new Theory(new Struct(clauseList))
    theory.append(new Theory(new Struct(otherClauseList)))

    prolog.setTheory(theory)
    prolog.solve("p.").isSuccess shouldBe true
    prolog.solve("b.").isSuccess shouldBe true
  }
}
