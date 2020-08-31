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
package com.szadowsz.gospel.core.data

import java.util

import com.szadowsz.gospel.core.engine.Executor

abstract class Number extends Term {
  
  /**
    * gets a copy (with renamed variables) of the term.
    *
    * The list argument passed contains the list of variables to be renamed (if empty list then no renaming).
    *
    * Used By The engine to initialise it's stack
    *
    * @param vMap      variables to rename
    * @param idExecCtx Execution Context identifier
    * @return Copy of Term
    */
  override def init(e: Executor, vMap: util.AbstractMap[Var, Var], idExecCtx: scala.Int): Term = {
    executor = e
    this // TODO Revisit behaviour
  }
  
  final override private[data] def copy(vMap: util.AbstractMap[Var, Var], substMap: util.AbstractMap[Term, Var]) = {
    this
  }
  
  final override private[data] def resolveVars(count: scala.Long) = count
  
  final override def isAtomic: Boolean = true
  
  final override def isGround: Boolean = true
  
  final override def isGreater(other: Term): Boolean = {
    other.isInstanceOf[Number] && doubleValue > other.asInstanceOf[Number].doubleValue
  }
  
  /**
    * Returns the value of the Double as int
    */
  def intValue: scala.Int
  
  /**
    * Returns the value of the Double as float
    */
  def floatValue: scala.Float
  
  /**
    * Returns the value of the Double as double
    */
  def doubleValue: scala.Double
  
  /**
    * Returns the value of the Double as long
    */
  def longValue: scala.Long
  
  /**
    * is this term a prolog integer term?
    */
  def isInteger: Boolean
  
  /**
    * is this term a prolog real term?
    */
  def isReal: Boolean
}
