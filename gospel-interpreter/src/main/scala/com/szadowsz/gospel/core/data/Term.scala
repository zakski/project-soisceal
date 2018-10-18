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



abstract class Term extends Serializable {


  /**
    * Resolves variables inside the term
    *
    * If the variables has been already resolved, no renaming is done.
    */
  def resolveTerm() : Unit

  def isAtom: Boolean = false

  /**
    * Check if this struct is a clause.
    *
    * @return true if this is a clause, false otherwise
    */
  def isClause: Boolean = false

  def isGround: Boolean

  def isEmptyList: Boolean = false

  def isEquals(term: Term): Boolean

  def isList: Boolean = false

  final override def equals(obj: Any): Boolean = obj.isInstanceOf[Term] && isEquals(obj.asInstanceOf[Term])

  /**
    * Gets the actual term referred by this Term.
    *
    * @return if the Term is a bound variable, the method gets the Term linked to the variable, otherwise returns itself.
    */
  def getBinding: Term = this
}
