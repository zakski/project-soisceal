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

import java.util

import com.szadowsz.gospel.core.data.Var
import com.szadowsz.gospel.core.engine.context.goal.SubGoalId

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

case class ChoicePointContext(
                               compatibleGoals : ClauseStore,
                               execContext : ExecutionContext,
                               indexSubGoal : SubGoalId,
                               varsToDeunify : List[util.List[Var]]
                             ) {
  
  var prevContext : Option[ChoicePointContext] = None
  
  override def toString: String = {
    //"varsToDeunify: "+getVarsToDeunify()+"\n"+
    s"""     ChoicePointId: ${execContext.id}:$indexSubGoal
       |     compGoals:     $compatibleGoals
    """.stripMargin
  }
  
  def getVarsToDeunify: util.List[util.List[Var]] = {
    val l = new util.ArrayList[util.List[Var]]
    varsToDeunify.foreach(h => l.add(h))
    l
  }
}