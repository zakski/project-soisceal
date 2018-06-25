package com.szadowsz.gospel.core.db.libs

import com.szadowsz.gospel.core.data.{Int, Struct, Var}
import com.szadowsz.gospel.core.{BaseEngineSpec, PrologEngine, Solution, Theory}
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner

/**
  * Created on 18/02/2017.
  */
@RunWith(classOf[JUnitRunner])
class DCGLibrarySpec extends FlatSpec with BaseEngineSpec {

  override protected def init(): PrologEngine = {
    val p = new PrologEngine()
    p.loadLibrary(classOf[DCGLibrary])
    p
  }

  behavior of "Definite Clause Grammars"

  it should "pass simple test #1" in {
    prolog.setTheory(new Theory("a --> []."))
    val solution = prolog.solve("phrase(a, []).")
    solution.isSuccess shouldBe true
  }

  it should "pass simple test #2" in {
    prolog.setTheory(new Theory("a --> [a], b. b --> []."))
    val solution = prolog.solve("phrase(a, [a]).")
    solution.isSuccess shouldBe true
  }

  it should "pass simple test #3" in {
    prolog.setTheory(new Theory("a --> [a], b. b --> []."))
    val solution = prolog.solve("phrase(b, []).")
    solution.isSuccess shouldBe true
  }

  it should "pass simple test #4" in {
    prolog.setTheory(new Theory("a --> [a], a, [d]. a --> [b], a, [d]. a --> [c]."))
    val solution = prolog.solve("phrase(a, [a, b, a, a, c, d, d, d, d]).")
    solution.isSuccess shouldBe true
  }

  it should "pass simple test #5" in {
    prolog.setTheory(new Theory("x(V) --> [a], x(V), [a]. x(V) --> [V]."))
    val solution = prolog.solve("phrase(x(1), [a, a, a, 1, a, a, a]).")
    solution.isSuccess shouldBe true
  }

  it should "pass simple test #6" in {
    prolog.setTheory(new Theory("x --> [a], x, [a]. x --> [V], { number(V) }."))
    val solution = prolog.solve("phrase(x, [a, a, a, 151, a, a, a]).")
    solution.isSuccess shouldBe true
  }

  it should "pass simple test #7" in {
    prolog.setTheory(new Theory("e --> o, et. et --> []. et --> ['+'], e. o --> ['('], e, [')']. o --> [X], { number(X) }."))
    val solution = prolog.solve("phrase(e, [1, '+', '(', 2, '+', 3, ')', '+', 4]).")
    solution.isSuccess shouldBe true
  }

  it should "pass simple test #8" in {
    prolog.setTheory(new Theory("e --> t, et. et --> []. et --> [and], e. t --> ['0']. t --> ['1']."))
    val solution = prolog.solve("phrase(e, ['0']).")
    solution.isSuccess shouldBe true
  }

  it should "should pass variable test #1" in {
    prolog.setTheory(new Theory("e(V) --> o(V1), et(V1, V). et(V, V) --> []. et(VI, VO) --> ['+'], o(V1), { VI1 is VI + V1 }, et(VI1, VO). o(V) --> ['('], e" + "(V), [')']. o(X) --> [X], { number(X) }."))
    val solution = prolog.solve("phrase(e(V), [1, '+', '(', 2, '+', 3, ')']).")
    solution.isSuccess shouldBe true

    val result = solution.getVar("V")
    replaceUnderscore(result.toString) shouldBe "6"
  }

  it should "should pass variable test #2" in {
    prolog.setTheory(new Theory("e(V) --> t(W), et(W, V). et(V, V) --> []. et(W, V) --> [and], t(V1), { W = 1, V1 = 1, !, W2 = 1 ; W2 = 0 }, et(W2, V). t(0) " + "--> ['0']. t(1) --> ['1']."))
    val solution = prolog.solve("phrase(e(V), ['1']).")
    solution.isSuccess shouldBe true

    val result = solution.getVar("V")
    replaceUnderscore(result.toString) shouldBe "1"
  }

  it should "throw an instantiation error when phrase (X, []) is called" in {
    val goal = "catch(phrase(X, []), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("phrase_guard", new Var("X"), new Struct)
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
  }

  it should "throw an instantiation error when phrase(X, [], []) is called" in {
    val goal = "catch(phrase(X, [], []), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("phrase_guard", new Var("X"), new Struct, new Struct)
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
  }
}
