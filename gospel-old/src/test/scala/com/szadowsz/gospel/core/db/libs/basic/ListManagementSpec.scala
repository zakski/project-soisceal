package com.szadowsz.gospel.core.db.libs.basic

import com.szadowsz.gospel.core.data.Struct
import com.szadowsz.gospel.core.{BaseEngineSpec, PrologEngine, data}
import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

/**
  * Created on 04/03/2017.
  */
@RunWith(classOf[JUnitRunner])
class ListManagementSpec extends FunSpec with BaseEngineSpec {

  override protected def init(): PrologEngine = new PrologEngine()

  describe("length/2") {
    it("should pass simple test #1") {
      val solution = prolog.solve("length([], 0).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #2") {
      val solution = prolog.solve("length(X, 5).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #3") {
      val solution = prolog.solve("length([1, 2 | T], X).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #4") {
      val solution = prolog.solve("length(L, S).")
      solution.isSuccess shouldBe true
    }

    it("should pass negative test #1") {
      val solution = prolog.solve("length('scarlet', 7).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #2") {
      val solution = prolog.solve("length(A, -1).")
      solution.isSuccess shouldBe false
    }

    it("should pass variable test #1") {
      val solution = prolog.solve("length(X, 5).")
      solution.isSuccess shouldBe true

      val result = solution.getVar("X").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "[_,_,_,_,_]"
    }

    it("should pass variable test #2") {
      var solution = prolog.solve("length([1, 2 | T], X).")
      solution.isSuccess shouldBe true

      var result1 = solution.getVar("X").asInstanceOf[data.Number]
      replaceUnderscore(result1.toString) shouldBe "2"

      var result2 = solution.getVar("T").asInstanceOf[Struct]
      replaceUnderscore(result2.toString) shouldBe "[]"

      solution = prolog.solveNext()

      result1 = solution.getVar("X").asInstanceOf[data.Number]
      replaceUnderscore(result1.toString) shouldBe "3"

      result2 = solution.getVar("T").asInstanceOf[Struct]
      replaceUnderscore(result2.toString) shouldBe "[_]"

      solution = prolog.solveNext()

      result1 = solution.getVar("X").asInstanceOf[data.Number]
      replaceUnderscore(result1.toString) shouldBe "4"

      result2 = solution.getVar("T").asInstanceOf[Struct]
      replaceUnderscore(result2.toString) shouldBe "[_,_]"
    }

    it("should pass variable test #3") {
      var solution = prolog.solve("length(L, S).")
      solution.isSuccess shouldBe true

      var result1 = solution.getVar("L").asInstanceOf[Struct]
      replaceUnderscore(result1.toString) shouldBe "[]"

      var result2 = solution.getVar("S").asInstanceOf[data.Number]
      replaceUnderscore(result2.toString) shouldBe "0"

      solution = prolog.solveNext()

      result1 = solution.getVar("L").asInstanceOf[Struct]
      replaceUnderscore(result1.toString) shouldBe "[_]"

      result2 = solution.getVar("S").asInstanceOf[data.Number]
      replaceUnderscore(result2.toString) shouldBe "1"

      solution = prolog.solveNext()

      result1 = solution.getVar("L").asInstanceOf[Struct]
      replaceUnderscore(result1.toString) shouldBe "[_,_]"

      result2 = solution.getVar("S").asInstanceOf[data.Number]
      replaceUnderscore(result2.toString) shouldBe "2"
    }
  }
}
