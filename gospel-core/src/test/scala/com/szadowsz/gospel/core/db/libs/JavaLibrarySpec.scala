package com.szadowsz.gospel.core.db.libs

import com.szadowsz.gospel.core.data.{Struct,Int,Float}
import com.szadowsz.gospel.core.{BaseEngineSpec, PrologEngine, Theory}
import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

/**
  * Created on 18/02/2017.
  */
@RunWith(classOf[JUnitRunner])
class JavaLibrarySpec extends FunSpec with BaseEngineSpec {

  override protected def init(): PrologEngine = new PrologEngine()

  describe("java_object/3") {
    it("should pass simple test #1") {
      val solution = prolog.solve("java_object('java.lang.Integer', [0], zero), java_object('java.lang.Integer', [0], expected), zero <- equals(expected) " +
        "returns true.")
      solution.isSuccess shouldBe true
    }
    it("should pass negative test #1") {
      val solution = prolog.solve("java_object('non.existant.Class', [], _).")
      solution.isSuccess shouldBe false
    }
    it("should pass negative test #2") {
      val solution = prolog.solve("java_object('java.lang.Integer', [], _).")
      solution.isSuccess shouldBe false
    }
    it("should pass negative test #3") {
      val solution = prolog.solve("java_object('java.lang.Integer', [0], Z), java_object('java.lang.Integer', [1], Z).")
      solution.isSuccess shouldBe false
    }
  }

