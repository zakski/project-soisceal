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

import com.szadowsz.gospel.core.data.{Term, Var}

object Number {

  def apply(input: Any): Number = {
    input match {
      case i : scala.Int => new Int(i)
      case f : scala.Float => new Double(f)
      case d : scala.Double => new Double(d)
    }
  }
}

/**
 *
 * Number abstract class represents numbers prolog data type
 *
 * @see Int
 * @see Long
 * @see Float
 * @see Double
 *
 *      Reviewed by Paolo Contessi: implements Comparable<Number>
 */
@SerialVersionUID(1L)
abstract class Number extends Term with Comparable[Number] {
  /**
   * Returns the value of the number as int
   */
  def intValue: scala.Int

  /**
   * Returns the value of the number as float
   */
  def floatValue: scala.Float

  /**
   * Returns the value of the number as long
   */
  def longValue: scala.Long

  /**
   * Returns the value of the number as double
   */
  def doubleValue: scala.Double

  /**
   *  is this term a prolog integer term?
   */
  def isInteger: Boolean

  /**
   * is this term a prolog real term?
   */
  def isReal: Boolean

  /**
   * is this term a null term?
   */
  final override def isEmptyList: Boolean = false

  /**
   * is this term a constant prolog term?
   */
  final override def isAtomic: Boolean = true

  /**
   *  is this term a prolog compound term?
   */
  final override def isCompound: Boolean = false

  /**
   * is this term a prolog (alphanumeric) atom?
   */
  final override def isAtom: Boolean = false

  /**
   * is this term a prolog list?
   */
  final override def isList: Boolean = false

  /**
   * is this term a ground term?
   */
  final override def isGround: Boolean = true

  /**
   * Gets the actual term referred by this Term.
   */
  override def getTerm: Term = this

  /**
   * gets a copy (with renamed variables) of the term.
   * <p>
   * the list argument passed contains the list of variables to be renamed
   * (if empty list then no renaming)
   */
  private[gospel] override def copy(vMap: ju.AbstractMap[Var, Var], idExecCtx: scala.Int): Term = this

  /**
   * gets a copy of the term.
   */
  private[gospel] override def copy(vMap: ju.AbstractMap[Var, Var], substMap: ju.AbstractMap[Term, Var]): Term = this


  private[gospel] override def resolveTerm(count: scala.Long): scala.Long = count

  override def free {}
}