/*
 * tuProlog - Copyright (C) 2001-2007  aliCE team at deis.unibo.it
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.szadowsz.gospel.core.data

import java.util.NoSuchElementException

import org.junit.runner.RunWith
import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class StructIteratorSpec extends FlatSpec with Matchers {

  behavior of "Struct Iterator"

  it should "throw an exception if there is no next element" in {
    val list = new Struct
    val i = list.getListIterator
    i.hasNext shouldBe false
    intercept[NoSuchElementException] {
      i.next
    }
  }

  it should "be able to iterate through the correct number of elements" in {
    val list = new Struct(Array[Term](new Int(1), new Int(2), new Int(3), new Int(5), new Int(7)))
    val i = list.getListIterator
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
    val i = list.getListIterator
    i.hasNext shouldBe true
    i.hasNext shouldBe true
    i.hasNext shouldBe true
    i.next shouldBe new Struct("p")
  }

  it should "be able to handle multiple next calls" in {
    val list = new Struct(Array[Term](new Int(0), new Int(1), new Int(2), new Int(3), new Int(5), new Int(7)))
    val i = list.getListIterator
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
}
