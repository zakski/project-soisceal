package com.szadowsz.gospel.core.db.libs.basic

import com.szadowsz.gospel.core.data.{Int, Struct}
import com.szadowsz.gospel.core.{BaseEngineSpec, PrologEngine, Theory}
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ThrowCatchSpec extends FlatSpec with BaseEngineSpec {

  override protected def init(): PrologEngine = new PrologEngine()

  behavior of "throw/1 and catch/3"

  it should "execute the throw/catch with the substitutions made in unification" in {
    val theory = "p(0) :- p(1). p(1) :- throw(error)."
    prolog.setTheory(new Theory(theory))
    val goal = "atom_length(err, 3), catch(p(0), E, (atom_length(E, Length), X is 2+3)), Y is X+5."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val e = info.getVar("E").asInstanceOf[Struct]
    e shouldBe new Struct("error")
    val length = info.getVar("Length").asInstanceOf[Int]
    length.intValue shouldBe 5
    val x = info.getVar("X").asInstanceOf[Int]
    x.intValue shouldBe 5
    val y = info.getVar("Y").asInstanceOf[Int]
    y.intValue shouldBe 10
  }

  it should "execute the nearest catch/3 ancestor in the tree of resolution whose second argument unifies with the throw/1 argument" in {
    val theory = "p(0) :- throw(error). p(1)."
    prolog.setTheory(new Theory(theory))
    val goal = "catch(p(1), E, fail), catch(p(0), E, atom_length(E, Length))."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val e = info.getVar("E").asInstanceOf[Struct]
    e shouldBe new Struct("error")
    val length = info.getVar("Length").asInstanceOf[Int]
    length.intValue shouldBe 5
  }

  it should "fail to execute if an error occurs and no catch/3 ancestor is found whose second argument unifies with the argument of the exception" in {
    val theory = "p(0) :- throw(error)."
    prolog.setTheory(new Theory(theory))
    val goal = "catch(p(0), error(X), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe false
    info.isHalted shouldBe true
  }

  it should "fail to execute if catch/3's handler is false" in {
    val theory = "p(0) :- throw(error)."
    prolog.setTheory(new Theory(theory))
    val goal = "catch(p(0), E, E == err)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe false
  }

   it should "cut all choice points of a non-deterministic Goal if an exception is thrown" in {
    val theory = "p(0). p(1) :- throw(error). p(2)."
    prolog.setTheory(new Theory(theory))
    val goal = "catch(p(X), E, E == error)."
    var info = prolog.solve(goal)
    info.isSuccess shouldBe true
    info.hasOpenAlternatives shouldBe true
    info = prolog.solveNext()
    info.isSuccess shouldBe true
    info.hasOpenAlternatives shouldBe false
  }

  it should "fail to execute if catch/3's handler throws an exception" in {
    val theory = "p(0) :- throw(error)."
    prolog.setTheory(new Theory(theory))
    val goal = "catch(p(0), E, throw(err))."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe false
    info.isHalted shouldBe true
  }
}
