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
package com.szadowsz.gospel.core.db.primitive

import com.szadowsz.gospel.core.data.{Struct, Term}
import com.szadowsz.gospel.util.InspectionUtils
import java.lang.reflect.{InvocationTargetException, Method}

import com.szadowsz.gospel.core.db.lib.Library

/**
 * Primitive class
 * referring to a builtin predicate or functor
 *
 * @see Struct
 */
object PrimitiveInfo {
  val DIRECTIVE: Int = 0
  val PREDICATE: Int = 1
  val FUNCTOR: Int = 2
}

@throws(classOf[NoSuchMethodException])
class PrimitiveInfo(theType: Int, key: String, lib: Library, m: Method, arity: Int) {
  private val _type: Int = theType

  /**
   * method to be call when evaluating the built-in
   */
  private val _method: Method =  if (m == null) { throw new NoSuchMethodException} else m
  /**
   * lib object where the builtin is defined
   */
  private val source: Library = lib
  /**
   * for optimization purposes
   */
  private val primitive_args = new Array[Term](arity)
  private var primitive_key: String = key

  /**
   * Method to invalidate primitives. It's called just mother library removed
   */
  def invalidate: String = {
    val key: String = primitive_key
    primitive_key = null
     key
  }

  def getKey: String = {
     primitive_key
  }

  def isDirective: Boolean = {
     (_type == PrimitiveInfo.DIRECTIVE)
  }

  def isFunctor: Boolean = {
     (_type == PrimitiveInfo.FUNCTOR)
  }

  def isPredicate: Boolean = {
     (_type == PrimitiveInfo.PREDICATE)
  }

  def getType: Int = {
     _type
  }

  def getSource: Library = {
     source
  }

  /**
    * evaluates the primitive as a directive
    *
    * @throws InvocationTargetException
    * @throws IllegalAccessException
    */
  @throws(classOf[IllegalAccessException])
  @throws(classOf[InvocationTargetException])
  def evalAsDirective(g: Struct) {
    {
      var i: Int = 0
      while (i < primitive_args.length) {
        {
          primitive_args(i) = g.getTerm(i)
        }
        ({
          i += 1; i - 1
        })
      }
    }
    InspectionUtils.methodCall(source, _method,primitive_args.asInstanceOf[Array[Object]])
  }

  /**
   * evaluates the primitive as a predicate
   * @throws Exception if invocation primitive failure
   */
  @throws(classOf[Throwable])
  def evalAsPredicate(g: Struct): Boolean = {
    {
      var i: Int = 0
      while (i < primitive_args.length) {
        {
          primitive_args(i) = g.getArg(i)
        }
        ({
          i += 1; i - 1
        })
      }
    }
    try {
      InspectionUtils.methodCall(source, _method,primitive_args.asInstanceOf[Array[Object]]).asInstanceOf[Boolean].booleanValue
    }
    catch {
      case e: InvocationTargetException => {
        throw e.getCause
      }
    }
  }

  /**
   * evaluates the primitive as a functor
   * @throws Throwable
   */
  @throws(classOf[Throwable])
  def evalAsFunctor(g: Struct): Term = {
    try { {
      var i: Int = 0
      while (i < primitive_args.length) {
        {
          primitive_args(i) = g.getTerm(i)
        }
        ({
          i += 1; i - 1
        })
      }
    }
    InspectionUtils.methodCall(source, _method,primitive_args.asInstanceOf[Array[Object]]).asInstanceOf[Term]
    }
    catch {
      case ex: Exception => {
        throw ex.getCause
      }
    }
  }

  override def toString: String = {
     "[ primitive: method " + _method.getName + " - " + primitive_args + " - N args: " + primitive_args.length + " - " + source.getClass.getName + " ]\n"
  }
}