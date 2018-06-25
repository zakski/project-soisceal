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

import com.szadowsz.gospel.core.exception.InvalidTermException
import com.szadowsz.gospel.core.{BaseEngineSpec, PrologEngine}
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class StructSpec extends FlatSpec with BaseEngineSpec {

  override protected def init(): PrologEngine = new PrologEngine()

  behavior of "Struct"

  it should "not have null arguments #1" in {
    intercept[InvalidTermException] {
      new Struct("p", null.asInstanceOf[Term])
    }
  }

  it should "not have null arguments #2" in {
    intercept[InvalidTermException] {
      new Struct("p",  Int(1), null)
    }
  }

  it should "not have null arguments #3" in {
    intercept[InvalidTermException] {
      new Struct("p",  Int(1),  Int(2), null)
    }
  }

  it should "not have null arguments #4" in {
    intercept[InvalidTermException] {
      new Struct("p",  Int(1),  Int(2),  Int(3), null)
    }
  }

  it should "not have null arguments #5" in {
    intercept[InvalidTermException] {
      new Struct("p",  Int(1),  Int(2),  Int(3),  Int(4), null)
    }
  }

  it should "not have null arguments #6" in {
    intercept[InvalidTermException] {
      new Struct("p",  Int(1),  Int(2),  Int(3),  Int(4),  Int(5), null)
    }
  }

  it should "not have null arguments #7" in {
    intercept[InvalidTermException] {
      new Struct("p",  Int(1),  Int(2),  Int(3),  Int(4),  Int(5),  Int(6), null)
    }
  }

  it should "not have null arguments #8" in {
    intercept[InvalidTermException] {
      val args = Array[Term](new Struct("a"), null, new Var("P"))
      new Struct("p", args)
    }
  }

  it should "not have a null name" in {
    intercept[InvalidTermException] {
      new Struct(null,  Int(1),  Int(2))
    }
  }

  it should "not have an empty name if not an atom" in {
    intercept[InvalidTermException] {
      new Struct("",  Int(1),  Int(2))
    }
  }

  it should "be able to represent an empty list" in {
    val list = new Struct
    list.isList shouldBe true
    list.isEmptyList shouldBe true
    list.listSize shouldBe 0
    list.getName shouldBe "[]"
    list.getArity shouldBe 0
  }

  it should "be able to represent an empty list as a squared struct" in {
    val list = new Struct("[]")
    list.isList shouldBe true
    list.isEmptyList shouldBe true
    list.listSize shouldBe 0
    list.getName shouldBe "[]"
    list.getArity shouldBe 0
  }

  it should "be able to represent an empty list as a dotted struct" in {
    val list = new Struct(".")
    list.isList shouldBe false
    list.isEmptyList shouldBe false
    list.getName shouldBe "."
    list.getArity shouldBe 0
  }

  it should "be able to represent a nonempty list as a dotted struct" in {
    val notAnEmptyList = new Struct(".", new Struct("a"), new Struct(".", new Struct("b"), new Struct))
    notAnEmptyList.isList shouldBe true
    notAnEmptyList.isEmptyList shouldBe false
    notAnEmptyList.getName shouldBe "."
    notAnEmptyList.getArity shouldBe 2
  }

  it should "be able to construct a list from an argument array" in {
    new Struct(new Array[Term](0)) shouldBe new Struct

    val args = new Array[Term](2)
    args(0) = new Struct("a")
    args(1) = new Struct("b")

    val list = new Struct(args)
    list.listTail.listTail shouldBe new Struct
  }

  it should "be able to correctly report the list size" in {
    val list = new Struct(new Struct("a"), new Struct(new Struct("b"), new Struct(new Struct("c"), new Struct)))
    list.isList shouldBe true
    list.isEmptyList shouldBe false
    list.listSize shouldBe 3
  }

  it should "not support returning head of a list from a non list" in {
    val s = new Struct("f", new Var("X"))
    intercept[UnsupportedOperationException] {
      s.listHead
    }
  }

  it should "not support returning tail of a list from a non list" in {
    val s = new Struct("h",  Int(1))
    intercept[UnsupportedOperationException] {
      s.listTail
    }
  }

  it should "not support returning size of a list from a non list" in {
    val s = new Struct("f", new Var("X"))
    intercept[UnsupportedOperationException] {
      s.listSize
    }
  }

  it should "not support returning a list iterator from a non list" in {
    val s = new Struct("f", new Var("X"))
    intercept[UnsupportedOperationException] {
      s.listIterator
    }
  }

  it should "support the toList method properly" in {
    val emptyList = new Struct
    val emptyListToList = new Struct(new Struct("[]"), new Struct)
    emptyList.toStructList shouldBe emptyListToList
  }

  it should "support the toString method properly" in {
    val emptyList = new Struct
    emptyList.toString shouldBe "[]"

    val s = new Struct("f", new Var("X"))
    s.toString shouldBe "f(X)"

    val list = new Struct(new Struct("a"), new Struct(new Struct("b"), new Struct(new Struct("c"), new Struct)))
    list.toString shouldBe "[a,b,c]"
 }

  it should "support appending object to itself properly" in {
    var emptyList = new Struct
    val list = new Struct(new Struct("a"), new Struct(new Struct("b"), new Struct(new Struct("c"), new Struct)))
    emptyList.append(new Struct("a"))
    emptyList.append(new Struct("b"))
    emptyList.append(new Struct("c"))
    emptyList shouldBe list

    val tail = new Struct(new Struct("b"), new Struct(new Struct("c"), new Struct))
    emptyList.listTail shouldBe tail

    emptyList = new Struct
    emptyList.append(new Struct)
    emptyList shouldBe new Struct(new Struct, new Struct)

    val anotherList = new Struct(new Struct("d"), new Struct(new Struct("e"), new Struct))
    list.append(anotherList)
    list.listTail.listTail.listTail.listHead shouldBe anotherList
  }

  it should "support iterated goal term" in {
    val x = new Var("X")
    val foo = new Struct("foo", x)
    val term = new Struct("^", x, foo)
    term.iteratedGoalTerm shouldBe foo
  }

  it should "support not being alist" in {
    val notList = new Struct(".", new Struct("a"), new Struct("b"))
    notList.isList shouldBe false
  }

  it should "be able to be atomic" in {
    val emptyList = new Struct
    emptyList.isAtomic shouldBe true

    val atom = new Struct("atom")
    atom.isAtomic shouldBe true

    val list = new Struct(Array[Term]( Int(0),  Int(1)))
    list.isAtomic shouldBe false

    val compound = new Struct("f", new Struct("a"), new Struct("b"))
    compound.isAtomic shouldBe false

    val singleQuoted = new Struct("'atom'")
    singleQuoted.isAtomic shouldBe true

    val doubleQuoted = new Struct("\"atom\"")
    doubleQuoted.isAtomic shouldBe true
  }

  it should "be able to be an atom" in {
    val emptyList = new Struct
    emptyList.isAtom shouldBe true

    val atom = new Struct("atom")
    atom.isAtom shouldBe true

    val list = new Struct(Array[Term]( Int(0),  Int(1)))
    list.isAtom shouldBe false

    val compound = new Struct("f", new Struct("a"), new Struct("b"))
    compound.isAtom shouldBe false

    val singleQuoted = new Struct("'atom'")
    singleQuoted.isAtom shouldBe true

    val doubleQuoted = new Struct("\"atom\"")
    doubleQuoted.isAtom shouldBe true
  }

  it should "be able to be a compound" in {
    val emptyList = new Struct
    emptyList.isCompound shouldBe false

    val atom = new Struct("atom")
    atom.isCompound shouldBe false

    val list = new Struct(Array[Term]( Int(0),  Int(1)))
    list.isCompound shouldBe true

    val compound = new Struct("f", new Struct("a"), new Struct("b"))
    compound.isCompound shouldBe true

    val singleQuoted = new Struct("'atom'")
    singleQuoted.isCompound shouldBe false

    val doubleQuoted = new Struct("\"atom\"")
    doubleQuoted.isCompound shouldBe false
  }
}
