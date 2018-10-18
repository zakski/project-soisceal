package com.szadowsz.gospel.core.db.libs.basic

import com.szadowsz.gospel.core.{BaseEngineSpec, PrologEngine}
import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TypeTestingSpec extends FunSpec with BaseEngineSpec {

  override protected def init(): PrologEngine = new PrologEngine()

  describe("var/1") {

    it("should pass simple test #1") {
      val solution = prolog.solve("var(Foo).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #2") {
      val solution = prolog.solve("var(_).")
      solution.isSuccess shouldBe true
    }

    it("should pass negative test #1") {
      val solution = prolog.solve("var(foo).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #2") {
      val solution = prolog.solve("foo = Foo, var(Foo).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #3") {
      val solution = prolog.solve("foo=Bar, var(Bar).")
      solution.isSuccess shouldBe false
    }
  }

  describe("atom/1") {

    it("should pass simple test #1") {
      val solution = prolog.solve("atom(atom).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #2") {
      val solution = prolog.solve("atom('string').")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #3") {
      val solution = prolog.solve("atom([]).")
      solution.isSuccess shouldBe true
    }

    it("should pass negative test #1") {
      val solution = prolog.solve("atom(a(b)).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #2") {
      val solution = prolog.solve("atom(Var).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #3") {
      val solution = prolog.solve("atom(6).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #4") {
      val solution = prolog.solve("atom(3.3).")
      solution.isSuccess shouldBe false
    }
  }

  describe("integer/1") {

    it("should pass simple test #1") {
      val solution = prolog.solve("integer(6).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #2") {
      val solution = prolog.solve("integer(-3).")
      solution.isSuccess shouldBe true
    }

    it("should pass negative test #1") {
      val solution = prolog.solve("integer(3.3).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #2") {
      val solution = prolog.solve("integer(X).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #3") {
      val solution = prolog.solve("integer(atom).")
      solution.isSuccess shouldBe false
    }
  }

  describe("float/1") {

    it("should pass simple test #1") {
      val solution = prolog.solve("float(3.3).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #2") {
      val solution = prolog.solve("float(-3.3).")
      solution.isSuccess shouldBe true
    }

    it("should pass negative test #1") {
      val solution = prolog.solve("float(atom).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #2") {
      val solution = prolog.solve("float(X1).")
      solution.isSuccess shouldBe false
    }
  }

  describe("atomic/1") {

    it("should pass simple test #1") {
      val solution = prolog.solve("atomic(atom).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #2") {
      val solution = prolog.solve("atomic(2.3).")
      solution.isSuccess shouldBe true
    }

    it("should pass negative test #1") {
      val solution = prolog.solve("atomic(a(b)).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #2") {
      val solution = prolog.solve("atomic(V1).")
      solution.isSuccess shouldBe false
    }
  }

  describe("compound/1") {

    it("should pass simple test #1") {
      val solution = prolog.solve("compound(-a).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #2") {
      val solution = prolog.solve("compound(a(b)).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #3") {
      val solution = prolog.solve("compound([a]).")
      solution.isSuccess shouldBe true
    }

    it("should pass negative test #1") {
      val solution = prolog.solve("compound(33.3).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #2") {
      val solution = prolog.solve("compound(-33.3).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #3") {
      val solution = prolog.solve("compound(_).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #4") {
      val solution = prolog.solve("compound(a).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #5") {
      val solution = prolog.solve("compound([]).")
      solution.isSuccess shouldBe false
    }
  }

  describe("nonvar/1") {

    it("should pass simple test #1") {
      val solution = prolog.solve("nonvar(33.3).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #2") {
      val solution = prolog.solve("nonvar(foo).")
      solution.isSuccess shouldBe true
    }
    it("should pass simple test #3") {
      val solution = prolog.solve("foo = Baz, nonvar(Baz).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #4") {
      val solution = prolog.solve("nonvar(a(b)).")
      solution.isSuccess shouldBe true
    }

    it("should pass negative test #1") {
      val solution = prolog.solve("nonvar(_).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #2") {
      val solution = prolog.solve("nonvar(FOO).")
      solution.isSuccess shouldBe false
    }
  }

  describe("number/1") {

    it("should pass simple test #1") {
      val solution = prolog.solve("number(3).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #2") {
      val solution = prolog.solve("number(3.3).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #3") {
      val solution = prolog.solve("number(-3).")
      solution.isSuccess shouldBe true
    }

    it("should pass negative test #1") {
      val solution = prolog.solve("number(a).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #2") {
      val solution = prolog.solve("number(X).")
      solution.isSuccess shouldBe false
    }


  }

}