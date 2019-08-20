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
package com.szadowsz.gospel.core.db.theory

import com.szadowsz.gospel.core.Interpreter
import com.szadowsz.gospel.core.data.{Struct, Term}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class TheorySpec extends FlatSpec with Matchers {

 behavior of "Theory Object"

  it should "be able to handle strings with parenthesis" in {
    val before = "a :- b, (d ; e)."
    val theory = new Theory(before)
    val after = theory.toString
    new Theory(after).toString shouldBe theory.toString
  }

  it should "be able to append clause lists" in {
    val clauseList = Array[Term](new Struct("p"), new Struct("q"), new Struct("r"))
    val otherClauseList = Array[Term](new Struct("a"), new Struct("b"), new Struct("c"))
    val theory = new Theory(new Struct(clauseList))
    theory.append(new Theory(new Struct(otherClauseList)))

    val prolog = new Interpreter()
    prolog.setTheory(theory)
    prolog.solve("p.").isSuccess shouldBe true
    prolog.solve("b.").isSuccess shouldBe true
  }
}
