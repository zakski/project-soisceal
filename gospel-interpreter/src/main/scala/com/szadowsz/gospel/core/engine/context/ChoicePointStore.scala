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
package com.szadowsz.gospel.core.engine.context

class ChoicePointStore() {
  
  private var pointer : Option[ChoicePointContext] = None
  
  def add(cpc: ChoicePointContext): Unit = {
    pointer match {
      case None => pointer = Option(cpc)
      
      case Some(parent) =>
        cpc.prevContext = parent
        pointer = Option(cpc)
    }
  }
  
  def cut(pointerAfterCut: ChoicePointContext): Unit = {
    pointer = Option(pointerAfterCut)
  }
  
  
  
  /**
    * Returns a valid choice-point, removing choice points which have been already used and are now empty.
    *
    * @return a valid choice-point if one can be found, otherwise None
    */
  def findValidChoice: Option[ChoicePointContext] = {
    pointer match {
      case None => None
      case Some(potential) =>
        val clauses = potential.compatibleGoals
        if (clauses.existCompatibleClause){
          pointer
        } else {
          pointer = Option(potential.prevContext)
          findValidChoice
        }
    }
  }
  
  /**
    * Return the actual choice-point store
    *
    * @return
    */
  def getPointer: ChoicePointContext = pointer.orNull
  
  
  /**
    * Removes choice points which have been already used and are now empty.
    */
  def removeUnusedChoicePoints(): Unit = { // Note: it uses the side effect of this.existChoicePoint()!
    findValidChoice
  }
  
  override def toString: String = pointer + "\n"
}