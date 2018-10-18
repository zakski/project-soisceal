package com.szadowsz.gospel.core

import com.szadowsz.gospel.core.data.Struct
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, FunSpec}

/**
  * Created on 16/02/2017.
  */
@RunWith(classOf[JUnitRunner])
class BugFixesSpec extends FunSpec with BaseEngineSpec {

  protected override def init(): PrologEngine = new PrologEngine()

  describe("number unification") {
    it("real numbers should not unify with integers even if they are equal") {
      val solution = prolog.solve("0.0 = 0.")
      solution.isSuccess shouldBe false
    }

    it("real numbers should not unify with integers if they are equal") {
      val solution = prolog.solve("0.9 = 0.")
      solution.isSuccess shouldBe false
    }

    it("integers should not unify with integers if they are not equal") {
      val solution = prolog.solve("0 = 0.9.")
      solution.isSuccess shouldBe false
    }
  }

  describe("number comparison") {
    it("real numbers should not equal integers even if they are equal") {
      val solution = prolog.solve("0.0 == 0.")
      solution.isSuccess shouldBe false
    }

    it("integers should not equal integers even if they are equal") {
      val solution = prolog.solve("1 = 1.0.")
      solution.isSuccess shouldBe false
    }
  }

  describe("operator management") {
    it("current_op should unify to true") {
      val solution = prolog.solve("current_op(_, _, '+').")
      solution.isSuccess shouldBe true
    }

    it("current_op should unify Op to ':' and '@'") {
      var solution = prolog.solve("op(10, yfx, ['@', ':']), current_op(10, yfx, Op).")
      solution.isSuccess shouldBe true

      var op = solution.getVar("Op").asInstanceOf[Struct]
      op.getName shouldBe ":"

      solution = prolog.solveNext()
      solution.isSuccess shouldBe true

      op = solution.getVar("Op").asInstanceOf[Struct]
      op.getName shouldBe "@"
    }
  }

  describe("expanding subgoals") {
    it("the expansion of subgoals in flat lists should be successful") {
      prolog.setTheory(new Theory(" a. p((a,fail)). p((a))."))
      val solution = prolog.solve("p(X), X.")
      solution.isSuccess shouldBe true

      var op = solution.getVar("X").asInstanceOf[Struct]
      op.getName shouldBe "a"
    }
  }

  describe("functor identification") {
    it("the identification of functors (and their subsequent evaluation) in subgoals should be successful #1") {
      var solution = prolog.solve("X is 5, Y =.. ['+', X, 2], K is Y.")
      solution.isSuccess shouldBe true

      var op = solution.getVar("X")
      op.toString shouldBe "5"

      op = solution.getVar("Y")
      op.toString shouldBe "'+'(5,2)"

      op = solution.getVar("K")
      op.toString shouldBe "7"
    }

    it("the identification of functors (and their subsequent evaluation) in subgoals should be successful #2") {
      var solution = prolog.solve("X is 5, Y =.. ['+', X, 2], 10 > Y.")
      solution.isSuccess shouldBe true

      var op = solution.getVar("X")
      op.toString shouldBe "5"

      op = solution.getVar("Y")
      op.toString shouldBe "'+'(5,2)"
    }
  }

  describe("asserting/retracting clauses") {
    it("You cannot retract clauses belonging to a library's theory") {
      var solution = prolog.solve("retract(call(X)).")
      solution.isSuccess shouldBe false
    }
  }

  describe("arithmetic operations on long numbers and on boundaries between int and long") {

    it("arithmetic operations test #1") {
      prolog.setTheory(new Theory("ops(s). ops(y). ops(z)."))
      val solution = prolog.solve("ops(A), assert(ops_result(A)).")
      solution.isSuccess shouldBe true
    }

    it("arithmetic operations test #2") {
      prolog.setTheory(new Theory("p(0) :- !. p(1)."))
      val solution = prolog.solve("retractall(p(X)).")
      solution.isSuccess shouldBe true
    }

    it("arithmetic operations test #3") {
      prolog.setTheory(new Theory("p(0) :- !. p(1)."))
      val solution = prolog.solve("p(X).")
      solution.isSuccess shouldBe true
    }

    it("arithmetic operations test #4") {
      prolog.setTheory(new Theory("ops(s). ops(y). ops(z)."))
      var solution = prolog.solve("ops(X).")
      solution.isSuccess shouldBe true

      var op = solution.getVar("X")
      op.toString shouldBe "s"

      solution = prolog.solveNext()
      op = solution.getVar("X")
      op.toString shouldBe "y"

      solution = prolog.solveNext()
      op = solution.getVar("X")
      op.toString shouldBe "z"
    }
  }
}
