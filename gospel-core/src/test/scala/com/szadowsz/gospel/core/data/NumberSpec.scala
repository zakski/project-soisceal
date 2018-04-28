package com.szadowsz.gospel.core.data

import com.szadowsz.gospel.core.{BaseEngineSpec, PrologEngine}
import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class NumberSpec extends FunSpec with BaseEngineSpec {

  override protected def init(): PrologEngine = new PrologEngine()

  describe("Int") {

    it("should be atomic") {
      new Int(0).isAtomic should be (true)
    }

    it("should not be an atom") {
      new Int(0).isAtom should be (false)
    }

    it("should not be a compound") {
      new Int(0).isCompound should be (false)
    }

    it("should not be equal to a struct") {
      new Int(0) should not be new Struct()
    }

    it("should not be equal to a var") {
      new Int(1) should not be new Var("X")
    }

    it("should not be equal to an int of a different value") {
      new Int(0) should not be new Int(1)
    }

    it("should be equal to an int of the same value") {
      new Int(0) should be (new Int(0))
    }

    it("long equality")(pending) // TODO Test Int numbers for equality with Long numbers

    it("should not be equal to a double of the same value") {
      new Int(1) should not be new Float(1)
    }

    it("float equality")(pending) // TODO Test Int numbers for equality with Long numbers
  }

  describe("Double") {

    it("should be atomic") {
      new Float(0).isAtomic should be (true)
    }

    it("should not be an atom") {
      new Float(0).isAtom should be (false)
    }

    it("should not be a compound") {
      new Float(0).isCompound should be (false)
    }

    it("should not be equal to a struct") {
      new Float(0) should not be new Struct()
    }

    it("should not be equal to a var") {
      new Float(1) should not be new Var("X")
    }

    it("should not be equal to a double of a different value") {
      new Float(0) should not be new Float(1)
    }

    it("should be equal to a double of the same value") {
      new Float(0) should be (new Float(0))
    }

    it("long equality")(pending) // TODO Test Double numbers for equality with Long numbers

    it("should not be equal to an int of the same value") {
      new Float(1) should not be new Int(1)
    }

    it("float equality")(pending) // TODO Test Double numbers for equality with Long numbers
  }
}
