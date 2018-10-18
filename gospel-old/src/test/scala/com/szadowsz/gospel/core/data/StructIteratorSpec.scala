package com.szadowsz.gospel.core.data

import java.util.NoSuchElementException

import com.szadowsz.gospel.core.{BaseEngineSpec, PrologEngine}
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class StructIteratorSpec extends FlatSpec with BaseEngineSpec {

  override protected def init(): PrologEngine = new PrologEngine()

  behavior of "Struct Iterator"

  it should "throw an exception if there is no next element" in {
    val list = new Struct
    val i = list.listIterator
    i.hasNext shouldBe false
    intercept[NoSuchElementException] {
      i.next
    }
  }

  it should "be able to iterate through the correct number of elements" in {
    val list = new Struct(Array[Term](new Int(1), new Int(2), new Int(3), new Int(5), new Int(7)))
    val i = list.listIterator
    var count = 0

    while (i.hasNext) {
      i.next
      count += 1
    }
    count shouldBe 5
    i.hasNext shouldBe false
  }

  it should "be able to handle multiple hasNext calls" in {
    val list = new Struct(Array[Term](new Struct("p"), new Struct("q"), new Struct("r")))
    val i = list.listIterator
    i.hasNext shouldBe true
    i.hasNext shouldBe true
    i.hasNext shouldBe true
    i.next shouldBe new Struct("p")
  }

  it should "be able to handle multiple next calls" in {
    val list = new Struct(Array[Term](new Int(0), new Int(1), new Int(2), new Int(3), new Int(5), new Int(7)))
    val i = list.listIterator
    i.hasNext shouldBe true
    i.next // skip the first term

    i.next shouldBe new Int(1)
    i.next shouldBe new Int(2)
    i.next shouldBe new Int(3)
    i.next shouldBe new Int(5)
    i.next shouldBe new Int(7)

    // no more terms
    i.hasNext shouldBe false
    intercept[NoSuchElementException] {
      i.next
    }
  }

  it should "not support the remove operation" in {
    val list = new Struct(new Int(1), new Struct)
    val i = list.listIterator
    i.next should not be null
    intercept[UnsupportedOperationException] {
      i.remove()
    }
  }
}
