/**
  * tuProlog - Copyright (C) 2001-2002  aliCE team at deis.unibo.it
  *
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
package com.szadowsz.gospel.core.data

import java.util

/**
  * Int class represents the integer prolog data type
  */
@SerialVersionUID(1L)
final case class Int(value: scala.Int) extends Number {

  /**
    * Returns the value of the Integer as int
    */
  override def intValue: scala.Int = value

  /**
    * Returns the value of the Integer as float
    */
  override def floatValue: scala.Float = value.toFloat

  /**
    * Returns the value of the Integer as double
    */
  override def doubleValue: scala.Double = value.toDouble

  /**
    * Returns the value of the Integer as long
    */
  override def longValue: scala.Long = value

  /**
    * is this term a prolog integer term?
    */
  override def isInteger = true

  /**
    * is this term a prolog real term?
    */
  override def isReal = false

  /**
    * Returns true if this integer term is grater that the term provided.
    * For number term argument, the int value is considered.
    */
  override def isGreater(t: Term): Boolean = {
    t.getTerm match {
      case n : Number => value > n.intValue
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
      case term: Term => term.isInstanceOf[Number] && term.asInstanceOf[Number].isInteger && value == term.asInstanceOf[Number].intValue
    }
  }

  override def toString: String = Integer.toString(value)

  /**
    * @author Paolo Contessi
    */
  override def compareTo(o: Number): scala.Int = value.compareTo(o.intValue)

  override private[data] def unify(varsUnifiedArg1: util.List[Var], varsUnifiedArg2: util.List[Var], t: Term) = {
    unify(varsUnifiedArg1, varsUnifiedArg2, t, true)
  }
}