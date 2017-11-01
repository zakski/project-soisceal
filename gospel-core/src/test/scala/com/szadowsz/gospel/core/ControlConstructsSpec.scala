package com.szadowsz.gospel.core

import com.szadowsz.gospel.core.data.Struct
import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

/**
  * Created on 18/02/2017.
  */
@RunWith(classOf[JUnitRunner])
class ControlConstructsSpec extends FunSpec with BaseEngineSpec {

  override protected def init(): PrologEngine = new PrologEngine()

  describe("call/1") {
    it("should pass simple test #1") {
      val solution = prolog.solve("call(!).")
      solution.isSuccess shouldBe true
    }

    it("should pass negative test #1") {
      val solution = prolog.solve("call(fail). false")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #2") {
      val solution = prolog.solve("call((fail, X)). false")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #3") {
      val solution = prolog.solve("call((fail, call(1))).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #4") {
      prolog.setTheory(new Theory("b(X) :- Y = (write(X), X), call(Y). a(1). a(2)."))
      val solution = prolog.solve("b(_).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #5") {
      prolog.setTheory(new Theory("b(X) :- Y = (write(X), X), call(Y). a(1). a(2)."))
      val solution = prolog.solve("b(3).")
      solution.isSuccess shouldBe false
    }

    it("should pass exception test #1") {
      val ex = getExceptionListener
      prolog.addExceptionListener(ex)
      val solution = prolog.solve("call((write(3), X)).")
      ex.exFound shouldBe true
    }

    it("should pass exception test #2") {
      val ex = getExceptionListener
      prolog.addExceptionListener(ex)
      val solution = prolog.solve("call((write(3), call(1))).")
      ex.exFound shouldBe true
    }

    it("should pass exception test #3") {
      val ex = getExceptionListener
      prolog.addExceptionListener(ex)
      val solution = prolog.solve("call(X).")
      ex.exFound shouldBe true
    }

    it("should pass exception test #4") {
      val ex = getExceptionListener
      prolog.addExceptionListener(ex)
      val solution = prolog.solve("call(1).")
      ex.exFound shouldBe true
    }

    it("should pass exception test #5") {
      val ex = getExceptionListener
      prolog.addExceptionListener(ex)
      val solution = prolog.solve("call((fail, 1)).")
      ex.exFound shouldBe true
    }

    it("should pass exception test #6") {
      val ex = getExceptionListener
      prolog.addExceptionListener(ex)
      val solution = prolog.solve("call((write(3), 1)).")
      ex.exFound shouldBe true
    }

    it("should pass exception test #7") {
      val ex = getExceptionListener
      prolog.addExceptionListener(ex)
      val solution = prolog.solve("call((1 ; true)).")
      ex.exFound shouldBe true
    }
  }

  describe(";/2 (disjunction)") {
    it("should pass simple test #1") {
      val solution = prolog.solve("';'(true, fail).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #2") {
      val solution = prolog.solve("';'(!, call(3)).")
      solution.isSuccess shouldBe true
    }

    it("should pass this negative test") {
      val solution = prolog.solve("';'((!, fail), true).")
      solution.isSuccess shouldBe false
    }

    it("should pass variable test #1") {
      val solution = prolog.solve("';'((X = 1, !), X = 2).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("X")
      replaceUnderscore(result.toString) shouldBe "1"
    }

    it("should pass variable test #2") {
      val solution = prolog.solve("','(';'(X = 1, X = 2), ';'(true, !)).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("X")
      replaceUnderscore(result.toString) shouldBe "1"
    }
  }

  describe("->/2 (if-then)") {
    it("should pass simple test #1") {
      val solution = prolog.solve("'->'(true, true).")
      solution.isSuccess shouldBe true
    }

    it("should pass negative test #1") {
      val solution = prolog.solve("'->'(true, fail).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #2") {
      val solution = prolog.solve("'->'(fail, true).")
      solution.isSuccess shouldBe false
    }

    it("should pass variable test #1") {
      val solution = prolog.solve("'->'(true, X = 1).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("X")
      replaceUnderscore(result.toString) shouldBe "1"
    }

    it("should pass variable test #2") {
      val solution = prolog.solve("'->'(';'(X = 1, X = 2), true).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("X")
      replaceUnderscore(result.toString) shouldBe "1"
    }

    it("should pass variable test #3") {
      var solution = prolog.solve("'->'(true, ';'(X = 1, X = 2)).")
      solution.isSuccess shouldBe true

      var result = solution.getVarValue("X")
      replaceUnderscore(result.toString) shouldBe "1"

      solution = prolog.solveNext()
      result = solution.getVarValue("X")
      replaceUnderscore(result.toString) shouldBe "2"
    }
  }

  describe("if-then-else") {
    it("should pass simple test #1") {
      val solution = prolog.solve("';'('->'(true, true), fail).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #2") {
      val solution = prolog.solve("';'('->'(fail, true), true).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #3") {
      val solution = prolog.solve("';'('->'((!, fail), true), true).")
      solution.isSuccess shouldBe true
    }


    it("should pass negative test #1") {
      val solution = prolog.solve("';'('->'(true, fail), fail).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #2") {
      val solution = prolog.solve("';'('->'(fail, true), fail).")
      solution.isSuccess shouldBe false
    }

    it("should pass variable test #1") {
      val solution = prolog.solve("';'('->'(true, X = 1), X = 2).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("X")
      replaceUnderscore(result.toString) shouldBe "1"
    }

    it("should pass variable test #2") {
      val solution = prolog.solve("';'('->'(fail, X = 1), X = 2).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("X")
      replaceUnderscore(result.toString) shouldBe "2"
    }

    it("should pass variable test #3") {
      var solution = prolog.solve("'->'(true, ';'(X = 1, X = 2)).")
      solution.isSuccess shouldBe true

      var result = solution.getVarValue("X")
      replaceUnderscore(result.toString) shouldBe "1"

      solution = prolog.solveNext()
      result = solution.getVarValue("X")
      replaceUnderscore(result.toString) shouldBe "2"
    }

    it("should pass variable test #4") {
      val solution = prolog.solve("';'('->'(';'(X = 1, X = 2), true), true).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("X")
      replaceUnderscore(result.toString) shouldBe "1"
    }
  }
}
