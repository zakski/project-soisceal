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
package com.szadowsz.gospel.core.db.primitives

import com.szadowsz.gospel.core.data.{Int, Struct, Term}
import com.szadowsz.gospel.core.db.primitives.slang.{SPrimitive0, SPrimitiveN}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class PrimitiveSpec extends FlatSpec with Matchers with BeforeAndAfter {

  behavior of "Primitive Functions"

  val f0Bool : Function0[Boolean] = () => true
  val f0Term : Function0[Term] = () => Int(1)
  val f0Unit : Function0[Unit] = () => println()

  val f1Bool : Function1[Term,Boolean] = {
    case i: Int => i.value % 2 == 0
    case t: Any => throw new NumberFormatException(s"$t is not a Number")
  }
  val f1Term : Function1[Term,Term] = {
    case i: Int => Int(i.value*2)
    case t: Any => throw new NumberFormatException(s"$t is not a Number")
  }
  val f1Unit : Function1[Term,Unit] = t => println(t)

  val s1Odd = new Struct("test",Int(1))
  val s1Even = new Struct("test",Int(2))

  val f3Bool : Function3[Term,Term,Term,Boolean] = (t1,t2,t3) => {
    (t1.asInstanceOf[Int].value + t2.asInstanceOf[Int].value) == t3.asInstanceOf[Int].value
  }

  val f3Term : Function3[Term,Term,Term,Term] =(t1,t2,t3) => {
    Int(t1.asInstanceOf[Int].value + t2.asInstanceOf[Int].value + t3.asInstanceOf[Int].value)
  }

  val f3Unit : Function3[Term,Term,Term,Unit] = (t1,t2,t3) => {
    println(t1)
    println(t2)
    println(t3)
  }

  val s3 = new Struct("test",Int(1),Int(2),Int(3))


  it should "return a SFunction0 for 0-arg Boolean Functions" in {
    val sf = Primitive(PrimitiveType.PREDICATE,"true/0",null,f0Bool)
    sf shouldBe a [SPrimitive0]
    sf.evalAsPredicate(null) shouldBe true
  }

  it should "return a SFunction0 for 0-arg Unit Functions" in {
    val sf = Primitive(PrimitiveType.DIRECTIVE,"true/0",null,f0Unit)
    sf shouldBe a [SPrimitive0]
    sf.evalAsDirective(null)
  }

  it should "return a SFunction0 for 0-arg Term Functions" in {
    val sf = Primitive(PrimitiveType.FUNCTOR,"true/0",null,f0Term)
    sf shouldBe a [SPrimitive0]
    sf.evalAsFunctor(null) shouldBe Int(1)
  }

  it should "return a SFunctionN for 1-arg Boolean Functions" in {
    val sf = Primitive(PrimitiveType.PREDICATE,"true/0",null,f1Bool)
    sf shouldBe a [SPrimitiveN]
    sf.evalAsPredicate(s1Odd) shouldBe false
    sf.evalAsPredicate(s1Even) shouldBe true
  }

  it should "return a SFunctionN for 1-arg Unit Functions" in {
    val sf = Primitive(PrimitiveType.DIRECTIVE,"true/0",null,f1Unit)
    sf shouldBe a [SPrimitiveN]
    sf.evalAsDirective(s1Odd)
  }

  it should "return a SFunctionN for 1-arg Term Functions" in {
    val sf = Primitive(PrimitiveType.FUNCTOR,"true/0",null,f1Term)
    sf shouldBe a [SPrimitiveN]
    sf.evalAsFunctor(s1Odd) shouldBe Int(2)
    sf.evalAsFunctor(s1Even) shouldBe Int(4)
  }

  it should "return a SFunctionN for 3-arg Boolean Functions" in {
    val sf = Primitive(PrimitiveType.PREDICATE,"true/0",null,f3Bool)
    sf shouldBe a [SPrimitiveN]
    sf.evalAsPredicate(s3) shouldBe true
  }

  it should "return a SFunctionN for 3-arg Unit Functions" in {
    val sf = Primitive(PrimitiveType.DIRECTIVE,"true/0",null,f3Unit)
    sf shouldBe a [SPrimitiveN]
    sf.evalAsDirective(s3)
  }

  it should "return a SFunctionN for 3-arg Term Functions" in {
    val sf = Primitive(PrimitiveType.FUNCTOR,"true/0",null,f3Term)
    sf shouldBe a [SPrimitiveN]
    sf.evalAsFunctor(s3) shouldBe Int(6)
  }
}
