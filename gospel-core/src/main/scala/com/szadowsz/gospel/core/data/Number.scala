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

  /** is this term a prolog integer term? */
  def isInteger: Boolean

  /** is this term a prolog real term? */
  def isReal: Boolean

  /**
    * Gets the actual term referred by this Term.
    */
  override def getTerm: Term = this

  override final def isEmptyList = false

  /** is this term a constant prolog term? */
  override final def isAtomic = true

  /** is this term a prolog compound term? */
  override final def isCompound = false

  /** is this term a prolog (alphanumeric) atom? */
  override final def isAtom = false

  /** is this term a prolog list? */
  override final def isList = false

  /** is this term a ground term? */
  override final def isGround = true

  /**
    * gets a copy of this term.
    */
  def copy(idExecCtx: Int): Term = this

  /**
    * gets a copy (with renamed variables) of the term.
    * <p>
    * the list argument passed contains the list of variables to be renamed
    * (if empty list then no renaming)
    */
  override def copy(vMap: util.AbstractMap[Var, Var], idExecCtx: scala.Int): Term = this

  /**
    * gets a copy of the term.
    */
  override private[data] def copy(vMap: util.AbstractMap[Var, Var], substMap: util.AbstractMap[Term, Var]) = this

  override def copyAndRetainFreeVar(vMap: util.AbstractMap[Var, Var], idExecCtx: scala.Int): Term = { // TODO Auto-generated method stub
    this
  }

  override private[data] def resolveTerm(count: scala.Long) = count

  /**
    *
    */
  override def free(): Unit = {
  }

  private[data] def restoreVariables() = {
  }
}