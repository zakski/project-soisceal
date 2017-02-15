package com.szadowsz.gospel.core.data.numeric

import com.szadowsz.gospel.core.data.{Term, Struct, Var}
import org.scalatest.{Matchers, FlatSpec}


class DoubleTestCase  extends FlatSpec with Matchers {

	"A Double" should " be atomic" in {
		Number(0.0).isAtomic should be(true)
	}

	"A Double" should " not be an atom" in {
		Number(0.0).isAtom should be(false)
	}

	"A Double" should " not be a compound" in {
		Number(0.0).isCompound should be(false)
	}

	"A Double" should " not equivalent to an empty Struct" in {
		val t1 : Term = Number(0.0)
		val t2 : Term = new Struct()
		t1 should not be t2
	}

	"A Double" should " not equivalent to an unbound Var" in {
		val t1 : Term = Number(0.0)
		val t2 : Term = new Var("X")
		t1 should not be t2
	}

	"A Double of value 0.0" should " not equivalent to a Double of value 1.0" in {
		Number(0.0) should not be (Number(1.0))
	}

  "A Double of value 1.0" should " not equivalent to a Double of value 1.1" in {
    Number(1.0) should not be (Number(1.1))
  }

	"A Double of value 1.0" should " be equivalent to a Double of value 1.0" in {
		Number(1.0) should be (Number(1.0))
	}

  "A Double of value 1.0" should " be equivalent to a Float of value 1.0f" in {
    Number(1.0) should be (Number(1.0f))
  }

  "A Double of value 1.5" should " be equivalent to a Float of value 1.5f" in {
    Number(1.5) should be (Number(1.5f))
  }

  "A Double of value 1.0" should " be equivalent to an Integer of value 1" in {
    Number(1.0) should be (Number(1))
  }
}
