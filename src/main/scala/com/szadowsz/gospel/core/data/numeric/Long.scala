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
package com.szadowsz.gospel.core.data.numeric

import java.{util => ju}

import com.szadowsz.gospel.core.data.{Struct, Term, Var}

/**
 *
 * Long class represents the long prolog data type
 *
 *
 *
 */
@SerialVersionUID(1L)
class Long(v: scala.Long) extends Number {
  private val _value: scala.Long = v

  /**
   * Returns the value of the Long as int
   */
  final override def intValue: scala.Int = _value.toInt

  /**
   * Returns the value of the Long as float
   */
  final override def floatValue: scala.Float = _value.toFloat

  /**
   * Returns the value of the Long as double
   */
  final override def doubleValue: scala.Double = _value.toDouble

  /**
   * Returns the value of the Long as long
   */
  final override def longValue: scala.Long = _value

  /**
   * is this term a prolog integer term?
   */
  final override def isInteger: Boolean = true


  /** is this term a prolog real term? */
  final override def isReal: Boolean = false

  /**
   * Returns true if this integer term is grater that the term provided.
   * For number term argument, the int value is considered.
   */
  override def isGreater(theTerm : Term): Boolean = {
    val t = theTerm.getTerm
    t match {
      case num : Number => _value > num.longValue
      case struct : Struct => false
      case variable : Var => true
      case _ => false
    }
  }

  override def isGreaterRelink(theTerm : Term, vorder: ju.ArrayList[String]): Boolean = isGreater(theTerm)


  /**
   * Returns true if this integer term is equal that the term provided.
   * For number term argument, the int value is considered.
   */
  override def isEqual(theTerm : Term): Boolean = {
    val t = theTerm.getTerm
    t match {
      case num : Number => num.isInteger && _value == num.longValue
      case _ => false
    }
  }

  /**
   * Tries to unify a term with the provided term argument.
   * This service is to be used in demonstration context.
   */
  override def unify(vl1: ju.List[Var], vl2: ju.List[Var], theTerm : Term): Boolean = {
    val t = theTerm.getTerm
    t match {
      case variable : Var => variable.unify(vl1, vl2, this)
      case num : Number => num.isInteger && longValue == num.longValue
      case _ => false
    }
  }

  override def toString: String = {
    java.lang.Long.toString(_value)
  }

  /**
   * @author Paolo Contessi
   */
  override def compareTo(o: Number): scala.Int = {
    _value.compareTo(o.longValue)
  }
}