package com.szadowsz.gospel.core.db.libs.basic

import com.szadowsz.gospel.core.data.{Double, Int, Number}
import com.szadowsz.gospel.core.{BaseEngineSpec, PrologEngine}
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TermComparisonSpec extends FlatSpec with BaseEngineSpec {

  override protected def init(): PrologEngine = new PrologEngine()

  behavior of "Term Comparison"

  it should "pass simple test #1" in {
    val solution = prolog.solve("'@=<'(1.0, 1).")
    solution.isSuccess shouldBe true
  }

  it should "pass simple test #2" in {
    val solution = prolog.solve("'@<'(1.0, 1).")
    solution.isSuccess shouldBe true
  }


  it should "pass simple test #3" in {
    val solution = prolog.solve("'@=<'(aardvark, zebra).")
    solution.isSuccess shouldBe true
  }


  it should "pass simple test #4" in {
    val solution = prolog.solve("'@=<'(short, short).")
    solution.isSuccess shouldBe true
  }


  it should "pass simple test #5" in {
    val solution = prolog.solve("'@=<'(short, shorter).")
    solution.isSuccess shouldBe true
  }


  it should "pass simple test #6" in {
    val solution = prolog.solve("'@>'(foo(b), foo(a)).")
    solution.isSuccess shouldBe true
  }


  it should "pass simple test #7" in {
    val solution = prolog.solve("'@<'(foo(a, X), foo(b, Y)).")
    solution.isSuccess shouldBe true
  }


  it should "pass simple test #8" in {
    val solution = prolog.solve("'@<'(foo(X, a), foo(Y, b)).")
    solution.isSuccess shouldBe true
  }


  it should "pass simple test #9" in {
    val solution = prolog.solve("'@=<'(X, X).")
    solution.isSuccess shouldBe true
  }


  it should "pass simple test #10" in {
    val solution = prolog.solve("'=='(X, X).")
    solution.isSuccess shouldBe true
  }


  it should "pass simple test #11" in {
    val solution = prolog.solve("'@=<'(X, Y).")
    solution.isSuccess shouldBe true
  }


  it should "pass simple test #12" in {
    val solution = prolog.solve("'\\=='(_, _).")
    solution.isSuccess shouldBe true
  }


  it should "pass simple test #13" in {
    val solution = prolog.solve("'@=<'(_, _).")
    solution.isSuccess shouldBe true
  }


  it should "pass simple test #14" in {
    val solution = prolog.solve("'@=<'(foo(X, a), foo(Y, b)).")
    solution.isSuccess shouldBe true
  }

  it should "pass negative test #1" in {
    val solution = prolog.solve("'\\=='(1, 1).")
    solution.isSuccess shouldBe false
  }


  it should "pass negative test #2" in {
    val solution = prolog.solve("'@>='(short, shorter).")
    solution.isSuccess shouldBe false
  }


  it should "pass negative test #3" in {
    val solution = prolog.solve("'@<'(foo(a, b), north(a)).")
    solution.isSuccess shouldBe false
  }


  it should "pass negative test #4" in {
    val solution = prolog.solve("'=='(X, Y).")
    solution.isSuccess shouldBe false
  }


  it should "pass negative test #5" in {
    val solution = prolog.solve("'=='(_, _).")
    solution.isSuccess shouldBe false
  }

}