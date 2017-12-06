package com.szadowsz.gospel.core.db.libs.basic

import com.szadowsz.gospel.core.{BaseEngineSpec, PrologEngine}
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ArithmeticEvaluationSpec extends FlatSpec with BaseEngineSpec {

  override protected def init(): PrologEngine = new PrologEngine()

  behavior of "Arithmetic Evaluation Predicate"

  it should "pass simple test #1" in {
    val solution = prolog.solve("'is'(3, 3).")
    solution.isSuccess shouldBe true
  }

  it should "pass simple test #2" in {
    val solution = prolog.solve("'is'(Result, 3 + 11.0).")
    solution.isSuccess shouldBe true
  }

  it should "pass simple test #3" in {
    val solution = prolog.solve("X = 1 + 2, Y is X * 3.")
    solution.isSuccess shouldBe true
  }

  it should "pass negative test #1" in {
    val solution = prolog.solve("'is'(3, 3.0).")
    solution.isSuccess shouldBe false
  }

  it should "pass negative test #2" in {
    val solution = prolog.solve("'is'(foo, 77).")
    solution.isSuccess shouldBe false
  }


  it should "pass negative test #3" in {
    val solution = prolog.solve("is(_, foo).")
    solution.isSuccess shouldBe false
  }

  it should "pass negative test #4" in {
    val solution = prolog.solve("'is'(77, N).")
    solution.isSuccess shouldBe false
  }
}
