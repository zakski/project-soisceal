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
import com.szadowsz.gospel.core.data.Term
import java.lang.reflect.InvocationTargetException

object PrimitiveInfo{
  val DIRECTIVE = 0
  val PREDICATE = 1
  val FUNCTOR = 2
}

trait PrimitiveInfo {

  def getKey: String

  def isDirective: Boolean

  def isFunctor: Boolean

  def isPredicate: Boolean

  def getType: Int

  def getSource: IPrimitives

  @throws[IllegalAccessException]
  @throws[InvocationTargetException]
  def evalAsDirective(g: Struct): Unit

  @throws[Throwable]
  def evalAsPredicate(g: Struct): Boolean

  @throws[Throwable]
  def evalAsFunctor(g: Struct): Term

  override def toString: String
}