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
class ClauseCreationAndDestructionSpec extends FunSpec with BaseEngineSpec {

  override protected def init(): PrologEngine = new PrologEngine()

  describe("asserta/1") {
    it("should pass simple test #1") {
      prolog.setTheory(new Theory(":- dynamic(legs/2). legs(A, 6) :- insect(A). :- dynamic(insect/1). insect(ant). insect(bee)."))
      val solution = prolog.solve("asserta(legs(octopus, 8)).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #2") {
      prolog.setTheory(new Theory(":- dynamic(legs/2). legs(A, 6) :- insect(A). :- dynamic(insect/1). insect(ant). insect(bee)."))
      val solution = prolog.solve("asserta((legs(A, 4) :- animal(A))).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #3") {
      prolog.setTheory(new Theory(":- dynamic(legs/2). legs(A, 6) :- insect(A). :- dynamic(insect/1). insect(ant). insect(bee)."))
      val solution = prolog.solve("asserta((foo(X) :- X, call(X))).")
      solution.isSuccess shouldBe true
    }

    it("should pass exception test #1") {
      val ex = getExceptionListener
      prolog.addExceptionListener(ex)
      prolog.setTheory(new Theory(":- dynamic(legs/2). legs(A, 6) :- insect(A). :- dynamic(insect/1). insect(ant). insect(bee)."))
      val solution = prolog.solve("asserta(_).")
      ex.exFound shouldBe true
      ex.exMsg shouldBe "Instantiation error in argument 1 of asserta(_90307)"
    }

    it("should pass exception test #2") {
      val ex = getExceptionListener
      prolog.addExceptionListener(ex)
      prolog.setTheory(new Theory(":- dynamic(legs/2). legs(A, 6) :- insect(A). :- dynamic(insect/1). insect(ant). insect(bee)."))
      val solution = prolog.solve("asserta(4).")
      ex.exFound shouldBe true
      ex.exMsg shouldBe "Type error in argument 1 of asserta(4)"
    }

    it("should pass exception test #3") {
      val ex = getExceptionListener
      prolog.addExceptionListener(ex)
      prolog.setTheory(new Theory(":- dynamic(legs/2). legs(A, 6) :- insect(A). :- dynamic(insect/1). insect(ant). insect(bee)."))
      val solution = prolog.solve("asserta((foo :- 4)).")
      ex.exFound shouldBe true
      ex.exMsg shouldBe "Type error in argument 1 of asserta(':-'(foo,4))"
    }

    it("should pass exception test #4") {
      val ex = getExceptionListener
      prolog.addExceptionListener(ex)
      prolog.setTheory(new Theory(":- dynamic(legs/2). legs(A, 6) :- insect(A). :- dynamic(insect/1). insect(ant). insect(bee)."))
      val solution = prolog.solve("asserta((atom(_) :- true)).")
      ex.exFound shouldBe true
    }
  }

  describe("assertz/1") {
    it("should pass simple test #1") {
      prolog.setTheory(new Theory(":- dynamic(legs/2). legs(A, 4) :- animal(A). legs(octopus, 8). legs(A, 6) :- insect(A). :- dynamic(insect/1). insect(ant). insect(bee). :- dynamic(foo/1). foo(X) :- call(X), call(X)."))
      val solution = prolog.solve("assertz(legs(spider, 8)).")
      solution.isSuccess shouldBe true
    }
    it("should pass simple test #2") {
      prolog.setTheory(new Theory(":- dynamic(legs/2). legs(A, 4) :- animal(A). legs(octopus, 8). legs(A, 6) :- insect(A). :- dynamic(insect/1). insect(ant). insect(bee). :- dynamic(foo/1). foo(X) :- call(X), call(X)."))
      val solution = prolog.solve("assertz((legs(B, 2) :- bird(B))).")
      solution.isSuccess shouldBe true
    }
    it("should pass simple test #3") {
      prolog.setTheory(new Theory(":- dynamic(legs/2). legs(A, 4) :- animal(A). legs(octopus, 8). legs(A, 6) :- insect(A). :- dynamic(insect/1). insect(ant). insect(bee). :- dynamic(foo/1). foo(X) :- call(X), call(X)."))
      val solution = prolog.solve("assertz((foo(X) :- X -> call(X))).")
      solution.isSuccess shouldBe true
    }
    it("should pass exception test #1") {
      val ex = getExceptionListener
      prolog.addExceptionListener(ex)
      prolog.setTheory(new Theory(":- dynamic(legs/2). legs(A, 6) :- insect(A). :- dynamic(insect/1). insect(ant). insect(bee)."))
      val solution = prolog.solve("assertz(_).")
      ex.exFound shouldBe true
      ex.exMsg shouldBe "Instantiation error in argument 1 of assertz(_105032)"
    }

    it("should pass exception test #2") {
      val ex = getExceptionListener
      prolog.addExceptionListener(ex)
      prolog.setTheory(new Theory(":- dynamic(legs/2). legs(A, 6) :- insect(A). :- dynamic(insect/1). insect(ant). insect(bee)."))
      val solution = prolog.solve("assertz(4).")
      ex.exFound shouldBe true
      ex.exMsg shouldBe "Type error in argument 1 of assertz(4)"
    }

    it("should pass exception test #3") {
      val ex = getExceptionListener
      prolog.addExceptionListener(ex)
      prolog.setTheory(new Theory(":- dynamic(legs/2). legs(A, 6) :- insect(A). :- dynamic(insect/1). insect(ant). insect(bee)."))
      val solution = prolog.solve("assertz((foo :- 4)).")
      ex.exFound shouldBe true
      ex.exMsg shouldBe "Type error in argument 1 of assertz(':-'(foo,4))"
    }

    it("should pass exception test #4") {
      val ex = getExceptionListener
      prolog.addExceptionListener(ex)
      prolog.setTheory(new Theory(":- dynamic(legs/2). legs(A, 6) :- insect(A). :- dynamic(insect/1). insect(ant). insect(bee)."))
      val solution = prolog.solve("assertz((atom(_) :- true)).")
      ex.exFound shouldBe true
    }
  }

  describe("retract/1") {
    it("should pass simple test #1") {
      prolog.setTheory(new Theory(":- dynamic(legs/2). legs(A, 4) :- animal(A). legs(octopus, 8). legs(A, 6) :- insect(A). legs(spider, 8). legs(B, 2) :- bird(B). :- dynamic(insect/1). insect(ant). insect(bee). :- dynamic(foo/1). foo(X) :- call(X), call(X). foo(X) :- call(X) -> call(X)."))
      val solution = prolog.solve("retract(legs(octopus, 8)).")
      solution.isSuccess shouldBe true
    }
    it("should pass simple test #2") {
      prolog.setTheory(new Theory(":- dynamic(legs/2). legs(A, 4) :- animal(A). legs(octopus, 8). legs(A, 6) :- insect(A). legs(spider, 8). legs(B, 2) :- bird(B). :- dynamic(insect/1). insect(ant). insect(bee). :- dynamic(foo/1). foo(X) :- call(X), call(X). foo(X) :- call(X) -> call(X)."))
      val solution = prolog.solve("retract((foo(C) :- A -> B)).")
      solution.isSuccess shouldBe true
    }
    it("should pass negative test #1") {
      val solution = prolog.solve("retract(legs(spider, 6)).")
      solution.isSuccess shouldBe false
    }
    it("should pass negative test #2") {
      prolog.setTheory(new Theory(":- dynamic(legs/2). legs(A, 4) :- animal(A). legs(octopus, 8). legs(A, 6) :- insect(A). legs(spider, 8). legs(B, 2) :- bird(B). :- dynamic(insect/1). insect(ant). insect(bee). :- dynamic(foo/1). foo(X) :- call(X), call(X). foo(X) :- call(X) -> call(X)."))
      val solution = prolog.solve("retract(insect(I)), write(I), retract(insect(bee)), fail.")
      solution.isSuccess shouldBe false
    }

    it("should pass exception test #1") {
      val ex = getExceptionListener
      prolog.addExceptionListener(ex)
      val solution = prolog.solve("retract(insect(I)), write(I), retract(insect(bee)), fail.")
      ex.exFound shouldBe true
    }

    it("should pass exception test #2") {
      val ex = getExceptionListener
      prolog.addExceptionListener(ex)
      val solution = prolog.solve("retract((X :- in_eec(Y))). ")
      ex.exFound shouldBe true
    }

    it("should pass exception test #3") {
      val ex = getExceptionListener
      prolog.addExceptionListener(ex)
      prolog.setTheory(new Theory(":- dynamic(legs/2). legs(A, 4) :- animal(A). legs(octopus, 8). legs(A, 6) :- insect(A). legs(spider, 8). legs(B, 2) :- bird(B). :- dynamic(insect/1). insect(ant). insect(bee). :- dynamic(foo/1). foo(X) :- call(X), call(X). foo(X) :- call(X) -> call(X)."))
      val solution = prolog.solve("retract((4 :- X)).")
      ex.exFound shouldBe true
    }

    it("should pass exception test #4") {
      val ex = getExceptionListener
      prolog.addExceptionListener(ex)
      prolog.setTheory(new Theory(":- dynamic(legs/2). legs(A, 4) :- animal(A). legs(octopus, 8). legs(A, 6) :- insect(A). legs(spider, 8). legs(B, 2) :- bird(B). :- dynamic(insect/1). insect(ant). insect(bee). :- dynamic(foo/1). foo(X) :- call(X), call(X). foo(X) :- call(X) -> call(X)."))
      val solution = prolog.solve("retract((atom(X) :- X == '[]')).")
      ex.exFound shouldBe true
    }

    it("should pass variable test #1") {
      prolog.setTheory(new Theory(":- dynamic(legs/2). legs(A, 4) :- animal(A). legs(octopus, 8). legs(A, 6) :- insect(A). legs(spider, 8). legs(B, 2) :- bird(B). :- dynamic(insect/1). insect(ant). insect(bee). :- dynamic(foo/1). foo(X) :- call(X), call(X). foo(X) :- call(X) -> call(X)."))
      var solution = prolog.solve("retract((legs(X, 2) :- T)).")
      solution.isSuccess shouldBe true

      var result1 = solution.getVarValue("T").asInstanceOf[Struct]
      replaceUnderscore(result1.toString) shouldBe "bird(X)"
    }

    // TODO
    //    retract((legs(X, Y) :- Z)).	:- dynamic(legs/2). legs(A, 4) :- animal(A). legs(octopus, 8). legs(A, 6) :- insect(A). legs(spider, 8). legs(B, 2) :- bird(B). :- dynamic(insect/1). insect(ant). insect(bee). :- dynamic(foo/1). foo(X) :- call(X), call(X). foo(X) :- call(X) -> call(X).	Y	2	true
    //    retract((legs(X, Y) :- Z)).	:- dynamic(legs/2). legs(A, 4) :- animal(A). legs(octopus, 8). legs(A, 6) :- insect(A). legs(spider, 8). legs(B, 2) :- bird(B). :- dynamic(insect/1). insect(ant). insect(bee). :- dynamic(foo/1). foo(X) :- call(X), call(X). foo(X) :- call(X) -> call(X).	Z	animal(X)	true
    //    retract((legs(X, Y) :- Z)).	:- dynamic(legs/2). legs(A, 4) :- animal(A). legs(octopus, 8). legs(A, 6) :- insect(A). legs(spider, 8). legs(B, 2) :- bird(B). :- dynamic(insect/1). insect(ant). insect(bee). :- dynamic(foo/1). foo(X) :- call(X), call(X). foo(X) :- call(X) -> call(X).	Y	6	true
    //    retract((legs(X, Y) :- Z)).	:- dynamic(legs/2). legs(A, 4) :- animal(A). legs(octopus, 8). legs(A, 6) :- insect(A). legs(spider, 8). legs(B, 2) :- bird(B). :- dynamic(insect/1). insect(ant). insect(bee). :- dynamic(foo/1). foo(X) :- call(X), call(X). foo(X) :- call(X) -> call(X).	Z	insect(X)	true
  }

  describe("abolish/1") {
    it("should pass simple test #1") {
      val solution = prolog.solve("abolish(foo/2).")
      solution.isSuccess shouldBe true
    }

    it("should pass exception test #1") {
      val ex = getExceptionListener
      prolog.addExceptionListener(ex)
      val solution = prolog.solve("abolish(foo/_).")
      ex.exFound shouldBe true
    }

    it("should pass exception test #2") {
      val ex = getExceptionListener
      prolog.addExceptionListener(ex)
      val solution = prolog.solve("abolish(foo).")
      ex.exFound shouldBe true
    }

    it("should pass exception test #3") {
      val ex = getExceptionListener
      prolog.addExceptionListener(ex)
      val solution = prolog.solve("abolish(foo(_)).")
      ex.exFound shouldBe true
    }

    it("should pass exception test #4") {
      val ex = getExceptionListener
      prolog.addExceptionListener(ex)
      val solution = prolog.solve("abolish(abolish/1).")
      ex.exFound shouldBe true
    }
  }
}
