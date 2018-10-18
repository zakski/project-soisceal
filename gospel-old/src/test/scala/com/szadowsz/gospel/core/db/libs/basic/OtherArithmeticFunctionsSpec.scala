package com.szadowsz.gospel.core.db.libs.basic

import com.szadowsz.gospel.core.data.Float
import com.szadowsz.gospel.core.{BaseEngineSpec, PrologEngine}
import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class OtherArithmeticFunctionsSpec extends FunSpec with BaseEngineSpec {

  override protected def init(): PrologEngine = new PrologEngine()

  describe("(**)/2 (power)") {

    it("should pass variable test #1") {
      var solution = prolog.solve("X is '**'(5, 3).")
      solution.isSuccess shouldBe true

      var result = solution.getVar("X").asInstanceOf[Float]
      result.doubleValue shouldBe 125.0
    }

    it("should pass variable test #2") {
      var solution = prolog.solve("X is '**'(-5.0, 3).")
      solution.isSuccess shouldBe true

      var result = solution.getVar("X").asInstanceOf[Float]
      result.doubleValue shouldBe -125.0
    }

    it("should pass variable test #3") {
      var solution = prolog.solve("X is '**'(5, -1).")
      solution.isSuccess shouldBe true

      var result = solution.getVar("X").asInstanceOf[Float]
      result.doubleValue shouldBe 0.2
    }

    it("should pass variable test #4") {
      var solution = prolog.solve("X is '**'(5, 3.0).")
      solution.isSuccess shouldBe true

      var result = solution.getVar("X").asInstanceOf[Float]
      result.doubleValue shouldBe 125.0
    }

    it("should pass variable test #5") {
      var solution = prolog.solve("X is '**'(0.0, 0).")
      solution.isSuccess shouldBe true

      var result = solution.getVar("X").asInstanceOf[Float]
      result.doubleValue shouldBe 1.0
    }
  }

  describe("sin/1") {
    it("should pass variable test #1") {
      var solution = prolog.solve("X is sin(0.0).")
      solution.isSuccess shouldBe true

      var result = solution.getVar("X").asInstanceOf[Float]
      result.doubleValue shouldBe 0.0
    }

    it("should pass variable test #2") {
      var solution = prolog.solve("X is sin(0).")
      solution.isSuccess shouldBe true

      var result = solution.getVar("X").asInstanceOf[Float]
      result.doubleValue shouldBe 0.0
    }

    it("should pass variable test #3") {
      var solution = prolog.solve("PI is atan(1.0) * 4, X is sin(PI / 2.0).")
      solution.isSuccess shouldBe true

      var result = solution.getVar("X").asInstanceOf[Float]
      result.doubleValue shouldBe 1.0

      result = solution.getVar("PI").asInstanceOf[Float]
      result.doubleValue shouldBe 3.141592653589793
    }
  }

  describe("cos/1") {
    it("should pass variable test #1") {
      var solution = prolog.solve("X is cos(0.0).")
      solution.isSuccess shouldBe true

      var result = solution.getVar("X").asInstanceOf[Float]
      result.doubleValue shouldBe 1.0
    }

    it("should pass variable test #2") {
      var solution = prolog.solve("X is cos(0).")
      solution.isSuccess shouldBe true

      var result = solution.getVar("X").asInstanceOf[Float]
      result.doubleValue shouldBe 1.0
    }

    it("should pass variable test #3") {
      var solution = prolog.solve("PI is atan(1.0) * 4, X is cos(PI / 2.0).")
      solution.isSuccess shouldBe true

      var result = solution.getVar("X").asInstanceOf[Float]
      result.doubleValue shouldBe 6.123233995736766E-17

      result = solution.getVar("PI").asInstanceOf[Float]
      result.doubleValue shouldBe 3.141592653589793
    }
  }

    describe("atan/1") {
        it("should pass variable test #1") {
        var solution = prolog.solve("PI is atan(1.0) * 4.")
        solution.isSuccess shouldBe true

        val result = solution.getVar("PI").asInstanceOf[Float]
        result.doubleValue shouldBe 3.141592653589793
      }
  }

  describe("exp/1") {
    it("should pass variable test #1") {
      var solution = prolog.solve("X is exp(0.0).")
      solution.isSuccess shouldBe true

      var result = solution.getVar("X").asInstanceOf[Float]
      result.doubleValue shouldBe 1.0
    }

    it("should pass variable test #2") {
      var solution = prolog.solve("X is exp(1.0).")
      solution.isSuccess shouldBe true

      var result = solution.getVar("X").asInstanceOf[Float]
      result.doubleValue shouldBe 2.718281828459045
    }

    it("should pass variable test #3") {
      var solution = prolog.solve("X is exp(0).")
      solution.isSuccess shouldBe true

      var result = solution.getVar("X").asInstanceOf[Float]
      result.doubleValue shouldBe 1.0
    }
  }

  describe("log/1") {
    it("should pass variable test #1") {
      var solution = prolog.solve("X is log(1.0).")
      solution.isSuccess shouldBe true

      var result = solution.getVar("X").asInstanceOf[Float]
      result.doubleValue shouldBe 0.0
    }

    it("should pass variable test #2") {
      var solution = prolog.solve("X is log(2.7182818284590455).")
      solution.isSuccess shouldBe true

      var result = solution.getVar("X").asInstanceOf[Float]
      result.doubleValue shouldBe 1.0
    }
  }

  describe("sqrt/1") {
    it("should pass variable test #1") {
      var solution = prolog.solve("X is sqrt(0.0).")
      solution.isSuccess shouldBe true

      var result = solution.getVar("X").asInstanceOf[Float]
      result.doubleValue shouldBe 0.0
    }

    it("should pass variable test #2") {
      var solution = prolog.solve("X is sqrt(1.0).")
      solution.isSuccess shouldBe true

      var result = solution.getVar("X").asInstanceOf[Float]
      result.doubleValue shouldBe 1.0
    }

    it("should pass variable test #3") {
      var solution = prolog.solve("X is sqrt(1.21).")
      solution.isSuccess shouldBe true

      var result = solution.getVar("X").asInstanceOf[Float]
      result.doubleValue shouldBe 1.1
    }
  }
}
