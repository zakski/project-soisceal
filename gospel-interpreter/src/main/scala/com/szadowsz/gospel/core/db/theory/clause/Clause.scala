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
package com.szadowsz.gospel.core.db.theory.clause

import com.szadowsz.gospel.core.data.{Struct, Term}

object Clause {

  /**
    * Gets a clause from a generic Term
    */
  private def extractHead(clause: Struct): Struct = {
    clause(0).asInstanceOf[Struct]
  }
}
/**
  * This class mantains information about a clause creation
  * (clause copy, final time T after renaming, validity stillValid Flag).
  * These information are necessary to the Theory Manager
  * to use the clause in a consistent way
  *
  * CONSTRUCTOR
  * building a valid clause with a time stamp = original time stamp + NumVar in clause
  */
private[theory] class Clause protected(clause: Struct, head: Struct, /* TODO body: SubGoalTree,*/ libName: Option[String]) {

  def this(clause: Struct, lib: Option[String]) {
    this(clause, Clause.extractHead(clause),  /* TODO ClauseInfo.extractBody(clause.getArg(1)),*/ lib)
  }

  def getHead: Struct = head

  def getClause: Struct = clause
}
