package com.szadowsz.gospel.core.engine.flags

import com.szadowsz.gospel.core.data.Struct
import com.szadowsz.gospel.core.{BaseEngineSpec, PrologEngine, Theory}
import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

/**
  * Created on 18/02/2017.
  */
@RunWith(classOf[JUnitRunner])
class ImplementationDefinedHooksSpec extends FunSpec with BaseEngineSpec {

  override protected def init(): PrologEngine = new PrologEngine()

  describe("set_prolog_flag/2") {
    it("should pass negative test #1") {
      val solution = prolog.solve("set_prolog_flag(unknown, fail).")
      solution.isSuccess shouldBe false
    }

    it("should pass exception test #1") {
      val solution = prolog.solve("set_prolog_flag(X, off).")
      solution.isSuccess shouldBe false
    }

    it("should pass exception test #2") {
      val solution = prolog.solve("set_prolog_flag(5, decimals).")
      solution.isSuccess shouldBe false
    }
    it("should pass exception test #3") {
      val solution = prolog.solve("set_prolog_flag(date, 'July 1988').")
      solution.isSuccess shouldBe false
    }

    it("should pass exception test #4") {
      val solution = prolog.solve("set_prolog_flag(debug, trace).")
      solution.isSuccess shouldBe false
    }
  }

  describe("current_prolog_flag/2") {
    it("should pass simple test #1") {
      val solution = prolog.solve("current_prolog_flag(debug, off).")
      solution.isSuccess shouldBe true
    }

    it("should pass negative test #1") {
      val solution = prolog.solve("current_prolog_flag(F, V).")
      solution.isSuccess shouldBe false
    }

    it("should pass exception test #1") {
      val solution = prolog.solve("current_prolog_flag(5, _).")
      solution.isSuccess shouldBe false
    }
  }
}
