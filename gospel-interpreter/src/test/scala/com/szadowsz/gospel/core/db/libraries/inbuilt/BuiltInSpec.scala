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
package com.szadowsz.gospel.core.db.libraries.inbuilt

import com.szadowsz.gospel.core.Interpreter
import com.szadowsz.gospel.core.db.primitives.PrimitiveType
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class BuiltInSpec extends FlatSpec with Matchers with BeforeAndAfter {

  protected var wam : Interpreter = _
  protected var builtIn : BuiltIn = _

  behavior of "Built-In Library"

  before {
    wam = new Interpreter
    builtIn = new BuiltIn(wam)
  }


  it should "return the current number of annotated methods" in {
    val prims = builtIn.getPrimitives

    prims should have size 1
    prims.getOrElse(PrimitiveType.DIRECTIVE,Seq()) should have length 1
    prims.getOrElse(PrimitiveType.FUNCTOR,Seq()) should have length 0
    prims.getOrElse(PrimitiveType.PREDICATE,Seq()) should have length 0
  }
}
