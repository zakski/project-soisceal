package com.szadowsz.gospel.core.data

import java.util.NoSuchElementException

import com.szadowsz.gospel.core.error.InvalidTermException
import com.szadowsz.gospel.core.{BaseEngineSpec, PrologEngine}
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TermIteratorSpec extends FlatSpec with BaseEngineSpec {

  override protected def init(): PrologEngine = new PrologEngine()

  behavior of "Term Iterator"

  it should "throw an exception if there is no next element" in {
    val theory: String = ""
    val i = prolog.createTerms(theory)
    i.hasNext shouldBe false
    intercept[NoSuchElementException] {
      i.next
    }
  }

  it should "be able to iterate through the correct number of elements" in {
    val theory = "q(1).\nq(2).\nq(3).\nq(5).\nq(7)."
    val i = prolog.createTerms(theory)
    var count = 0

    while (i.hasNext) {
      i.next
      count += 1
    }
    count shouldBe 5
    i.hasNext shouldBe false
  }

  it should "be able to handle multiple hasNext calls" in {
    val theory = "p. q. r."
    val i = prolog.createTerms(theory)
    i.hasNext shouldBe true
    i.hasNext shouldBe true
    i.hasNext shouldBe true
    i.next shouldBe new Struct("p")
  }

  it should "be able to handle multiple next calls" in {
    val theory: String = "p(X):-q(X),X>1.\nq(1).\nq(2).\nq(3).\nq(5).\nq(7)."
    val i = prolog.createTerms(theory)
    i.hasNext shouldBe true
    i.next // skip the first term

    i.next shouldBe new Struct("q",new Int(1))
    i.next shouldBe new Struct("q",new Int(2))
    i.next shouldBe new Struct("q",new Int(3))
    i.next shouldBe new Struct("q",new Int(5))
    i.next shouldBe new Struct("q",new Int(7))

    // no more terms
    i.hasNext shouldBe false
    intercept[NoSuchElementException] {
      i.next
    }
  }

  it should "not support iterating on invalid terms" in {
    val t = "q(1)" // missing the End-Of-Clause!
    intercept[InvalidTermException] {
      prolog.createTerms(t)
    }
  }

  it should "iterate until it reaches an invalid term" in {
    val engine = new PrologEngine
    val theory = "q(1).\nq(2).\nq(3)\nq(5).\nq(7)." // missing the End-Of-Clause!
    val firstTerm = new Struct("q", new Int(1))
    val secondTerm = new Struct("q", new Int(2))
    val i = engine.createTerms(theory)
    i.hasNext shouldBe true
    i.next shouldBe firstTerm
    i.hasNext shouldBe true
    i.next shouldBe secondTerm
    intercept[InvalidTermException] {
      i.hasNext
    }
  }

    it should "not support the remove operation" in {
   val theory = "p(1)."
    val i = prolog.createTerms(theory)
    i.next should not be null
    intercept[UnsupportedOperationException] {
      i.remove()
    }
  }
}
