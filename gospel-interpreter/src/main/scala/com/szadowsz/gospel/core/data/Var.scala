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

class Var(val name : String) extends Term {

  /**
    * term used for unification process
    */
  private var binding : Option[Term] = None

  /**
    * Resolves variables inside the term
    *
    * If the variables has been already resolved, no renaming is done.
    */
  override def resolveTerm(): Unit = ???

  override def isGround: Boolean = binding.exists(_.isGround)

  override def isEmptyList: Boolean = binding.exists(_.isEmptyList)

  override def isList: Boolean = binding.exists(_.isList)

  override def isEquals(term: Term): Boolean = {
    term match {
      case v : Var => name == v.name && binding.sameElements(v.binding)
      case _ => false
    }
  }

  override def getBinding: Term = binding.getOrElse(this)
}
