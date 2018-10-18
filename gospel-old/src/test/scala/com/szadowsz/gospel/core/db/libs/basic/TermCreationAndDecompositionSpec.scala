package com.szadowsz.gospel.core.db.libs.basic

import com.szadowsz.gospel.core.{BaseEngineSpec, PrologEngine}
import com.szadowsz.gospel.core.data.{Number, Struct, Var}
import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TermCreationAndDecompositionSpec extends FunSpec with BaseEngineSpec {

  override protected def init(): PrologEngine = new PrologEngine()

  describe("functor/3") {
    it("should pass simple test #1") {
      val solution = prolog.solve("functor(foo(a, b, c), foo, 3).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #2") {
      val solution = prolog.solve("functor([_|_], '.', 2).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #3") {
      val solution = prolog.solve("functor([], [], 0).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #4") {
      val solution = prolog.solve("functor(foo(a,b,c),X,Y).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #5") {
      val solution = prolog.solve("functor(X,foo,3).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #6") {
      val solution = prolog.solve("functor(X,foo,0).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #7") {
      val solution = prolog.solve("functor(mats(A,B), A, B).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #8") {
      val solution = prolog.solve("functor(1, X, Y).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #9") {
      val solution = prolog.solve("functor(X, 1.1, 0).")
      solution.isSuccess shouldBe true
    }

    it("should pass negative test #1") {
      val solution = prolog.solve("functor(foo(a), foo, 2).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #2") {
      val solution = prolog.solve("functor(foo(a), fo, 1).")
      solution.isSuccess shouldBe false
    }

    it("should pass variable test #1") {
      val solution = prolog.solve("functor(foo(a, b, c), X, Y).")
      solution.isSuccess shouldBe true

      val x = solution.getVar("X")
      x shouldBe new Struct("foo")

      val y = solution.getVar("Y").asInstanceOf[Number]
      y.intValue shouldBe 3
    }

    it("should pass variable test #2") {
      val solution = prolog.solve("functor(X, foo, 3).")
      solution.isSuccess shouldBe true

      val x = solution.getVar("X")
      replaceUnderscore(x.toString) shouldBe "foo(_,_,_)"

    }

    it("should pass variable test #3") {
      val solution = prolog.solve("functor(X, foo, 0).")
      solution.isSuccess shouldBe true

      val x = solution.getVar("X")
      x shouldBe new Struct("foo")
    }

    it("should pass variable test #4") {
      val solution = prolog.solve("functor(mats(A, B), A, B).")
      solution.isSuccess shouldBe true

      val a = solution.getVar("A")
      a shouldBe new Struct("mats")

      val b = solution.getVar("B").asInstanceOf[Number]
      b.intValue shouldBe 2
    }

    it("should pass variable test #5") {
      val solution = prolog.solve("functor(1, X, Y).")
      solution.isSuccess shouldBe true

      val x = solution.getVar("X").asInstanceOf[Number]
      x.intValue shouldBe 1

      val y = solution.getVar("Y").asInstanceOf[Number]
      y.intValue shouldBe 0
    }


    it("should pass variable test #6") {
      val solution = prolog.solve("functor(X, 1.1, 0).")
      solution.isSuccess shouldBe true

      val x = solution.getVar("X").asInstanceOf[Number]
      x.doubleValue shouldBe 1.1
    }

    it("should pass exception test #1") {
      //    val ex = getExceptionListener
      //    prolog.addExceptionListener(ex)
      val solution = prolog.solve("functor(X, Y, 3).")
      //   ex.exFound shouldBe true
      solution.isSuccess shouldBe false
    }

    it("should pass exception test #2") {
      val ex = getExceptionListener
      prolog.addExceptionListener(ex)
      val solution = prolog.solve("functor(X, foo, N).")
      ex.exFound shouldBe true
    }

    it("should pass exception test #3") {
      val ex = getExceptionListener
      prolog.addExceptionListener(ex)
      val solution = prolog.solve("functor(X, foo, a).")
      ex.exFound shouldBe true
    }

    it("should pass exception test #4") {
      //    val ex = getExceptionListener
      //    prolog.addExceptionListener(ex)
      val solution = prolog.solve("functor(F, 1.5, 1).")
      //   ex.exFound shouldBe true
      solution.isSuccess shouldBe false
    }

    it("should pass exception test #5") {
      //    val ex = getExceptionListener
      //    prolog.addExceptionListener(ex)
      val solution = prolog.solve("functor(F, foo(a), 1).")
      //   ex.exFound shouldBe true
      solution.isSuccess shouldBe false
    }

    it("should pass exception test #6") {
      //    val ex = getExceptionListener
      //    prolog.addExceptionListener(ex)
      val solution = prolog.solve("current_prolog_flag(max_arity, A), X is A + 1, functor(T, foo, X).")
   //   ex.exFound shouldBe true
      solution.isSuccess shouldBe false
    }

    it("should pass exception test #7") {
      //    val ex = getExceptionListener
      //    prolog.addExceptionListener(ex)
      val solution = prolog.solve("Minus1 is 0 - 1, functor(F, foo, Minus1).")
      //ex.exFound shouldBe true
      solution.isSuccess shouldBe false
    }
  }

  describe("arg/3") {
    it("should pass simple test #1") {
      val solution = prolog.solve("arg(1, foo(a,b), a).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #2") {
      val solution = prolog.solve("arg(1, foo(a,b), X).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #3") {
      val solution = prolog.solve("arg(1, foo(a,b), X).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #4") {
      val solution = prolog.solve("arg(1, foo(X,b), a).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #5") {
      val solution = prolog.solve("arg(1, foo(X,b), Y).")
      solution.isSuccess shouldBe true
    }

    it("should pass negative test #1") {
      val solution = prolog.solve("arg(1, foo(a, b), b).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #2") {
      val solution = prolog.solve("arg(0, foo(a, b), foo).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #3") {
      val solution = prolog.solve("arg(3, foo(3, 4), N).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #4") {
      val solution = prolog.solve("arg(1, foo(X), u(X)).")
      solution.isSuccess shouldBe false
    }

    it("should pass variable test #1") {
      val solution = prolog.solve("arg(1, foo(a, b), X).")
      solution.isSuccess shouldBe true

      val x = solution.getVar("X")
      x shouldBe new Struct("a")
    }

    it("should pass variable test #2") {
      val solution = prolog.solve("arg(1, foo(X, b), a).")
      solution.isSuccess shouldBe true

      val x = solution.getVar("X")
      x shouldBe new Struct("a")
    }

    it("should pass variable test #3") {
      val solution = prolog.solve("arg(1, foo(X, b), Y).")
      solution.isSuccess shouldBe true

      val x = solution.getVar("X")
      x shouldBe new Var("X")
    }

    it("should pass exception test #1") {
      val ex = getExceptionListener
      prolog.addExceptionListener(ex)
      val solution = prolog.solve("arg(X, foo(a,b), a).")
      ex.exFound shouldBe true
    }

    it("should pass exception test #2") {
      val ex = getExceptionListener
      prolog.addExceptionListener(ex)
      val solution = prolog.solve("arg(1, X, a).")
      ex.exFound shouldBe true
    }

    it("should pass exception test #3") {
      val ex = getExceptionListener
      prolog.addExceptionListener(ex)
      val solution = prolog.solve("arg(0, atom, a).")
      ex.exFound shouldBe true
    }

    it("should pass exception test #4") {
      val ex = getExceptionListener
      prolog.addExceptionListener(ex)
      val solution = prolog.solve("arg(0, 3, a).")
      ex.exFound shouldBe true
    }
  }

  describe("=../2") {
    it("should pass simple test #1") {
      val solution = prolog.solve("'=..'(foo(a,b), [foo, a, b]).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #2") {
      val solution = prolog.solve("'=..'(1, [1]).")
      solution.isSuccess shouldBe true
    }

    it("should pass negative test #1") {
      val solution = prolog.solve("'=..'(foo(a,b), [foo, b, a]).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #2") {
      val solution = prolog.solve("'=..'(f(X), [f, u(X)]).")
      solution.isSuccess shouldBe false
    }

    it("should pass variable test #1") {
      val solution = prolog.solve("'=..'(X, [foo,a,b]).")
      solution.isSuccess shouldBe true

      val x = solution.getVar("X")
      x shouldBe new Struct("foo", new Struct("a"), new Struct("b"))
    }

    it("should pass variable test #2") {
      val solution = prolog.solve("'=..'(foo(a,b), L).")
      solution.isSuccess shouldBe true

      val x = solution.getVar("L")
      x shouldBe prolog.createTerm("[foo,a,b]")
    }

    it("should pass variable test #3") {
      val solution = prolog.solve("'=..'(foo(X, b), [foo, a, Y]).")
      solution.isSuccess shouldBe true

      val x = solution.getVar("Y")
      x shouldBe new Struct("b")
    }

    it("should pass exception test #1") {
      //    val ex = getExceptionListener
      //    prolog.addExceptionListener(ex)
      val solution = prolog.solve("'=..'(X, Y).")
      solution.isSuccess shouldBe false
      //   ex.exFound shouldBe true
    }

    it("should pass exception test #2") {
      //    val ex = getExceptionListener
      //    prolog.addExceptionListener(ex)
      val solution = prolog.solve("'=..'(X, [foo, a | Y]).")
      solution.isSuccess shouldBe false
      //   ex.exFound shouldBe true
    }

    it("should pass exception test #3") {
      //    val ex = getExceptionListener
      //    prolog.addExceptionListener(ex)
      val solution = prolog.solve("'=..'(X, [foo | bar]).")
      solution.isSuccess shouldBe false
      //   ex.exFound shouldBe true
    }

    it("should pass exception test #4") {
      //    val ex = getExceptionListener
      //    prolog.addExceptionListener(ex)
      val solution = prolog.solve("'=..'(X, [Foo, bar]).")
      solution.isSuccess shouldBe false
      //   ex.exFound shouldBe true
    }

    it("should pass exception test #5") {
      //    val ex = getExceptionListener
      //    prolog.addExceptionListener(ex)
      val solution = prolog.solve("'=..'(X, [3, 1]).")
      solution.isSuccess shouldBe false
      //   ex.exFound shouldBe true
    }

    it("should pass exception test #6") {
      //    val ex = getExceptionListener
      //    prolog.addExceptionListener(ex)
      val solution = prolog.solve("'=..'(X, [1.1, foo]).")
      solution.isSuccess shouldBe false
      //   ex.exFound shouldBe true
    }

    it("should pass exception test #7") {
      //    val ex = getExceptionListener
      //    prolog.addExceptionListener(ex)
      val solution = prolog.solve("'=..'(X, [a(b), 1]).")
      solution.isSuccess shouldBe false
      //   ex.exFound shouldBe true
    }

    it("should pass exception test #8") {
      //    val ex = getExceptionListener
      //    prolog.addExceptionListener(ex)
      val solution = prolog.solve("'=..'(X, 4).")
      solution.isSuccess shouldBe false
      //   ex.exFound shouldBe true
    }
  }

  describe("copy_term/2") {
    it("should pass simple test #1") {
      val solution = prolog.solve("copy_term(X, Y).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #2") {
      val solution = prolog.solve("copy_term(X, 3).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #3") {
      val solution = prolog.solve("copy_term(_, a).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #4") {
      val solution = prolog.solve("copy_term(_, _).")
      solution.isSuccess shouldBe true
    }

    it("should pass negative test #1") {
      val solution = prolog.solve("copy_term(a,b).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #2") {
      val solution = prolog.solve("copy_term(a+X, X+b), copy_term(a+X, X+b).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #3") {
      val solution = prolog.solve("copy_term(demoen(X, X), demoen(Y, f(Y))).")
      solution.isSuccess shouldBe false
    }

    it("should pass variable test #1") {
      val solution = prolog.solve("copy_term(a+X, X+b).")
      solution.isSuccess shouldBe true

      val x = solution.getVar("X")
      x shouldBe new Struct("a")
    }

    it("should pass variable test #2") {
      val solution = prolog.solve("copy_term(X+X+Y, A+B+B).")
      solution.isSuccess shouldBe true

      val x = solution.getVar("A")
      x shouldBe new Var("A")
    }
  }
}
