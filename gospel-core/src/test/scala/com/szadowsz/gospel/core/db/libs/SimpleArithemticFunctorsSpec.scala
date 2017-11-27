package com.szadowsz.gospel.core.db.libs

import java.util

import com.szadowsz.gospel.core.data.{Double,Int,Number}
import com.szadowsz.gospel.core.{BaseEngineSpec, PrologEngine}
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SimpleArithemticFunctorsSpec extends FlatSpec with BaseEngineSpec {

  override protected def init(): PrologEngine = new PrologEngine()

  behavior of "Simple arithmetic functors"

  it should "pass variable test #1" in {
    val solution = prolog.solve("X is '+'(7, 35).")
    solution.isSuccess shouldBe true

    val result = solution.getVarValue("X").asInstanceOf[Int]
    result.intValue shouldBe 42
  }

  it should "pass variable test #2" in {
    val solution = prolog.solve("X is '+'(0, 3+11).")
    solution.isSuccess shouldBe true

    val result = solution.getVarValue("X").asInstanceOf[Int]
    result.intValue shouldBe 14
  }

  it should "pass variable test #3" in {
    val solution = prolog.solve("X is '+'(0, 3.2+11).")
    solution.isSuccess shouldBe true

    val result = solution.getVarValue("X").asInstanceOf[Double]
    result.doubleValue shouldBe 14.2
  }

  it should "pass variable test #4" in {
    val solution = prolog.solve("X is '-'(7).")
    solution.isSuccess shouldBe true

    val result = solution.getVarValue("X").asInstanceOf[Int]
    result.intValue shouldBe -7
  }

  it should "pass variable test #5" in {
    val solution = prolog.solve("X is '-'(3-11).")
    solution.isSuccess shouldBe true

    val result = solution.getVarValue("X").asInstanceOf[Int]
    result.intValue shouldBe 8
  }

  it should "pass variable test #6" in {
    val solution = prolog.solve("X is '-'(3.2-11).")
    solution.isSuccess shouldBe true

    val result = solution.getVarValue("X").asInstanceOf[Double]
    result.doubleValue shouldBe 7.8
  }

  it should "pass variable test #7" in {
    val solution = prolog.solve("X is '-'(7, 35).")
    solution.isSuccess shouldBe true

    val result = solution.getVarValue("X").asInstanceOf[Int]
    result.intValue shouldBe -28
  }

  it should "pass variable test #8" in {
    val solution = prolog.solve("X is '-'(20, 3+11).")
    solution.isSuccess shouldBe true

    val result = solution.getVarValue("X").asInstanceOf[Int]
    result.intValue shouldBe 6
  }

  it should "pass variable test #9" in {
    val solution = prolog.solve("X is '-'(0, 3.2+11).")
    solution.isSuccess shouldBe true

    val result = solution.getVarValue("X").asInstanceOf[Double]
    result.doubleValue shouldBe -14.2
  }

  it should "pass variable test #10" in {
    val solution = prolog.solve("X is '*'(7, 35).")
    solution.isSuccess shouldBe true

    val result = solution.getVarValue("X").asInstanceOf[Int]
    result.intValue shouldBe 245
  }

  it should "pass variable test #11" in {
    val solution = prolog.solve("X is '*'(1.5, 3.2+11).")
    solution.isSuccess shouldBe true

    val result = solution.getVarValue("X").asInstanceOf[Double]
    result.doubleValue shouldBe 21.299999999999997
  }

  it should "pass variable test #12" in {
    val solution = prolog.solve("X is '/'(7, 35).")
    solution.isSuccess shouldBe true

    val result = solution.getVarValue("X").asInstanceOf[Int]
    result.intValue shouldBe 0
  }

  it should "pass variable test #13" in {
    val solution = prolog.solve("X is '/'(7.0, 35).")
    solution.isSuccess shouldBe true

    val result = solution.getVarValue("X").asInstanceOf[Double]
    result.doubleValue shouldBe 0.2
  }

  it should "pass variable test #14" in {
    val solution = prolog.solve("X is '/'(140, 3+11).")
    solution.isSuccess shouldBe true

    val result = solution.getVarValue("X").asInstanceOf[Int]
    result.intValue shouldBe 10
  }

  it should "pass variable test #15" in {
    val solution = prolog.solve("X is '/'(20.164, 3.2+11).")
    solution.isSuccess shouldBe true

    val result = solution.getVarValue("X").asInstanceOf[Double]
    result.doubleValue shouldBe 1.4200000000000002
  }

    it should "pass variable test #16" in {
    val solution = prolog.solve("X is mod(7, 3).")
    solution.isSuccess shouldBe true

    val result = solution.getVarValue("X").asInstanceOf[Int]
    result.intValue shouldBe 1
  }

  it should "pass variable test #17" in {
    val solution = prolog.solve("X is mod(0, 3+11).")
    solution.isSuccess shouldBe true

    val result = solution.getVarValue("X").asInstanceOf[Int]
    result.intValue shouldBe 0
  }

  it should "pass variable test #18" in {
    val solution = prolog.solve("X is mod(7, -2).")
    solution.isSuccess shouldBe true

    val result = solution.getVarValue("X").asInstanceOf[Int]
    result.intValue shouldBe -1
  }

  it should "pass variable test #19" in {
    val solution = prolog.solve("X is floor(7.4).")
    solution.isSuccess shouldBe true

    val result = solution.getVarValue("X").asInstanceOf[Int]
    result.intValue shouldBe 7
  }

  it should "pass variable test #20" in {
    val solution = prolog.solve("X is floor(-0.4).")
    solution.isSuccess shouldBe true

    val result = solution.getVarValue("X").asInstanceOf[Int]
    result.intValue shouldBe -1
  }

  it should "pass variable test #21" in {
    val solution = prolog.solve("X is round(7.5).")
    solution.isSuccess shouldBe true

    val result = solution.getVarValue("X").asInstanceOf[Number]
    result.intValue shouldBe 8
  }

  it should "pass variable test #22" in {
    val solution = prolog.solve("X is round(7.6).")
    solution.isSuccess shouldBe true

    val result = solution.getVarValue("X").asInstanceOf[Number]
    result.intValue shouldBe 8
  }

  it should "pass variable test #23" in {
    val solution = prolog.solve("X is '/'(7, 35).")
    solution.isSuccess shouldBe true

    val result = solution.getVarValue("X").asInstanceOf[Int]
    result.intValue shouldBe 0
  }

  it should "pass variable test #24" in {
    val solution = prolog.solve("X is round(-0.6).")
    solution.isSuccess shouldBe true

    val result = solution.getVarValue("X").asInstanceOf[Number]
    result.intValue shouldBe -1
  }

  it should "pass variable test #25" in {
    val solution = prolog.solve("X is ceiling(-0.5).")
    solution.isSuccess shouldBe true

    val result = solution.getVarValue("X").asInstanceOf[Int]
    result.intValue shouldBe 0
  }

  it should "pass variable test #26" in {
    val solution = prolog.solve("X is truncate(-0.5).")
    solution.isSuccess shouldBe true

    val result = solution.getVarValue("X").asInstanceOf[Int]
    result.intValue shouldBe 0
  }

  it should "pass variable test #27" in {
    val solution = prolog.solve("X is float(7).")
    solution.isSuccess shouldBe true

    val result = solution.getVarValue("X").asInstanceOf[Number]
    result.floatValue shouldBe 7.0f
  }

  it should "pass variable test #28" in {
    val solution = prolog.solve("X is float(7.3).")
    solution.isSuccess shouldBe true

    val result = solution.getVarValue("X").asInstanceOf[Number]
    result.floatValue shouldBe 7.3f
  }
  it should "pass variable test #29" in {
    val solution = prolog.solve("X is float(5 / 3).")
    solution.isSuccess shouldBe true

    val result = solution.getVarValue("X").asInstanceOf[Number]
    result.floatValue shouldBe 1.0f
  }

  it should "pass variable test #30" in {
    val solution = prolog.solve("X is abs(7).")
    solution.isSuccess shouldBe true

    val result = solution.getVarValue("X").asInstanceOf[Int]
    result.intValue shouldBe 7
  }

  it should "pass variable test #31" in {
    val solution = prolog.solve("X is abs(3-11).")
    solution.isSuccess shouldBe true

    val result = solution.getVarValue("X").asInstanceOf[Int]
    result.intValue shouldBe 8
  }

  it should "pass variable test #32" in {
    val solution = prolog.solve("X is abs(3.2-11.0).")
    solution.isSuccess shouldBe true

    val result = solution.getVarValue("X").asInstanceOf[Double]
    result.doubleValue shouldBe 7.8
  }


  it should "pass exception test #1" in {
    val solution = prolog.solve("current_prolog_flag(max_integer, MI), X is '+'(MI, 1).")
    solution.isSuccess shouldBe false
  }

  it should "pass exception test #2" in {
    val solution = prolog.solve("current_prolog_flag(max_integer, MI), X is '-'('+'(MI, 1), 1).")
    solution.isSuccess shouldBe false
  }

  it should "pass exception test #3" in {
    val solution = prolog.solve("current_prolog_flag(max_integer, MI), X is '*'(MI, 2).")
    solution.isSuccess shouldBe false
  }

  it should "pass exception test #4" in {
    val solution = prolog.solve("current_prolog_flag(max_integer, MI), R is float(MI) * 2, X is floor(R).")
    solution.isSuccess shouldBe false
  }
}