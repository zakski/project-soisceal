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

final case class Float(value: Double) extends Number {
  
  override def intValue: scala.Int = value.toInt
  
  override def floatValue: scala.Float = value.toFloat
  
  override def doubleValue: scala.Double = value
  
  override def longValue: scala.Long = value.toLong
  
  override def isInteger: Boolean = false
  
  override def isReal: Boolean = true
  
  override def isEquals(other: Term): Boolean = other.isInstanceOf[Float] && other.asInstanceOf[Float].value == value
  
  /**
    * Tries to unify a term with the provided term argument.
    * This service is to be used in demonstration context.
    */
  override def unify(vl1: util.List[Var], vl2: util.List[Var], t: Term, isOccursCheckEnabled: Boolean): Boolean = {
    t.getBinding match {
      case v: Var => v.unify(vl2, vl1, this, isOccursCheckEnabled)
      case term: Term => term.isInstanceOf[Number] && term.asInstanceOf[Number].isReal && value == term.asInstanceOf[Number].doubleValue
    }
  }
  
  override def toString: String = value.toString
}
