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
package com.szadowsz.gospel.core.data

import java.util

/**
  * Float class represents the float prolog data type, stored as Doubles
  */
@SerialVersionUID(1L)
case class Float(value: scala.Double) extends Number {

  /**
    * Returns the value of the Double as int
    */
  override final def intValue: scala.Int = value.toInt

  /**
    * Returns the value of the Double as float
    */
  override final def floatValue: scala.Float = value.toFloat

  /**
    * Returns the value of the Double as double
    */
  override final def doubleValue: scala.Double = value

  /**
    * Returns the value of the Double as long
    */
  override final def longValue: scala.Long = value.toLong

  /**
    * is this term a prolog integer term?
    */
  override final def isInteger = false

  /**
    * is this term a prolog real term?
    */
  override final def isReal = true

  /**
    * Returns true if this Double term is grater that the term provided.
    * For number term argument, the int value is considered.
    */
  override def isGreater(t: Term): Boolean = {
    t.getTerm match {
      case n : Number => value > n.doubleValue
      case term : Term => !term.isInstanceOf[Struct] && term.isInstanceOf[Var]
    }
  }

  /**
    * Tries to unify a term with the provided term argument.
    * This service is to be used in demonstration context.
    */
  override def unify(vl1: util.List[Var], vl2: util.List[Var], t: Term, isOccursCheckEnabled: Boolean): Boolean = {
    t.getTerm match {
      case v: Var => v.unify(vl2, vl1, this, isOccursCheckEnabled)
      case term: Term => term.isInstanceOf[Number] && term.asInstanceOf[Number].isReal && value == term.asInstanceOf[Number].doubleValue
    }
  }

  override def toString: String = java.lang.Double.toString(value)

  def resolveVariables(count: Int): Int = count

  /**
    * @author Paolo Contessi
    */
  override def compareTo(o: Number): scala.Int = value.compareTo(o.doubleValue)

  override private[data] def unify(varsUnifiedArg1: util.List[Var], varsUnifiedArg2: util.List[Var], t: Term) = {
    unify(varsUnifiedArg1, varsUnifiedArg2, t, true)
  }
}