package com.szadowsz.gospel.core.db.libs.basic

import com.szadowsz.gospel.core.data.{Int, Struct, Var}
import com.szadowsz.gospel.core.{BaseEngineSpec, PrologEngine}
import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

/**
  * Created on 18/02/2017.
  */
@RunWith(classOf[JUnitRunner])
class TermUnificationSpec extends FunSpec with BaseEngineSpec {

  override protected def init(): PrologEngine = new PrologEngine()

  describe("=/2 (PrologEngine unify)") {
    it("should pass simple test #1") {
      val solution = prolog.solve("'='(1, 1).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #2") {
      val solution = prolog.solve("'='(_, _).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #3") {
      val solution = prolog.solve("'='(X, 1).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #4") {
      val solution = prolog.solve("'='(X, Y).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #5") {
      val solution = prolog.solve("'='(X=Y, X=abc).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #6") {
      val solution = prolog.solve("'='(f(X,def), f(def,Y)).")
      solution.isSuccess shouldBe true
    }

    it("should pass negative test #1") {
      val solution = prolog.solve("'='(1, 2).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #2") {
      val solution = prolog.solve("'='(1, 2).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #3") {
      val solution = prolog.solve("'='(1, 1.0).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #4") {
      val solution = prolog.solve("'='(g(X), f(f(X))).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #5") {
      val solution = prolog.solve("'='(f(X,1),f(a(X))).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #6") {
      val solution = prolog.solve("'='(f(X, Y, X, 1), f(a(X), a(Y), Y, 2)).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #7") {
      val solution = prolog.solve("'='(f(X, Y, X), f(a(X), a(Y), Y, 2)).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #8") {
      val solution = prolog.solve("'='(X, a(X)).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #9") {
      val solution = prolog.solve("'='(f(X, 1), f(a(X), 2)).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #10") {
      val solution = prolog.solve("'='(f(1, X, 1), f(2, a(X), 2)).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #11") {
      val solution = prolog.solve("'='(f(1, X), f(2, a(X))).")
      solution.isSuccess shouldBe false
    }

    it("should pass variable test #1") {
      val solution = prolog.solve("'='(X, 1).")
      solution.isSuccess shouldBe true

      val result = solution.getVar("X").asInstanceOf[Int]
      result.intValue shouldBe 1
    }

    it("should pass variable test #2") {
      val solution = prolog.solve("'='(X, Y).")
      solution.isSuccess shouldBe true

      val result = solution.getVar("X").asInstanceOf[Var]
      result.getName shouldBe "X"
    }

    it("should pass variable test #3") {
      val solution = prolog.solve("'='(X, Y), '='(X, abc).")
      solution.isSuccess shouldBe true

      val result = solution.getVar("X").asInstanceOf[Struct]
      result.getName shouldBe "abc"
    }

    it("should pass variable test #4") {
      val solution = prolog.solve("'='(X, Y), '='(X, abc).")
      solution.isSuccess shouldBe true

      val result = solution.getVar("Y").asInstanceOf[Struct]
      result.getName shouldBe "abc"
    }

    it("should pass variable test #5") {
      val solution = prolog.solve("'='(f(X, def), f(def, Y)).")
      solution.isSuccess shouldBe true

      val result = solution.getVar("X").asInstanceOf[Struct]
      result.getName shouldBe "def"
    }

    it("should pass variable test #6") {
      val solution = prolog.solve("'='(f(X, def), f(def, Y)).")
      solution.isSuccess shouldBe true

      val result = solution.getVar("Y").asInstanceOf[Struct]
      result.getName shouldBe "def"
    }
  }

  describe("unify_with_occurs_check/2 (unify) ") {
    it("should pass simple test #1") {
      val solution = prolog.solve("unify_with_occurs_check(1,1).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #2") {
      val solution = prolog.solve("unify_with_occurs_check(X,Y).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #3") {
      val solution = prolog.solve("unify_with_occurs_check(_,_).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #4") {
      val solution = prolog.solve("unify_with_occurs_check(X,Y).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #5") {
      val solution = prolog.solve("unify_with_occurs_check(f(X,def),f(def,Y)).")
      solution.isSuccess shouldBe true
    }

    it("should pass negative test #1") {
      val solution = prolog.solve("unify_with_occurs_check(1, 2).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #2") {
      val solution = prolog.solve("unify_with_occurs_check(1, 1.0).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #3") {
      val solution = prolog.solve("unify_with_occurs_check(g(X), f(f(X))).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #4") {
      val solution = prolog.solve("unify_with_occurs_check(f(X,1), f(a(X))).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #5") {
      val solution = prolog.solve("unify_with_occurs_check(f(X,Y,X), f(a(X), a(Y), Y, 2)). \t")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #6") {
      val solution = prolog.solve("unify_with_occurs_check(X, a(X)).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #7") {
      val solution = prolog.solve("unify_with_occurs_check(f(X,1), f(a(X),2)).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #8") {
      val solution = prolog.solve("unify_with_occurs_check(f(1,X,1), f(2,a(X),2)).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #9") {
      val solution = prolog.solve("unify_with_occurs_check(f(1,X), f(2,a(X))).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #10") {
      val solution = prolog.solve("unify_with_occurs_check(f(X,Y,X,1), f(a(X), a(Y), Y, 2)).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #11") {
      val solution = prolog.solve("unify_with_occurs_check(Y, a(Y)).")
      solution.isSuccess shouldBe false
    }

    it("should pass variable test #1") {
      val solution = prolog.solve("unify_with_occurs_check(X,1).")
      solution.isSuccess shouldBe true

      val result = solution.getVar("X").asInstanceOf[Int]
      result.intValue shouldBe 1
    }
  }

  describe("\\=/2 (not PrologEngine unifiable)") {
    it("should pass simple test #1") {
      val solution = prolog.solve("'\\='(1, 2).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #2") {
      val solution = prolog.solve("'\\='(1, 1.0).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #3") {
      val solution = prolog.solve("'\\='(g(X), f(f(X))).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #4") {
      val solution = prolog.solve("'\\='(f(X, 1), f(a(X))).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #5") {
      val solution = prolog.solve("'\\='(f(X, Y, X), f(a(X), a(Y), Y, 2)).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #6") {
      val solution = prolog.solve("'\\='(X, a(X)).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #7") {
      val solution = prolog.solve("'\\='(f(X, 1), f(a(X), 2)).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #8") {
      val solution = prolog.solve("'\\='(f(1, X, 1), f(2, a(X), 2)).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #9") {
      val solution = prolog.solve("'\\='(f(1, X), f(2, a(X))).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #10") {
      val solution = prolog.solve("'\\='(f(X, Y, X, 1), f(a(X), a(Y), Y, 2)). \t")
      solution.isSuccess shouldBe true
    }

    it("should pass negative test #1") {
      val solution = prolog.solve("'\\='(1, 1).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #2") {
      val solution = prolog.solve("'\\='(X, 1).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #3") {
      val solution = prolog.solve("'\\='(X, Y).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #4") {
      val solution = prolog.solve("'\\='(_, _).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #5") {
      val solution = prolog.solve("'\\='(f(X, def), f(def, Y)).")
      solution.isSuccess shouldBe false
    }
  }
}
