package com.szadowsz.gospel.core

import alice.tuprolog.Struct
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
      val solution = prolog.solve("setof(X,(X=1;X=2),S).")
      solution.isSuccess shouldBe true
    }
    it("should pass simple test #2") {
      val solution = prolog.solve("setof(X,(X=1;X=2),X).")
      solution.isSuccess shouldBe true
    }
    it("should pass simple test #3") {
      val solution = prolog.solve("setof(X,(X=2;X=1),S).")
      solution.isSuccess shouldBe true
    }
    it("should pass simple test #4") {
      val solution = prolog.solve("setof(X,(X=2;X=2),S).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #5") {
      val solution = prolog.solve("setof(X,(X=Y;X=Z),S).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #6") {
      val solution = prolog.solve("setof(1,(Y=2;Y=1),L).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #7") {
      val solution = prolog.solve("setof(f(X,Y),(X=a;Y=b),L).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #8") {
      val solution = prolog.solve("setof(X,Y^((X=1,Y=1);(X=2,Y=2)),S).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #9") {
      val solution = prolog.solve("setof(X,Y^((X=1;Y=1);(X=2,Y=2)),S).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #10") {
      val solution = prolog.solve("setof(X,Y^((X=1,Y=1);X=3),S).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #11") {
      val solution = prolog.solve("setof(X,(X=Y;X=Z;Y=1),S).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #12") {
      prolog.setTheory(new Theory("a(1,f(_)). a(2,f(_))."))
      val solution = prolog.solve("setof(X,a(X,Y),L).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #13") {
      val solution = prolog.solve("setof(X,member(X,[f(U,b),f(V,c)]),L).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #14") {
      val solution = prolog.solve("setof(X,member(X,[f(b,U),f(c,V)]),[f(b,a),f(c,a)]).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #15") {
      val solution = prolog.solve("setof(X,member(X,[V,U,f(U),f(V)]),L).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #16") {
      val solution = prolog.solve("setof(X,member(X,[V,U,f(U),f(V)]),[a,b,f(a),f(b)]).")
      solution.isSuccess shouldBe true
    }

    it("should pass negative test #1") {
      val solution = prolog.solve("setof(X,fail,S).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #2") {
      val solution = prolog.solve("setof(X,member(X,[V,U,f(U),f(V)]),[a,b,f(b),f(a)]).")
      solution.isSuccess shouldBe false
    }

    it("should pass variable test #1") {
      val solution = prolog.solve("setof(X,(X=1;X=2),S).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("S").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "[1,2]"
    }

    it("should pass variable test #2") {
      val solution = prolog.solve("setof(X,(X=1;X=2),X).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("X").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "[1,2]"
    }

    it("should pass variable test #3") {
      val solution = prolog.solve("setof(X,(X=2;X=1),S).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("S").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "[1,2]"
    }

    it("should pass variable test #4") {
      val solution = prolog.solve("setof(X,(X=2;X=2),S).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("S").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "[2]"
    }

    it("should pass variable test #5") {
      val solution = prolog.solve("setof(X,(X=Y;X=Z),S).")
      solution.isSuccess shouldBe true

      val xResult = solution.getVarValue("S")
      replaceUnderscore(xResult.toString) shouldBe "[Y,Z]"
    }

    it("should pass variable test #6") {
      val solution = prolog.solve("setof(1,(Y=2;Y=1),L).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("L").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "[1]"
    }

    it("should pass variable test #7") {
      val solution = prolog.solve("setof(f(X,Y),(X=a;Y=b),L).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("L").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "[f(_,b),f(a,_)]"
    }

    it("should pass variable test #8") {
      val solution = prolog.solve("setof(X,Y^((X=1,Y=1);(X=2,Y=2)),S).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("S").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "[1,2]"
    }

    it("should pass variable test #9") {
      val solution = prolog.solve("setof(X,Y^((X=1;Y=1);(X=2,Y=2)),S).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("S").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "[_,1,2]"
    }

    it("should pass variable test #10") {
      val solution = prolog.solve("setof(X,Y^((X=1,Y=1);X=3),S).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("S").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "[1,3]"
    }

    it("should pass variable test #11") {
      val solution = prolog.solve("setof(X,(X=Y;X=Z;Y=1),S).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("S").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "[Y,Z]"
    }

    it("should pass variable test #12") {
      prolog.setTheory(new Theory("a(1,f(_)). a(2,f(_))."))
      val solution = prolog.solve("setof(X,a(X,Y),L).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("L").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "[1,2]"
    }

    it("should pass variable test #13") {
      val solution = prolog.solve("setof(X,member(X,[f(U,b),f(V,c)]),L).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("L").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "[f(U,b),f(V,c)]"
    }

    it("should pass variable test #14") {
      val solution = prolog.solve("setof(X,member(X,[V,U,f(U),f(V)]),L).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("L").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "[V,U,f(V),f(U)]"
    }

    it("should pass variable test #15") {
      val solution = prolog.solve("setof(X,member(X,[V,U,f(U),f(V)]),[a,b,f(a),f(b)]).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("V").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "a"
    }
  }
}
