/**
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3.0 of the License, or (at your option) any later version.
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
package com.szadowsz.gospel.core.parser

import java.util.NoSuchElementException

import com.szadowsz.gospel.core.data.{Int, Struct}
import com.szadowsz.gospel.core.db.operators.OperatorManager
import com.szadowsz.gospel.core.exception.InvalidTermException
import com.szadowsz.gospel.core.{BaseEngineSpec, Interpreter}
import org.junit.runner.RunWith
import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ParserIteratorSpec extends FlatSpec with Matchers {
  
  implicit val opManager: OperatorManager = new OperatorManager
  
  
  behavior of "Parser Iterator"

  it should "throw an exception if there is no next element" in {
    val theory: String = ""
    val i = new Parser(theory).iterator
    i.hasNext shouldBe false
    intercept[NoSuchElementException] {
      i.next
    }
  }

  it should "be able to iterate through the correct number of elements" in {
    val theory = "q(1).\nq(2).\nq(3).\nq(5).\nq(7)."
    val i = new Parser(theory).iterator
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
    val i = new Parser(theory).iterator
    i.hasNext shouldBe true
    i.hasNext shouldBe true
    i.hasNext shouldBe true
    i.next shouldBe new Struct("p")
  }

  it should "be able to handle multiple next calls" in {
    val theory: String = "p(X):-q(X),X>1.\nq(1).\nq(2).\nq(3).\nq(5).\nq(7)."
    val i = new Parser(theory).iterator
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
      val i = new Parser(t).iterator
      i.next()
    }
  }

  it should "iterate until it reaches an invalid term" in {
    val theory = "q(1).\nq(2).\nq(3)\nq(5).\nq(7)." // missing the End-Of-Clause!
    val firstTerm = new Struct("q", new Int(1))
    val secondTerm = new Struct("q", new Int(2))
    val i = new Parser(theory).iterator
    i.hasNext shouldBe true
    i.next shouldBe firstTerm
    i.hasNext shouldBe true
    i.next shouldBe secondTerm
    intercept[InvalidTermException] {
      i.hasNext
    }
  }
}
