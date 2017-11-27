package com.szadowsz.gospel.core.db.libs

import com.szadowsz.gospel.core.{BaseEngineSpec, PrologEngine}
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner

/**
  * Created on 18/02/2017.
  */
@RunWith(classOf[JUnitRunner])
class ArithmeticComparisonSpec extends FlatSpec with BaseEngineSpec {

  override protected def init(): PrologEngine = new PrologEngine()

  behavior of "Arithmetic Comparison Predicates"

  it should "Expand Not Equals and Return success" in {
      val solution = prolog.solve("'=\\='(0, 1).")
      solution.isSuccess shouldBe true
    }

  it should "Expand Less Than and Return success" in {
    val solution = prolog.solve("'<'(0, 1).")
    solution.isSuccess shouldBe true
  }

  it should "Expand Less Than Or Equal and Return success #1" in {
    val solution = prolog.solve("'=<'(0, 1).")
    solution.isSuccess shouldBe true
  }

  it should "Expand Equal and Return success #1" in {
    val solution = prolog.solve("'=:='(1.0, 1).")
    solution.isSuccess shouldBe true
  }

  it should "Expand More Than Or Equal and Return success #1" in {
    val solution = prolog.solve("'>='(1.0, 1).")
    solution.isSuccess shouldBe true
  }

  it should "Expand Less Than Or Equal and Return success #2" in {
    val solution = prolog.solve("'=<'(1.0, 1).")
    solution.isSuccess shouldBe true
  }

  it should "Expand Equal and Return success #2" in {
    val solution = prolog.solve("'=:='(3 * 2, 7 - 1).")
    solution.isSuccess shouldBe true
  }

  it should "Expand More Than Or Equal and Return success #2" in {
    val solution = prolog.solve("'>='(3 * 2, 7 - 1).")
    solution.isSuccess shouldBe true
  }

  it should "Expand Less Than Or Equal and Return success #3" in {
    val solution = prolog.solve("'=<'(3 * 2, 7 - 1).")
    solution.isSuccess shouldBe true
  }

  it should "Expand Equals and Return failure" in {
    val solution = prolog.solve("'=:='(0, 1).")
    solution.isSuccess shouldBe false
  }

  it should "Expand Greater Than and Return failure ]1" in {
    val solution = prolog.solve("'>'(0, 1).")
    solution.isSuccess shouldBe false
  }

  it should "Expand Greater Than Or Equal and Return failure #1" in {
    val solution = prolog.solve("'>='(0, 1).")
    solution.isSuccess shouldBe false
  }

  it should "Expand Not Equals and Return failure #1" in {
    val solution = prolog.solve("'=\\='(1.0, 1).")
    solution.isSuccess shouldBe false
  }

  it should "Expand Less Than and Return failure #1" in {
    val solution = prolog.solve("'<'(1.0, 1).")
    solution.isSuccess shouldBe false
  }

  it should "Expand Greater Than and Return failure #2" in {
    val solution = prolog.solve("'>'(1.0, 1).")
    solution.isSuccess shouldBe false
  }

  it should "Expand Not Equals and Return failure #2" in {
    val solution = prolog.solve("'=\\='(3 * 2, 7 - 1).")
    solution.isSuccess shouldBe false
  }

  it should "Expand Less Than and Return failure #2" in {
    val solution = prolog.solve("'<'(3 * 2, 7 - 1).")
    solution.isSuccess shouldBe false
  }

  it should "Expand Greater Than and Return failure #3" in {
    val solution = prolog.solve("'>'(3 * 2, 7 - 1).")
    solution.isSuccess shouldBe false
  }


  it should "pass exception test #1" in {
    val ex = getExceptionListener
    prolog.addExceptionListener(ex)
    val solution = prolog.solve("'=:='(X, 5).")
    ex.exFound shouldBe true
    ex.exMsg shouldBe "Instantiation error in argument 1 of expression_equality(X_e1,5)"
  }


  it should "pass exception test #2" in {
    val ex = getExceptionListener
    prolog.addExceptionListener(ex)
    val solution = prolog.solve("'=\\='(X, 5).")
    ex.exFound shouldBe true
    ex.exMsg shouldBe "Instantiation error in argument 1 of expression_equality(X_e1,5)"
  }


  it should "pass exception test #3" in {
    val ex = getExceptionListener
    prolog.addExceptionListener(ex)
    val solution = prolog.solve("'<'(X, 5).")
    ex.exFound shouldBe true
    ex.exMsg shouldBe "Instantiation error in argument 1 of expression_less_than(X_e1,5)"
  }


  it should "pass exception test #4" in {
    val ex = getExceptionListener
    prolog.addExceptionListener(ex)
    val solution = prolog.solve("'>'(X, 5).")
    ex.exFound shouldBe true
    ex.exMsg shouldBe "Instantiation error in argument 1 of expression_greater_than(X_e1,5)"
  }


  it should "pass exception test #5" in {
    val ex = getExceptionListener
    prolog.addExceptionListener(ex)
    val solution = prolog.solve("'>='(X, 5).")
    ex.exFound shouldBe true
    ex.exMsg shouldBe "Instantiation error in argument 1 of expression_greater_or_equal_than(X_e1,5)"
  }


  it should "pass exception test #6" in {
    val ex = getExceptionListener
    prolog.addExceptionListener(ex)
    val solution = prolog.solve("'=<'(X, 5).")
    ex.exFound shouldBe true
    ex.exMsg shouldBe "Instantiation error in argument 1 of expression_less_or_equal_than(X_e1,5)"
  }
}
