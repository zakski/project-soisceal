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

import java.lang.reflect.InvocationTargetException

import com.szadowsz.gospel.core.data.{Struct, Term}
import com.szadowsz.gospel.core.db.libraries.Library
import com.szadowsz.gospel.core.db.primitives.slang.{SPrimitive0, SPrimitiveN}
import com.szadowsz.gospel.core.engine.Executor


object Primitive {

  def apply(primType: PrimitiveType, id : String, src : Library, func : Any): Primitive = {
      func match {
        case f0 : Function0[_] => new SPrimitive0(primType,id,src,f0)
        case CurriedFunc(fn) => new SPrimitiveN(primType,id,src,CurriedFunc.curry(fn).get)
      }
  }
}

trait Primitive {

  protected val primType: PrimitiveType
  protected val id : String
  protected val source : Library

  final def getKey: String = id

  final def isDirective: Boolean = primType == PrimitiveType.DIRECTIVE

  final def isFunctor: Boolean = primType == PrimitiveType.FUNCTOR

  final def isPredicate: Boolean = primType == PrimitiveType.PREDICATE

  final def getType: PrimitiveType = primType

  def getSource: Library = source

  @throws[IllegalAccessException]
  @throws[InvocationTargetException]
  def evalAsDirective(g: Struct): Unit

  @throws[Throwable]
  def evalAsPredicate(g: Struct): Boolean

  @throws[Throwable]
  def evalAsFunctor(g: Struct): Term
}