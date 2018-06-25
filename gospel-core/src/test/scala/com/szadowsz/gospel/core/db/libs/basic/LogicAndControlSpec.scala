package com.szadowsz.gospel.core.db.libs.basic

import com.szadowsz.gospel.core.data.Int
import com.szadowsz.gospel.core.{BaseEngineSpec, PrologEngine}
import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class LogicAndControlSpec extends FunSpec with BaseEngineSpec {

  override protected def init(): PrologEngine = new PrologEngine()

  describe("\\+/1 (not provable)") {

    it("should pass simple test #1") {
      val solution = prolog.solve("'\\+'((!, false)).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #2") {
      val solution = prolog.solve("'\\+'(4 = 5).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #3") {
      val solution = prolog.solve("'\\+'(X = f(X)).")
      solution.isSuccess shouldBe true
    }

    it("should pass negative test #1") {
      val solution = prolog.solve("'\\+'(true).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #2") {
      val solution = prolog.solve("\\+(!).")
      solution.isSuccess shouldBe false
    }

    it("should pass variable test #1") {
      var solution = prolog.solve("(X=1; X=2), \\+((!, fail)).")
      solution.isSuccess shouldBe true

      var result = solution.getVar("X").asInstanceOf[Int]
      result.intValue shouldBe 1

      solution = prolog.solveNext()
      result = solution.getVar("X").asInstanceOf[Int]
      result.intValue shouldBe 2
    }

    it("should pass exception test #1") {
      val solution = prolog.solve("\\+(3).")
      solution.isSuccess shouldBe false
    }

    it("should pass exception test #2") {
      val solution = prolog.solve("'\\+'(X).")
      solution.isSuccess shouldBe false
    }
  }

  describe("once/1") {

    it("should pass simple test #1") {
      val solution = prolog.solve("once(!).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #2") {
      val solution = prolog.solve("once(repeat). \t")
      solution.isSuccess shouldBe true
    }

    it("should pass negative test #1") {
      val solution = prolog.solve("once(fail).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #2") {
      val solution = prolog.solve("once(X = f(X)).")
      solution.isSuccess shouldBe false
    }

    it("should pass variable test #1") {
      var solution = prolog.solve("once(!), (X=1; X=2).")
      solution.isSuccess shouldBe true

      var result = solution.getVar("X").asInstanceOf[Int]
      result.intValue shouldBe 1

      solution = prolog.solveNext()
      result = solution.getVar("X").asInstanceOf[Int]
      result.intValue shouldBe 2
    }
  }

  describe("repeat/0") {

    it("should pass negative test #1") {
      val solution = prolog.solve("repeat, !, fail.")
      solution.isSuccess shouldBe false
    }
  }
}
