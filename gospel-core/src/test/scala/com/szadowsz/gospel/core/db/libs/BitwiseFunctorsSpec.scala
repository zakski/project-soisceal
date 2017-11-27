package com.szadowsz.gospel.core.db.libs

import com.szadowsz.gospel.core.data.{Struct,Long}
import com.szadowsz.gospel.core.{BaseEngineSpec, PrologEngine, Theory}
import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class BitwiseFunctorsSpec extends FunSpec with BaseEngineSpec {

  override protected def init(): PrologEngine = new PrologEngine()

  describe("(>>)/2 (bitwise right shift)") {
    it("should pass variable test #1") {
      val solution = prolog.solve("X is '>>'(16, 2).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("X").asInstanceOf[Long]
       result shouldBe new Long(4)
    }

    it("should pass variable test #2") {
      val solution = prolog.solve("X is '>>'(19, 2).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("X").asInstanceOf[Long]
      result shouldBe new Long(4)
    }

    it("should pass variable test #3") {
      val solution = prolog.solve("X is '>>'(-16, 2).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("X").asInstanceOf[Long]
      result shouldBe new Long(-4)
    }
  }

  describe("(<<)/2 (bitwise left shift)") {
    it("should pass variable test #1") {
      val solution = prolog.solve("X is '<<'(16, 2).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("X").asInstanceOf[Long]
      result shouldBe new Long(64)
    }

    it("should pass variable test #2") {
      val solution = prolog.solve("X is '<<'(19, 2).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("X").asInstanceOf[Long]
      result shouldBe new Long(76)
    }

    it("should pass variable test #3") {
      val solution = prolog.solve("X is '<<'(-16, 2).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("X").asInstanceOf[Long]
      result shouldBe new Long(-64)
    }
  }

    describe("\t(/\\)/2 (bitwise and)") {
      it("should pass variable test #1") {
        val solution = prolog.solve("X is '/\\'(10, 12).")
        solution.isSuccess shouldBe true

        val result = solution.getVarValue("X").asInstanceOf[Long]
        result shouldBe new Long(8)
      }

      it("should pass variable test #2") {
        val solution = prolog.solve("X is '/\\'(125, 255).")
        solution.isSuccess shouldBe true

        val result = solution.getVarValue("X").asInstanceOf[Long]
        result shouldBe new Long(125)
      }

      it("should pass variable test #3") {
        val solution = prolog.solve("X is '/\\'(-10, 12).")
        solution.isSuccess shouldBe true

        val result = solution.getVarValue("X").asInstanceOf[Long]
        result shouldBe new Long(4)
      }
  }

  describe("(\\/)/2 (bitwise or)") {
    it("should pass variable test #1") {
      val solution = prolog.solve("X is '\\/'(10, 12).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("X").asInstanceOf[Long]
      result shouldBe new Long(14)
    }

    it("should pass variable test #2") {
      val solution = prolog.solve("X is '\\/'(125, 255).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("X").asInstanceOf[Long]
      result shouldBe new Long(255)
    }

    it("should pass variable test #3") {
      val solution = prolog.solve("X is '\\/'(-10, 12).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("X").asInstanceOf[Long]
      result shouldBe new Long(-2)
    }
  }

  describe("(\\)/1 (bitwise complement) ") {
    it("should pass variable test #1") {
      val solution = prolog.solve("X is '\\'('\\'(10)).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("X").asInstanceOf[Long]
      result shouldBe new Long(10)
    }

    it("should pass variable test #2") {
      val solution = prolog.solve("X is \\(\\(10)).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("X").asInstanceOf[Long]
      result shouldBe new Long(10)
    }

    it("should pass variable test #3") {
      val solution = prolog.solve("X is \\(10).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("X").asInstanceOf[Long]
      result shouldBe new Long(-11)
    }
  }
}
