package com.szadowsz.gospel.core.data.numeric

import com.szadowsz.gospel.core.data.{numeric, Var, Term, Struct}
import org.junit.runner.RunWith
import org.scalatest.{Matchers, FlatSpec}
import org.scalatest.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
class IntTestCase extends FlatSpec with Matchers {

  "An Integer" should " be atomic" in {
    Number(0).isAtomic should be(true)
  }

  "An Integer" should " not be an atom" in {
    Number(0).isAtom should be(false)
  }

  "An Integer" should " not be a compound" in {
    Number(0).isCompound should be(false)
  }

  "An Integer" should " not equivalent to an empty Struct" in {
    val t1 : Term = Number(0)
    val t2 : Term = new Struct()
    t1 should not be t2
  }

  "An Integer" should " not equivalent to an unbound Var" in {
    val t1 : Term = Number(0)
    val t2 : Term = new Var("X")
    t1 should not be t2
  }

  "An Integer of value 1" should " not equivalent to an Integer of value 0" in {
    Number(0) should not be Number(1)
  }

  "An Integer of value 1" should " be equivalent to an Integer of value 1" in {
    Number(1) should be (Number(1))
  }
}