  describe("<-/2 and returns/2") {
    it("should pass simple test #1") {
      val solution = prolog.solve("java_object('java.util.ArrayList', [], L), L <- clear returns X. \t")
      solution.isSuccess shouldBe true
    }
    it("should pass simple test #2") {
      val solution = prolog.solve("java_object('java.util.ArrayList', [], L), L <- size.")
      solution.isSuccess shouldBe true
    }
    it("should pass simple test #3") {
      val solution = prolog.solve("java_object('java.util.ArrayList', [], L), L <- isEmpty returns true.")
      solution.isSuccess shouldBe true
    }
    it("should pass simple test #4") {
      val solution = prolog.solve("java_object('java.lang.String', ['hello'], S), S <- toUpperCase returns 'HELLO'.")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #5") {
      val solution = prolog.solve("class('java.lang.System') <- gc returns X.")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #6") {
      val solution = prolog.solve("class('java.lang.Integer') <- parseInt('15') returns 15.")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #7") {
      val solution = prolog.solve("class('java.lang.System') <- currentTimeMillis.")
      solution.isSuccess shouldBe true
    }

    it("should pass negative test #1") {
      val solution = prolog.solve("java_object('java.util.ArrayList', [], L), L <- clear(10).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #2") {
      val solution = prolog.solve("java_object('java.lang.Object', [], Obj), Obj <- nonExistantMethod.")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #3") {
      val solution = prolog.solve("java_object('java.lang.Integer', [0], Z), Z <- compareTo(X).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #4") {
      val solution = prolog.solve("java_object('java.lang.Integer', [0], Z), Z <- compareTo('ciao').")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #5") {
      val solution = prolog.solve("class('java.lang.Integer') <- parseInt(10) returns N.")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #6") {
      val solution = prolog.solve("class('java.lang.Integer') <- parseInt(X) returns N.")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #7") {
      val solution = prolog.solve("class('java.lang.System') <- currentTimeMillis(10).")
      solution.isSuccess shouldBe false
    }
    it("should pass negative test #8") {
      val solution = prolog.solve("class('non.existant.Class') <- nonExistantMethod.")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #9") {
      val solution = prolog.solve("class('java.lang.Integer') <- nonExistantMethod.")
      solution.isSuccess shouldBe false
    }

    it("should pass variable test #1") {
      val solution = prolog.solve("java_object('java.lang.Integer', [5], N), N <- intValue returns V.")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("V")
      replaceUnderscore(result.toString) shouldBe "5"
    }

    it("should pass variable test #2") {
      val solution = prolog.solve("java_object('java.util.ArrayList', [], X), X <- toArray returns A, atom_chars(A, L).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("L").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "['$', 'o', 'b', 'j', '_', '2']"
    }
  }

  describe("as and '.'") {
    it("should pass simple test #1") {
      val solution = prolog.solve("java_object('java.lang.String', ['hello'], S1), java_object('java.lang.String', ['world'], S2), S2 <- compareTo(S1 as 'java.lang.Object') returns X, X > 0.")
      solution.isSuccess shouldBe true
    }
    it("should pass simple test #2") {
      val solution = prolog.solve("class('java.lang.Integer').'MAX_VALUE' <- get(V), V > 0.")
      solution.isSuccess shouldBe true
    }
    it("should pass simple test #3") {
      val solution = prolog.solve("java_object('java.awt.GridBagConstraints', [], C), java_object('java.awt.Insets', [1,1,1,1], I1), C.insets <- set(I1), C.insets <- get(I2), I1 == I2.")
      solution.isSuccess shouldBe true
    }
    it("should pass negative test #1") {
      val solution = prolog.solve("java_object('java.lang.Integer', [5], I), class('java.lang.Integer') <- toString(I as int) returns '5'.")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #2") {
      val solution = prolog.solve("java_object('java.lang.String', ['hello'], S1), java_object('java.lang.String', ['world'], S2), S2 <- compareTo(S1 as 'non.existant.Class') returns X.")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #3") {
      val solution = prolog.solve("java_object('java.util.ArrayList', [], L), java_object('java.lang.String', ['hello'], S), S <- compareToIgnoreCase(L as 'java.util.List') returns X.")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #4") {
      val solution = prolog.solve("java_object('java.lang.String', ['hello'], S), java_object('java.lang.Integer', [2], I), S <- indexOf(I as 'java.util.List') returns N.")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #5") {
      val solution = prolog.solve("class('java.lang.Integer').MAX_VALUE <- get(V).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #6") {
      val solution = prolog.solve("class('java.lang.Integer').'NON_EXISTANT_FIELD' <- get(X).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #7") {
      val solution = prolog.solve("java_object('java.awt.Point', [], P), P.nonExistantField <- get(X).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #8") {
      val solution = prolog.solve("java_object('java.awt.Point', [], P), P.nonExistantField <- set(0).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #9") {
      val solution = prolog.solve("java_object('java.awt.Point', [], P), P.y <- set(X).")
      solution.isSuccess shouldBe false
    }

    it("should pass variable test #1") {
      val solution = prolog.solve("java_object('java.awt.Point', [], P), P.x <- get(X).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("X").asInstanceOf[Int]
      result.toString shouldBe "0"
    }

    it("should pass variable test #2") {
      val solution = prolog.solve("java_object('java.awt.Point', [], P), P.y <- set(5), P.y <- get(Y).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("Y").asInstanceOf[Int]
      result.toString shouldBe "5"
    }
  }

    describe("java_array_set/3, java_array_get/3 and java_array_length/2") {
      it("should pass simple test #1") {
        val solution = prolog.solve("java_object('java.lang.Object[]', [3], A), java_object('java.lang.Object', [], Obj), java_array_set(A, 2, Obj), java_array_get(A, 2, X), X == Obj.")
        solution.isSuccess shouldBe true
      }

      it("should pass negative test #1") {
        val solution = prolog.solve("java_object('java.lang.Integer[]', [3], A), java_array_get(A, 4, Obj).")
        solution.isSuccess shouldBe false
      }

      it("should pass negative test #2") {
        val solution = prolog.solve("java_object('java.awt.Polygon', [], P), P.xpoints <- get(XP), java_array_get_boolean(XP, 2, V).")
        solution.isSuccess shouldBe false
      }

      it("should pass negative test #3") {
        val solution = prolog.solve("java_object('java.lang.String[]', [5], A), java_array_set(A, 2, X).")
        solution.isSuccess shouldBe false
      }

      it("should pass negative test #4") {
        val solution = prolog.solve("java_object('java.lang.Integer[]', [5], A), java_array_set(A, 2, zero).")
        solution.isSuccess shouldBe false
      }

      it("should pass negative test #5") {
        val solution = prolog.solve("java_object('java.awt.Polygon', [], P), P.xpoints <- get(XP), java_array_set_boolean(XP, 3, 2).")
        solution.isSuccess shouldBe false
      }

      it("should pass negative test #6") {
        val solution = prolog.solve("java_object('java.lang.Object', [], Obj), java_array_length(Obj, Size).")
        solution.isSuccess shouldBe false
      }

      it("should pass negative test #7") {
        val solution = prolog.solve("java_object('java.lang.Object', [], Obj), java_array_get(Obj, 0, X).")
        solution.isSuccess shouldBe false
      }

      it("should pass negative test #8") {
        val solution = prolog.solve("java_object('java.lang.Integer', [0], I), java_array_set(I, 0, 5).")
        solution.isSuccess shouldBe false
      }

      it("should pass negative test #9") {
        val solution = prolog.solve("java_object('java.lang.Integer', [0], I), java_array_set_int(I, 0, 5).")
        solution.isSuccess shouldBe false
      }

      it("should pass variable test #1") {
        val solution = prolog.solve("java_object('java.lang.Integer[]', [3], A), java_array_length(A, Size).")
        solution.isSuccess shouldBe true

        val result = solution.getVarValue("Size").asInstanceOf[Int]
        result.toString shouldBe "3"
      }

      it("should pass variable test #2") {
        val solution = prolog.solve("java_object('java.lang.Integer[]', [3], A), java_array_get(A, 0, I), I <- intValue returns V.")
        solution.isSuccess shouldBe true

        val result = solution.getVarValue("V").asInstanceOf[Int]
        result.toString shouldBe "_"
      }


      it("should pass variable test #3") {
        val solution = prolog.solve("java_object('java.awt.Polygon', [], P), P.xpoints <- get(XP), java_array_get_int(XP, 3, V).")
        solution.isSuccess shouldBe true

        val result = solution.getVarValue("V").asInstanceOf[Int]
        result.toString shouldBe "0"
      }

      it("should pass variable test #4") {
        val solution = prolog.solve("java_object('java.awt.Polygon', [], P), P.xpoints <- get(XP), java_array_get_float(XP, 3, V).")
        solution.isSuccess shouldBe true

        val result = solution.getVarValue("V").asInstanceOf[Float]
        result.toString shouldBe "0.0"
      }

      it("should pass variable test #5") {
        val solution = prolog.solve("java_object('java.lang.Integer[]', [3], A), java_object('java.lang.Integer', [2], Two), java_array_set(A, 2, Two), java_array_get(A, 2, X).")
        solution.isSuccess shouldBe true

        val result = solution.getVarValue("X").asInstanceOf[Int]
        result.toString shouldBe "2"
      }

      it("should pass variable test #6") {
        val solution = prolog.solve("java_object('java.awt.Polygon', [], P), P.xpoints <- get(XP), java_array_set_int(XP, 3, 2), java_array_get_int(XP, 3, V).")
        solution.isSuccess shouldBe true

        val result = solution.getVarValue("V").asInstanceOf[Int]
        result.toString shouldBe "2"
      }

      it("should pass variable test #7") {
        val solution = prolog.solve("java_object('java.awt.Polygon', [], P), P.xpoints <- get(XP), java_array_set_float(XP, 3, 2), java_array_get_int(XP, 3, V).")
        solution.isSuccess shouldBe true

        val result = solution.getVarValue("V").asInstanceOf[Int]
        result.toString shouldBe "2.0"
      }
  }
}
