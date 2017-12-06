package com.szadowsz.gospel.core.db.libs.basic

import com.szadowsz.gospel.core.data.Struct
import com.szadowsz.gospel.core.{BaseEngineSpec, PrologEngine, Theory}
import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

/**
  * Created on 18/02/2017.
  */
@RunWith(classOf[JUnitRunner])
class ClauseRetrievalAndInformationSpec extends FunSpec with BaseEngineSpec {

  override protected def init(): PrologEngine = new PrologEngine()

  describe("clause/2") {
    it("should pass simple test #1") {
      prolog.setTheory(new Theory(":- dynamic(cat/0). cat. :- dynamic(dog/0). dog :- true. elk(X) :- moose(X). :- dynamic(legs/2). legs(A, 6) :- insect(A). legs(A, 7) :- A, call(A). :- dynamic(insect/1). insect(ant). insect(bee)."))
      val solution = prolog.solve("clause(cat, true).")
      solution.isSuccess shouldBe true
    }
    it("should pass simple test #2") {
      prolog.setTheory(new Theory(":- dynamic(cat/0). cat. :- dynamic(dog/0). dog :- true. elk(X) :- moose(X). :- dynamic(legs/2). legs(A, 6) :- insect(A). legs(A, 7) :- A, call(A). :- dynamic(insect/1). insect(ant). insect(bee)."))
      val solution = prolog.solve("clause(dog, true).")
      solution.isSuccess shouldBe true
    }
    it("should pass simple test #3") {
      prolog.setTheory(new Theory(":- dynamic(cat/0). cat. :- dynamic(dog/0). dog :- true. elk(X) :- moose(X). :- dynamic(legs/2). legs(A, 6) :- insect(A). legs(A, 7) :- A, call(A). :- dynamic(insect/1). insect(ant). insect(bee)."))
      val solution = prolog.solve("clause(legs(I, 6), Body).")
      solution.isSuccess shouldBe true
    }
    it("should pass negative test #1") {
      prolog.setTheory(new Theory(":- dynamic(cat/0). cat. :- dynamic(dog/0). dog :- true. elk(X) :- moose(X). :- dynamic(legs/2). legs(A, 6) :- insect(A). legs(A, 7) :- A, call(A). :- dynamic(insect/1). insect(ant). insect(bee)."))
      val solution = prolog.solve("clause(x, Body).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #2") {
      prolog.setTheory(new Theory(":- dynamic(cat/0). cat. :- dynamic(dog/0). dog :- true. elk(X) :- moose(X). :- dynamic(legs/2). legs(A, 6) :- insect(A). legs(A, 7) :- A, call(A). :- dynamic(insect/1). insect(ant). insect(bee)."))
      val solution = prolog.solve("clause(legs(A, 6), insect(f(A))).")
      solution.isSuccess shouldBe false
    }

    it("should pass variable test #1") {
      prolog.setTheory(new Theory(":- dynamic(cat/0). cat. :- dynamic(dog/0). dog :- true. elk(X) :- moose(X). :- dynamic(legs/2). legs(A, 6) :- insect(A). legs(A, 7) :- A, call(A). :- dynamic(insect/1). insect(ant). insect(bee)."))
      val solution = prolog.solve("clause(legs(I, 6), Body).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("Body").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "insect(I)"
    }

    it("should pass variable test #2") {
      prolog.setTheory(new Theory(":- dynamic(cat/0). cat. :- dynamic(dog/0). dog :- true. elk(X) :- moose(X). :- dynamic(legs/2). legs(A, 6) :- insect(A). legs(A, 7) :- A, call(A). :- dynamic(insect/1). insect(ant). insect(bee)."))
      val solution = prolog.solve("clause(legs(C, 7), Body).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("Body").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "','(C,call(C))"
    }

    it("should pass variable test #3") {
      prolog.setTheory(new Theory(":- dynamic(cat/0). cat. :- dynamic(dog/0). dog :- true. elk(X) :- moose(X). :- dynamic(legs/2). legs(A, 6) :- insect(A). legs(A, 7) :- A, call(A). :- dynamic(insect/1). insect(ant). insect(bee)."))
      var solution = prolog.solve("clause(insect(I), T).")
      solution.isSuccess shouldBe true

      var result1 = solution.getVarValue("I").asInstanceOf[Struct]
      replaceUnderscore(result1.toString) shouldBe "ant"

     var result2 = solution.getVarValue("T").asInstanceOf[Struct]
      replaceUnderscore(result1.toString) shouldBe "true"

      solution = prolog.solveNext()
      result1 = solution.getVarValue("I").asInstanceOf[Struct]
      replaceUnderscore(result1.toString) shouldBe "bee"

      result2 = solution.getVarValue("T").asInstanceOf[Struct]
      replaceUnderscore(result1.toString) shouldBe "true"
    }

    it("should pass exception test #1") {
      val ex = getExceptionListener
      prolog.setTheory(new Theory(":- dynamic(cat/0). cat. :- dynamic(dog/0). dog :- true. elk(X) :- moose(X). :- dynamic(legs/2). legs(A, 6) :- insect(A). legs(A, 7) :- A, call(A). :- dynamic(insect/1). insect(ant). insect(bee)."))
      prolog.addExceptionListener(ex)
      val solution = prolog.solve("clause(_, B).")
      ex.exFound shouldBe true
    }

    it("should pass exception test #2") {
      val ex = getExceptionListener
      prolog.setTheory(new Theory(":- dynamic(cat/0). cat. :- dynamic(dog/0). dog :- true. elk(X) :- moose(X). :- dynamic(legs/2). legs(A, 6) :- insect(A). legs(A, 7) :- A, call(A). :- dynamic(insect/1). insect(ant). insect(bee)."))
      prolog.addExceptionListener(ex)
      val solution = prolog.solve("clause(4, X).")
      ex.exFound shouldBe true
    }

    it("should pass exception test #3") {
      val ex = getExceptionListener
      prolog.setTheory(new Theory(":- dynamic(cat/0). cat. :- dynamic(dog/0). dog :- true. elk(X) :- moose(X). :- dynamic(legs/2). legs(A, 6) :- insect(A). legs(A, 7) :- A, call(A). :- dynamic(insect/1). insect(ant). insect(bee)."))
      prolog.addExceptionListener(ex)
      val solution = prolog.solve("clause(elk(N), Body).")
      ex.exFound shouldBe true
    }

    it("should pass exception test #4") {
      val ex = getExceptionListener
      prolog.setTheory(new Theory(":- dynamic(cat/0). cat. :- dynamic(dog/0). dog :- true. elk(X) :- moose(X). :- dynamic(legs/2). legs(A, 6) :- insect(A). legs(A, 7) :- A, call(A). :- dynamic(insect/1). insect(ant). insect(bee)."))
      prolog.addExceptionListener(ex)
      val solution = prolog.solve("clause(atom(_), Body).")
      ex.exFound shouldBe true
    }
  }

  describe("current_predicate/1") {
    it("should pass simple test #1") {
      prolog.setTheory(new Theory(":- dynamic(cat/0). cat. :- dynamic(dog/0). dog :- true. elk(X) :- moose(X). :- dynamic(legs/2). legs(A, 6) :- insect(A). legs(A, 7) :- A, call(A). :- dynamic(insect/1). insect(ant). insect(bee)."))
      val solution = prolog.solve("current_predicate(dog/0).")
      solution.isSuccess shouldBe true
    }

    it("should pass negative test #1") {
      val solution = prolog.solve("current_predicate(current_predicate/1).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #2") {
      prolog.setTheory(new Theory(":- dynamic(cat/0). cat. :- dynamic(dog/0). dog :- true. elk(X) :- moose(X). :- dynamic(legs/2). legs(A, 6) :- insect(A). legs(A, 7) :- A, call(A). :- dynamic(insect/1). insect(ant). insect(bee)."))
      val solution = prolog.solve("current_predicate(foo/A).")
      solution.isSuccess shouldBe false
    }

    it("should pass variable test #1") {
      prolog.setTheory(new Theory(":- dynamic(cat/0). cat. :- dynamic(dog/0). dog :- true. elk(X) :- moose(X). :- dynamic(legs/2). legs(A, 6) :- insect(A). legs(A, 7) :- A, call(A). :- dynamic(insect/1). insect(ant). insect(bee)."))
      val solution = prolog.solve("current_predicate(elk/Arity).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("Arity")
      replaceUnderscore(result.toString) shouldBe "1"
    }

    it("should pass variable test #2") {
      prolog.setTheory(new Theory("\t:- dynamic(cat/0). cat. :- dynamic(dog/0). dog :- true. elk(X) :- moose(X). :- dynamic(legs/2). legs(A, 6) :- insect(A). legs(A, 7) :- A, call(A). :- dynamic(insect/1). insect(ant). insect(bee)."))
      val solution = prolog.solve("current_predicate(Name/1).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("Name").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "elk"
    }
  }
}
