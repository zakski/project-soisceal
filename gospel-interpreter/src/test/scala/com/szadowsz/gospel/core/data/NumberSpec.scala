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

import org.junit.runner.RunWith
import org.scalatest.{FunSpec, Matchers}
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class NumberSpec extends FunSpec with Matchers {
  
  describe("Int") {

    it("should represent itself as a String correctly") {
      Int(0).toString should be ("0")
    }

    it("should be atomic") {
       Int(0).isAtomic should be (true)
    }

    it("should not be an atom") {
       Int(0).isAtom should be (false)
    }

    it("should not be a compound") {
       Int(0).isCompound should be (false)
    }

    it("should not be equal to a struct") {
       Int(0) should not be new Struct()
    }

    it("should not be equal to a var") {
       Int(1) should not be new Var("X")
    }

    it("should not be equal to an int of a different value") {
       Int(0) should not be Int(1)
    }

    it("should be equal to an int of the same value") {
       Int(0) should be ( Int(0))
    }
    
    it("should not be equal to a float of the same value") {
       Int(1) should not be Float(1)
    }
  }

  describe("Float") {

    it("should represent itself as a String correctly") {
      Float(0).toString should be("0.0")
    }

    it("should be atomic") {
      Float(0).isAtomic should be(true)
    }

    it("should not be an atom") {
      Float(0).isAtom should be(false)
    }

    it("should not be a compound") {
      Float(0).isCompound should be(false)
    }

    it("should not be equal to a struct") {
      Float(0) should not be new Struct()
    }

    it("should not be equal to a var") {
      Float(1) should not be new Var("X")
    }

    it("should not be equal to a float of a different value") {
      Float(0) should not be Float(1)
    }

    it("should be equal to a float of the same value") {
      Float(0) should be(Float(0))
    }

    it("should not be equal to an int of the same value") {
      Float(1) should not be Int(1)
    }
  }
}
