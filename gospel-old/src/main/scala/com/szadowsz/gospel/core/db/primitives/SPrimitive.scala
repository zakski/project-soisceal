/*
 * tuProlog - Copyright (C) 2001-2002  aliCE team at deis.unibo.it
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
package com.szadowsz.gospel.core.db.primitives

import alice.tuprolog.IPrimitives
import com.szadowsz.gospel.core.data.Struct
import com.szadowsz.gospel.core.db.primitives.slang._

object SPrimitive{

  def apply(primitiveType: Int, primitiveKey: String, source: IPrimitives, primitiveFunc : AnyRef): SPrimitive = {
    primitiveFunc match {
      case f0 : Function0[_] => new SPrimitive0(primitiveType,primitiveKey,source,primitiveFunc)
      case f1 : Function1[_,_] => new SPrimitive1(primitiveType,primitiveKey,source,primitiveFunc)
      case f2 : Function2[_,_,_] => new SPrimitive2(primitiveType,primitiveKey,source,primitiveFunc)
      case f3 : Function3[_,_,_,_] => new SPrimitive3(primitiveType,primitiveKey,source,primitiveFunc)
      case f4 : Function4[_,_,_,_,_] => new SPrimitive4(primitiveType,primitiveKey,source,primitiveFunc)
      case f5 : Function5[_,_,_,_,_,_] => new SPrimitive5(primitiveType,primitiveKey,source,primitiveFunc)
      case f6 : Function6[_,_,_,_,_,_,_] => new SPrimitive6(primitiveType,primitiveKey,source,primitiveFunc)
      case f7 : Function7[_,_,_,_,_,_,_,_] => new SPrimitive0(primitiveType,primitiveKey,source,primitiveFunc)
      case f8 : Function8[_,_,_,_,_,_,_,_,_] => new SPrimitive0(primitiveType,primitiveKey,source,primitiveFunc)
      case f9 : Function9[_,_,_,_,_,_,_,_,_,_] => new SPrimitive0(primitiveType,primitiveKey,source,primitiveFunc)
      case f10 : Function10[_,_,_,_,_,_,_,_,_,_,_] => new SPrimitive0(primitiveType,primitiveKey,source,primitiveFunc)
      case f11 : Function11[_,_,_,_,_,_,_,_,_,_,_,_] => new SPrimitive0(primitiveType,primitiveKey,source,primitiveFunc)
      case f12 : Function12[_,_,_,_,_,_,_,_,_,_,_,_,_] => new SPrimitive0(primitiveType,primitiveKey,source,primitiveFunc)
      case f13 : Function13[_,_,_,_,_,_,_,_,_,_,_,_,_,_] => new SPrimitive0(primitiveType,primitiveKey,source,primitiveFunc)
      case f14 : Function14[_,_,_,_,_,_,_,_,_,_,_,_,_,_,_] => new SPrimitive0(primitiveType,primitiveKey,source,primitiveFunc)
      case f15 : Function15[_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_] => new SPrimitive0(primitiveType,primitiveKey,source,primitiveFunc)
      case f16 : Function16[_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_] => new SPrimitive0(primitiveType,primitiveKey,source,primitiveFunc)
      case f17 : Function17[_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_] => new SPrimitive0(primitiveType,primitiveKey,source,primitiveFunc)
      case f18 : Function18[_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_] => new SPrimitive0(primitiveType,primitiveKey,source,primitiveFunc)
      case f19 : Function19[_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_] => new SPrimitive0(primitiveType,primitiveKey,source,primitiveFunc)
      case f20 : Function20[_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_] => new SPrimitive0(primitiveType,primitiveKey,source,primitiveFunc)
      case f21 : Function21[_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_] => new SPrimitive0(primitiveType,primitiveKey,source,primitiveFunc)
      case f22 : Function22[_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_] => new SPrimitive0(primitiveType,primitiveKey,source,primitiveFunc)
      case default => throw new IllegalArgumentException("primitiveFunc " + primitiveKey + " is not a valid scalafunction")
    }
  }
}


abstract class SPrimitive(primitiveType : Int, primitiveKey: String, source: IPrimitives, primitiveArity: Int) extends PrimitiveInfo {

  protected val primType : Int = primitiveType

  protected val key : String = primitiveKey

  protected val src : IPrimitives = source

  protected val arity : Int = primitiveArity

  final override def getKey: String = key

  final override def isDirective: Boolean = primType == PrimitiveInfo.DIRECTIVE

  final override def isFunctor: Boolean = primType == PrimitiveInfo.FUNCTOR

  final override def isPredicate: Boolean = primType == PrimitiveInfo.PREDICATE

  final override def getType: Int = primType

  final override def getSource: IPrimitives = src
}
