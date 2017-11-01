package com.szadowsz.gospel.core

import com.szadowsz.gospel.core.data.Struct
import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

/**
  * Created on 18/02/2017.
  */
@RunWith(classOf[JUnitRunner])
class AllSolutionsSpec extends FunSpec with BaseEngineSpec {

  override protected def init(): PrologEngine = new PrologEngine()

  describe("findall/3") {
    it("should pass simple test #1") {
      val solution = prolog.solve("findall(X, (X=1;Y=2), S).")
      solution.isSuccess shouldBe true
    }
    it("should pass simple test #2") {
      val solution = prolog.solve("findall(X+Y, (X=1), S).")
      solution.isSuccess shouldBe true
    }
    it("should pass simple test #3") {
      val solution = prolog.solve("findall(X, fail, L).")
      solution.isSuccess shouldBe true
    }
    it("should pass simple test #4") {
      val solution = prolog.solve("findall(X, (X=1;X=1), S).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #5") {
      val solution = prolog.solve("findall(X, (X=1;X=2), [X,Y]).")
      solution.isSuccess shouldBe true
    }

    it("should pass this negative test") {
      val solution = prolog.solve("findall(X, (X=2; X=1), [1, 2]).")
      solution.isSuccess shouldBe false
    }

    it("should pass variable test #1") {
      val solution = prolog.solve("findall(X, (X=1;Y=2), S).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("S").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "[1,_]"
    }

    it("should pass variable test #2") {
      val solution = prolog.solve("findall(X+Y, (X=1), S).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("S").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "['+'(1,_)]"
    }

    it("should pass variable test #3") {
      val solution = prolog.solve("findall(X, fail, L).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("L").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "[]"
    }

    it("should pass variable test #4") {
      val solution = prolog.solve("findall(X, (X=1;X=1), S).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("S").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "[1,1]"
    }

    it("should pass variable test #5") {
      val solution = prolog.solve("findall(X, (X=1;X=2), [X,Y]).")
      solution.isSuccess shouldBe true

      val xResult = solution.getVarValue("X")
      xResult.toString shouldBe "1"

      val yResult = solution.getVarValue("Y")
      yResult.toString shouldBe "2"
    }

    it("should pass variable test #6") {
      val solution = prolog.solve("findall(X, (X=1;X=2), S).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("S").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "[1,2]"
    }

    it("should pass exception test #1") {
      val ex = getExceptionListener
      prolog.addExceptionListener(ex)
      val solution = prolog.solve("findall(X,Goal,S).")
      ex.exFound shouldBe true
      ex.exMsg shouldBe "Instantiation error in argument 2 of all_solutions_predicates_guard(Template_e1,Goal_e1,Instances_e1)"
    }

    it("should pass exception test #2") {
      val ex = getExceptionListener
      prolog.addExceptionListener(ex)
      val solution = prolog.solve("findall(X,4,S).")
      ex.exFound shouldBe true
      ex.exMsg shouldBe "Type error in argument 2 of all_solutions_predicates_guard(Template_e1,4,Instances_e1)"
    }
  }

  describe("bagof/3") {
    it("should pass simple test #1") {
      val solution = prolog.solve("bagof(X, (X=1;Y=2), S).")
      solution.isSuccess shouldBe true
    }
    it("should pass simple test #2") {
      val solution = prolog.solve("bagof(X,(X=1;X=2), X).")
      solution.isSuccess shouldBe true
    }
    it("should pass simple test #3") {
      val solution = prolog.solve("bagof(X,(X=Y;X=Z), S1).")
      solution.isSuccess shouldBe true
    }
    it("should pass simple test #4") {
      val solution = prolog.solve("bagof(1,(Y=1;Y=2), L).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #5") {
      val solution = prolog.solve("bagof(f(X,Y), (X=a;Y=b), L1).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #6") {
      val solution = prolog.solve("bagof(X, Y^((X=1,Y=1);(X=2;Y=2)), L2).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #7") {
      val solution = prolog.solve("bagof(X, Y^((X=1;Y=1);(X=2;Y=2)), L3).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #8") {
      val solution = prolog.solve("bagof(X, Y^((X=1;Y=2);X=3), Si1).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #9") {
      val solution = prolog.solve("bagof(X, (X=Y;X=Z;Y=1), S3).")
      solution.isSuccess shouldBe true
    }

    it("should pass this negative test") {
      val solution = prolog.solve("bagof(X,fail,S2).")
      solution.isSuccess shouldBe false
    }

    it("should pass variable test #1") {
      val solution = prolog.solve("bagof(X,(X=1;X=2), S).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("S").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "[1,2]"
    }

    it("should pass variable test #2") {
      val solution = prolog.solve("bagof(X,(X=1;X=2), X).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("X").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "[1,2]"
    }

    it("should pass variable test #3") {
      val solution = prolog.solve("bagof(X,(X=Y;X=Z), S1).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("S1").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "[Y,Z]"
    }

    it("should pass variable test #4") {
      val solution = prolog.solve("bagof(1,(Y=1;Y=2), L).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("L").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "[1]"
    }

    it("should pass variable test #5") {
      val solution = prolog.solve("bagof(f(X,Y), (X=a;Y=b), L1).")
      solution.isSuccess shouldBe true

      val xResult = solution.getVarValue("L1")
      replaceUnderscore(xResult.toString) shouldBe "[f(a,_),f(_,b)]"
    }

    it("should pass variable test #6") {
      val solution = prolog.solve("bagof(X, Y^((X=1,Y=1);(X=2;Y=2)), L2).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("L2").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "[1,2,_]"
    }

    it("should pass variable test #7") {
      val solution = prolog.solve("bagof(X, Y^((X=1;Y=1);(X=2;Y=2)), L3).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("L3").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "[1,_,2,_]"
    }

    it("should pass variable test #8") {
      val solution = prolog.solve("bagof(X, Y^((X=1;Y=2);X=3), Si1).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("Si1").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "[1,_,3]"
    }

    it("should pass variable test #9") {
      val solution = prolog.solve("bagof(X, (X=Y;X=Z;Y=1), S3).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("S3").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "[Y,Z]"
    }

    it("should pass exception test #1") {
      val ex = getExceptionListener
      prolog.addExceptionListener(ex)
      val solution = prolog.solve("bagof(X,Y^Z,L).")
      ex.exFound shouldBe true
     // ex.exMsg shouldBe "Instantiation error in argument 2 of all_solutions_predicates_guard(_2376378_e173,G_e1,Instances_e1)"
    }

    it("should pass exception test #2") {
      val ex = getExceptionListener
      prolog.addExceptionListener(ex)
      val solution = prolog.solve("bagof(X,1,L).")
      ex.exFound shouldBe true
      ex.exMsg shouldBe "Type error in argument 2 of all_solutions_predicates_guard(Template_e1,1,Instances_e1)"
    }

    it("should pass exception test #3") {
      val ex = getExceptionListener
      prolog.addExceptionListener(ex)
      val solution = prolog.solve("bagof(X,4,S).")
      ex.exFound shouldBe true
      ex.exMsg shouldBe "Type error in argument 2 of all_solutions_predicates_guard(Template_e1,4,Instances_e1)"
    }
  }

  describe("setof/3") {
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
